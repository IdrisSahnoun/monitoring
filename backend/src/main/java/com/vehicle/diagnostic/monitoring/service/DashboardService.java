package com.vehicle.diagnostic.monitoring.service;

import com.vehicle.diagnostic.monitoring.dto.DashboardStatsDTO;
import com.vehicle.diagnostic.monitoring.model.DiagnosticSession;
import com.vehicle.diagnostic.monitoring.model.WorkerEvent;
import com.vehicle.diagnostic.monitoring.repository.DiagnosticSessionRepository;
import com.vehicle.diagnostic.monitoring.repository.WorkerEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for dashboard statistics and analytics
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DashboardService {
    
    private final DiagnosticSessionRepository sessionRepository;
    private final WorkerEventRepository workerEventRepository;
    private final ApplicationInsightsService appInsightsService;
    
    /**
     * Get comprehensive dashboard statistics
     */
    @Cacheable(value = "dashboardStats", key = "#startDate + '-' + #endDate")
    public DashboardStatsDTO getDashboardStats(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating dashboard statistics from {} to {}", startDate, endDate);
        
        List<DiagnosticSession> sessions = sessionRepository.findSessionsBetweenDates(startDate, endDate);
        List<WorkerEvent> events = workerEventRepository.findEventsBetweenDates(startDate, endDate);
        
        return DashboardStatsDTO.builder()
                .totalSessions((long) sessions.size())
                .activeSessions(countByStatus(sessions, DiagnosticSession.SessionStatus.RUNNING))
                .completedSessions(countByStatus(sessions, DiagnosticSession.SessionStatus.CLOSED))
                .failedSessions(countByStatus(sessions, DiagnosticSession.SessionStatus.ERROR))
                .overallSuccessRate(calculateSuccessRate(sessions))
                .averageSessionDuration(calculateAverageDuration(sessions))
                .workerMetrics(calculateWorkerMetrics(events))
                .sessionsByStatus(groupSessionsByStatus(sessions))
                .sessionsByHour(groupSessionsByHour(sessions))
                .build();
    }
    
    /**
     * Get real-time statistics (shorter cache TTL)
     */
    @Cacheable(value = "realtimeStats", key = "'current'")
    public Map<String, Object> getRealtimeStats() {
        log.info("Fetching real-time statistics");
        
        Long activeSessions = sessionRepository.countByStatus(DiagnosticSession.SessionStatus.RUNNING);
        Long completedToday = sessionRepository.countByStatus(DiagnosticSession.SessionStatus.CLOSED);
        Long failedToday = sessionRepository.countByStatus(DiagnosticSession.SessionStatus.ERROR);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeSessions", activeSessions);
        stats.put("completedToday", completedToday);
        stats.put("failedToday", failedToday);
        stats.put("timestamp", LocalDateTime.now());
        
        return stats;
    }
    
    /**
     * Get worker-specific statistics
     */
    @Cacheable(value = "workerStats", key = "#workerName")
    public DashboardStatsDTO.WorkerMetrics getWorkerStats(String workerName) {
        log.info("Calculating statistics for worker: {}", workerName);
        
        List<WorkerEvent> events = workerEventRepository.findByWorkerName(workerName);
        
        long totalExecutions = events.size();
        long successful = events.stream()
                .filter(e -> e.getStatus() == WorkerEvent.WorkerStatus.COMPLETED)
                .count();
        long failed = events.stream()
                .filter(e -> e.getStatus() == WorkerEvent.WorkerStatus.FAILED)
                .count();
        
        double avgExecTime = events.stream()
                .filter(e -> e.getExecutionTimeMs() != null)
                .mapToLong(WorkerEvent::getExecutionTimeMs)
                .average()
                .orElse(0.0);
        
        double successRate = totalExecutions > 0 ? (successful * 100.0 / totalExecutions) : 0.0;
        
        return DashboardStatsDTO.WorkerMetrics.builder()
                .workerName(workerName)
                .totalExecutions(totalExecutions)
                .successfulExecutions(successful)
                .failedExecutions(failed)
                .successRate(successRate)
                .averageExecutionTime(avgExecTime)
                .build();
    }
    
    // Helper methods
    
    private Long countByStatus(List<DiagnosticSession> sessions, DiagnosticSession.SessionStatus status) {
        return sessions.stream()
                .filter(s -> s.getStatus() == status)
                .count();
    }
    
    private Double calculateSuccessRate(List<DiagnosticSession> sessions) {
        if (sessions.isEmpty()) return 0.0;
        
        long completed = sessions.stream()
                .filter(s -> s.getStatus() == DiagnosticSession.SessionStatus.COMPLETED)
                .count();
        
        return (completed * 100.0) / sessions.size();
    }
    
    private Double calculateAverageDuration(List<DiagnosticSession> sessions) {
        return sessions.stream()
                .filter(s -> s.getDurationMs() != null)
                .mapToLong(DiagnosticSession::getDurationMs)
                .average()
                .orElse(0.0);
    }
    
    private Map<String, DashboardStatsDTO.WorkerMetrics> calculateWorkerMetrics(List<WorkerEvent> events) {
        Map<String, List<WorkerEvent>> eventsByWorker = events.stream()
                .collect(Collectors.groupingBy(WorkerEvent::getWorkerName));
        
        Map<String, DashboardStatsDTO.WorkerMetrics> metrics = new HashMap<>();
        
        eventsByWorker.forEach((workerName, workerEvents) -> {
            long total = workerEvents.size();
            long successful = workerEvents.stream()
                    .filter(e -> e.getStatus() == WorkerEvent.WorkerStatus.COMPLETED)
                    .count();
            long failed = workerEvents.stream()
                    .filter(e -> e.getStatus() == WorkerEvent.WorkerStatus.FAILED)
                    .count();
            
            double avgTime = workerEvents.stream()
                    .filter(e -> e.getExecutionTimeMs() != null)
                    .mapToLong(WorkerEvent::getExecutionTimeMs)
                    .average()
                    .orElse(0.0);
            
            metrics.put(workerName, DashboardStatsDTO.WorkerMetrics.builder()
                    .workerName(workerName)
                    .totalExecutions(total)
                    .successfulExecutions(successful)
                    .failedExecutions(failed)
                    .successRate(total > 0 ? (successful * 100.0 / total) : 0.0)
                    .averageExecutionTime(avgTime)
                    .build());
        });
        
        return metrics;
    }
    
    private Map<String, Long> groupSessionsByStatus(List<DiagnosticSession> sessions) {
        return sessions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getStatus().name(),
                        Collectors.counting()
                ));
    }
    
    private Map<String, Long> groupSessionsByHour(List<DiagnosticSession> sessions) {
        return sessions.stream()
                .collect(Collectors.groupingBy(
                        s -> String.format("%02d:00", s.getStartTime().getHour()),
                        Collectors.counting()
                ));
    }
}
