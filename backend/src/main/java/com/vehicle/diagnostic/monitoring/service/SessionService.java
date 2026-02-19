package com.vehicle.diagnostic.monitoring.service;

import com.vehicle.diagnostic.monitoring.dto.*;
import com.vehicle.diagnostic.monitoring.model.DiagnosticSession;
import com.vehicle.diagnostic.monitoring.model.WorkerEvent;
import com.vehicle.diagnostic.monitoring.repository.DiagnosticSessionRepository;
import com.vehicle.diagnostic.monitoring.repository.WorkerEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing and querying diagnostic sessions
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SessionService {
    
    private final DiagnosticSessionRepository sessionRepository;
    private final WorkerEventRepository workerEventRepository;
    private final ApplicationInsightsService appInsightsService;
    
    /**
     * Get session by ID with caching
     */
    @Cacheable(value = "sessions", key = "#sessionId")
    public SessionDTO getSession(String sessionId) {
        log.info("🔍 Fetching session: {}", sessionId);
        
        // First try local database
        Optional<DiagnosticSession> localSession = sessionRepository.findBySessionId(sessionId);
        
        if (localSession.isPresent()) {
            DiagnosticSession session = localSession.get();
            log.info("✓ Found session {} - Status: {} - Duration: {}ms",
                    sessionId, session.getStatus(), session.getDurationMs());
            
            // Log worker events summary
            List<WorkerEvent> events = workerEventRepository.findBySessionIdOrderByStepNumberAsc(sessionId);
            long failedWorkers = events.stream()
                    .filter(e -> e.getStatus() == WorkerEvent.WorkerStatus.FAILED)
                    .count();
            
            if (failedWorkers > 0) {
                log.warn("⚠ Session {} has {} failed worker(s)", sessionId, failedWorkers);
                events.stream()
                        .filter(e -> e.getStatus() == WorkerEvent.WorkerStatus.FAILED)
                        .forEach(e -> log.warn("  - {} failed: {}", e.getWorkerName(), e.getErrorDetails()));
            } else {
                log.info("✓ All {} workers completed successfully", events.size());
            }
            
            return convertToDTO(session);
        }
        
        // If not in local DB, query Application Insights
        log.warn("⚠ Session {} not found in local database, querying Azure...", sessionId);
        Map<String, Object> aiData = appInsightsService.querySessionData(sessionId);
        
        // TODO: Convert Application Insights data to SessionDTO when integration is ready
        return SessionDTO.builder()
                .sessionId(sessionId)
                .build();
    }
    
    /**
     * Get all sessions with filtering and pagination
     */
    public Page<SessionDTO> getSessions(SessionFilterDTO filter) {
        log.info("Fetching sessions with filter: {}", filter);
        
        Pageable pageable = createPageable(filter);
        
        DiagnosticSession.SessionStatus status = filter.getStatus() != null 
            ? DiagnosticSession.SessionStatus.valueOf(filter.getStatus()) 
            : null;
        
        Page<DiagnosticSession> sessions = sessionRepository.findByFilters(
            filter.getSessionId(),
            filter.getVehicleId(),
            status,
            filter.getStartDate(),
            filter.getEndDate(),
            pageable
        );
        
        return sessions.map(this::convertToDTO);
    }
    
    /**
     * Get worker events for a session
     */
    @Cacheable(value = "workerEvents", key = "#sessionId")
    public List<WorkerEventDTO> getWorkerEvents(String sessionId) {
        log.info("🔍 Fetching worker events for session: {}", sessionId);
        
        List<WorkerEvent> events = workerEventRepository.findBySessionIdOrderByStepNumberAsc(sessionId);
        
        log.info("Found {} worker events for session {}", events.size(), sessionId);
        
        // Log summary of worker execution
        for (WorkerEvent event : events) {
            if (event.getStatus() == WorkerEvent.WorkerStatus.COMPLETED) {
                log.info("  ✓ Step {}: {} - COMPLETED in {}ms",
                        event.getStepNumber(), event.getMessageType(), event.getExecutionTimeMs());
            } else if (event.getStatus() == WorkerEvent.WorkerStatus.FAILED) {
                log.error("  ✗ Step {}: {} - FAILED: {}",
                        event.getStepNumber(), event.getMessageType(), event.getErrorDetails());
            } else {
                log.debug("  ⏳ Step {}: {} - {}",
                        event.getStepNumber(), event.getMessageType(), event.getStatus());
            }
        }
        
        return events.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get performance metrics for a session
     */
    @Cacheable(value = "performanceMetrics", key = "#sessionId")
    public PerformanceMetrics getSessionMetrics(String sessionId) {
        log.info("Calculating performance metrics for session: {}", sessionId);
        
        List<WorkerEvent> events = workerEventRepository.findBySessionId(sessionId);
        
        return calculateMetrics(events);
    }
    
    /**
     * Create or update session from worker events
     * This will be called when receiving events from Azure
     */
    @Transactional
    public DiagnosticSession saveSession(DiagnosticSession session) {
        log.info("Saving session: {}", session.getSessionId());
        return sessionRepository.save(session);
    }
    
    /**
     * Save worker event
     */
    @Transactional
    public WorkerEvent saveWorkerEvent(WorkerEvent event) {
        log.info("Saving worker event for session: {}, worker: {}", 
                event.getSessionId(), event.getWorkerName());
        return workerEventRepository.save(event);
    }
    
    // Helper methods
    
    private SessionDTO convertToDTO(DiagnosticSession session) {
        List<WorkerEventDTO> workerEvents = getWorkerEvents(session.getSessionId());
        PerformanceMetrics metrics = getSessionMetrics(session.getSessionId());
        
        Double progress = 0.0;
        if (session.getTotalSteps() != null && session.getTotalSteps() > 0) {
            progress = (session.getCompletedSteps() * 100.0) / session.getTotalSteps();
        }
        
        return SessionDTO.builder()
                .sessionId(session.getSessionId())
                .vehicleId(session.getVehicleId())
                .vehicleVin(session.getVehicleVin())
                .diagnosticType(session.getDiagnosticType())
                .status(session.getStatus().name())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .durationMs(session.getDurationMs())
                .currentWorker(session.getCurrentWorker())
                .completedSteps(session.getCompletedSteps())
                .totalSteps(session.getTotalSteps())
                .progressPercentage(progress)
                .errorMessage(session.getErrorMessage())
                .workerEvents(workerEvents)
                .performanceMetrics(metrics)
                .build();
    }
    
    private WorkerEventDTO convertToDTO(WorkerEvent event) {
        return WorkerEventDTO.builder()
                .eventId(event.getId())
                .sessionId(event.getSessionId())
                .workerName(event.getMessageType() != null ? event.getMessageType().name() : null)
                .status(event.getStatus().name())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .executionTimeMs(event.getExecutionTimeMs())
                .stepNumber(event.getStepNumber())
                .inputData(event.getInputData())
                .outputData(event.getOutputData())
                .errorDetails(event.getErrorDetails())
                .retryCount(event.getRetryCount())
                .build();
    }
    
    private PerformanceMetrics calculateMetrics(List<WorkerEvent> events) {
        if (events.isEmpty()) {
            return PerformanceMetrics.builder().build();
        }
        
        List<Long> executionTimes = events.stream()
                .filter(e -> e.getExecutionTimeMs() != null)
                .map(WorkerEvent::getExecutionTimeMs)
                .sorted()
                .collect(Collectors.toList());
        
        long successful = events.stream()
                .filter(e -> e.getStatus() == WorkerEvent.WorkerStatus.COMPLETED)
                .count();
        
        long failed = events.stream()
                .filter(e -> e.getStatus() == WorkerEvent.WorkerStatus.FAILED)
                .count();
        
        double avgTime = executionTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
        
        return PerformanceMetrics.builder()
                .totalExecutions(events.size())
                .successfulExecutions((int) successful)
                .failedExecutions((int) failed)
                .successRate(successful * 100.0 / events.size())
                .failureRate(failed * 100.0 / events.size())
                .averageExecutionTime(avgTime)
                .minExecutionTime(executionTimes.isEmpty() ? 0L : executionTimes.get(0))
                .maxExecutionTime(executionTimes.isEmpty() ? 0L : executionTimes.get(executionTimes.size() - 1))
                .p50LatencyMs(getPercentile(executionTimes, 50))
                .p95LatencyMs(getPercentile(executionTimes, 95))
                .p99LatencyMs(getPercentile(executionTimes, 99))
                .build();
    }
    
    private Double getPercentile(List<Long> sortedValues, int percentile) {
        if (sortedValues.isEmpty()) return 0.0;
        int index = (int) Math.ceil(percentile / 100.0 * sortedValues.size()) - 1;
        return sortedValues.get(Math.max(0, Math.min(index, sortedValues.size() - 1))).doubleValue();
    }
    
    private Pageable createPageable(SessionFilterDTO filter) {
        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? filter.getSize() : 20;
        String sortBy = filter.getSortBy() != null ? filter.getSortBy() : "startTime";
        Sort.Direction direction = "asc".equalsIgnoreCase(filter.getSortDirection()) 
            ? Sort.Direction.ASC 
            : Sort.Direction.DESC;
        
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}
