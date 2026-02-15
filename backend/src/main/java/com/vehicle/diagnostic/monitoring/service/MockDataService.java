package com.vehicle.diagnostic.monitoring.service;

import com.vehicle.diagnostic.monitoring.model.DiagnosticSession;
import com.vehicle.diagnostic.monitoring.model.WorkerEvent;
import com.vehicle.diagnostic.monitoring.repository.DiagnosticSessionRepository;
import com.vehicle.diagnostic.monitoring.repository.WorkerEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for generating mock data for testing without Azure access
 * This allows testing the full system locally until Azure integration is ready
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MockDataService {
    
    private final DiagnosticSessionRepository sessionRepository;
    private final WorkerEventRepository workerEventRepository;
    
    private static final String[] VEHICLE_IDS = {
        "VEH-001", "VEH-002", "VEH-003", "VEH-004", "VEH-005",
        "VEH-006", "VEH-007", "VEH-008", "VEH-009", "VEH-010"
    };
    
    private static final String[] VINS = {
        "1HGBH41JXMN109186", "2HGFB2F51EH542914", "3FADP4EJ1DM191234",
        "4T1BF1FK5CU123456", "5FNRL6H78HB012345", "WBAPL33569A123456",
        "1G1ZD5ST1HF123456", "2C4RDGCG1ER123456", "3VWDX7AJ8FM123456",
        "1GNKVGKD1FJ123456"
    };
    
    private static final String[] WORKER_NAMES = {
        "InitializationWorker",
        "DataCollectionWorker",
        "AnalysisWorker",
        "ValidationWorker",
        "ReportGenerationWorker",
        "NotificationWorker"
    };
    
    private static final String[] DIAGNOSTIC_TYPES = {
        "FULL_DIAGNOSTIC", "ENGINE_DIAGNOSTIC", "ELECTRICAL_DIAGNOSTIC",
        "BRAKE_DIAGNOSTIC", "TRANSMISSION_DIAGNOSTIC"
    };
    
    private final Random random = new Random();
    
    /**
     * Generate a specified number of mock diagnostic sessions with worker events
     */
    @Transactional
    public int generateMockData(int numberOfSessions) {
        log.info("========================================");
        log.info("📊 Generating {} mock diagnostic sessions...", numberOfSessions);
        log.info("========================================");
        
        int created = 0;
        int completedCount = 0;
        int failedCount = 0;
        int inProgressCount = 0;
        
        for (int i = 0; i < numberOfSessions; i++) {
            try {
                DiagnosticSession session = createMockSession();
                sessionRepository.save(session);
                
                List<WorkerEvent> events = createMockWorkerEvents(session);
                workerEventRepository.saveAll(events);
                
                // Count by status
                if (session.getStatus() == DiagnosticSession.SessionStatus.COMPLETED) {
                    completedCount++;
                } else if (session.getStatus() == DiagnosticSession.SessionStatus.FAILED) {
                    failedCount++;
                } else if (session.getStatus() == DiagnosticSession.SessionStatus.IN_PROGRESS) {
                    inProgressCount++;
                }
                
                // Log session details
                log.info("✓ Created session {}/{}: {} - Status: {} - Workers: {}/{}",
                        i + 1, numberOfSessions,
                        session.getSessionId(),
                        session.getStatus(),
                        session.getCompletedSteps(),
                        session.getTotalSteps());
                
                // Log worker events for this session
                for (WorkerEvent event : events) {
                    if (event.getStatus() == WorkerEvent.WorkerStatus.FAILED) {
                        log.warn("  ⚠ Worker {} FAILED: {}", event.getWorkerName(), event.getErrorDetails());
                    } else {
                        log.debug("  ✓ Worker {} completed in {}ms", event.getWorkerName(), event.getExecutionTimeMs());
                    }
                }
                
                created++;
            } catch (Exception e) {
                log.error("❌ Error creating mock session", e);
            }
        }
        
        log.info("========================================");
        log.info("✅ Successfully created {} mock sessions", created);
        log.info("   - Completed: {} ({}%)", completedCount, (completedCount * 100 / created));
        log.info("   - Failed: {} ({}%)", failedCount, (failedCount * 100 / created));
        log.info("   - In Progress: {} ({}%)", inProgressCount, (inProgressCount * 100 / created));
        log.info("   - Total Worker Events: {}", created * 6);
        log.info("========================================");
        return created;
    }
    
    /**
     * Generate mock data for different time periods (last 24h, last 7d, last 30d)
     */
    @Transactional
    public Map<String, Integer> generateTimeBasedMockData() {
        Map<String, Integer> stats = new HashMap<>();
        
        // Last 24 hours - 50 sessions
        stats.put("last24h", generateSessionsForPeriod(50, 24));
        
        // Last 7 days - 200 sessions
        stats.put("last7d", generateSessionsForPeriod(200, 168)); // 7 * 24
        
        // Last 30 days - 500 sessions
        stats.put("last30d", generateSessionsForPeriod(500, 720)); // 30 * 24
        
        return stats;
    }
    
    /**
     * Clear all mock data from database
     */
    @Transactional
    public void clearAllData() {
        log.info("Clearing all mock data...");
        workerEventRepository.deleteAll();
        sessionRepository.deleteAll();
        log.info("All mock data cleared");
    }
    
    /**
     * Get statistics about current mock data
     */
    public Map<String, Object> getMockDataStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalSessions", sessionRepository.count());
        stats.put("totalEvents", workerEventRepository.count());
        stats.put("completedSessions", sessionRepository.countByStatus(DiagnosticSession.SessionStatus.COMPLETED));
        stats.put("failedSessions", sessionRepository.countByStatus(DiagnosticSession.SessionStatus.FAILED));
        stats.put("activeSessions", sessionRepository.countByStatus(DiagnosticSession.SessionStatus.IN_PROGRESS));
        
        return stats;
    }
    
    // Private helper methods
    
    private DiagnosticSession createMockSession() {
        String sessionId = "SESSION-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String vehicleId = VEHICLE_IDS[random.nextInt(VEHICLE_IDS.length)];
        String vin = VINS[random.nextInt(VINS.length)];
        String diagnosticType = DIAGNOSTIC_TYPES[random.nextInt(DIAGNOSTIC_TYPES.length)];
        
        LocalDateTime startTime = generateRandomStartTime(24); // Within last 24 hours
        DiagnosticSession.SessionStatus status = generateRandomStatus();
        
        LocalDateTime endTime = null;
        Long durationMs = null;
        Integer completedSteps = 0;
        String errorMessage = null;
        
        if (status == DiagnosticSession.SessionStatus.COMPLETED) {
            durationMs = 10000L + random.nextInt(120000); // 10s to 2min
            endTime = startTime.plusSeconds(durationMs / 1000);
            completedSteps = WORKER_NAMES.length;
        } else if (status == DiagnosticSession.SessionStatus.FAILED) {
            durationMs = 5000L + random.nextInt(60000); // 5s to 1min
            endTime = startTime.plusSeconds(durationMs / 1000);
            completedSteps = random.nextInt(WORKER_NAMES.length);
            errorMessage = generateRandomError();
        } else if (status == DiagnosticSession.SessionStatus.IN_PROGRESS) {
            completedSteps = random.nextInt(WORKER_NAMES.length);
        }
        
        return DiagnosticSession.builder()
                .sessionId(sessionId)
                .vehicleId(vehicleId)
                .vehicleVin(vin)
                .diagnosticType(diagnosticType)
                .status(status)
                .startTime(startTime)
                .endTime(endTime)
                .durationMs(durationMs)
                .currentWorker(status == DiagnosticSession.SessionStatus.IN_PROGRESS ? 
                        WORKER_NAMES[completedSteps] : null)
                .completedSteps(completedSteps)
                .totalSteps(WORKER_NAMES.length)
                .errorMessage(errorMessage)
                .build();
    }
    
    private List<WorkerEvent> createMockWorkerEvents(DiagnosticSession session) {
        List<WorkerEvent> events = new ArrayList<>();
        
        LocalDateTime eventTime = session.getStartTime();
        int stepsToCreate = session.getCompletedSteps() != null ? session.getCompletedSteps() : 0;
        
        if (session.getStatus() == DiagnosticSession.SessionStatus.COMPLETED) {
            stepsToCreate = WORKER_NAMES.length;
        }
        
        for (int i = 0; i < stepsToCreate; i++) {
            String workerName = WORKER_NAMES[i];
            Long executionTime = 1000L + random.nextInt(20000); // 1s to 20s
            
            WorkerEvent.WorkerStatus workerStatus = WorkerEvent.WorkerStatus.COMPLETED;
            String errorDetails = null;
            Integer retryCount = 0;
            
            // Last worker might have failed if session failed
            if (i == stepsToCreate - 1 && session.getStatus() == DiagnosticSession.SessionStatus.FAILED) {
                workerStatus = WorkerEvent.WorkerStatus.FAILED;
                errorDetails = session.getErrorMessage();
                retryCount = random.nextInt(3);
            }
            
            WorkerEvent event = WorkerEvent.builder()
                    .sessionId(session.getSessionId())
                    .workerName(workerName)
                    .status(workerStatus)
                    .startTime(eventTime)
                    .endTime(eventTime.plusSeconds(executionTime / 1000))
                    .executionTimeMs(executionTime)
                    .stepNumber(i + 1)
                    .inputData(generateMockInputData(workerName))
                    .outputData(workerStatus == WorkerEvent.WorkerStatus.COMPLETED ? 
                            generateMockOutputData(workerName) : null)
                    .errorDetails(errorDetails)
                    .retryCount(retryCount)
                    .build();
            
            events.add(event);
            eventTime = eventTime.plusSeconds(executionTime / 1000);
        }
        
        return events;
    }
    
    private int generateSessionsForPeriod(int count, int hoursAgo) {
        int created = 0;
        for (int i = 0; i < count; i++) {
            try {
                DiagnosticSession session = createMockSession();
                // Adjust start time to be within the specified period
                LocalDateTime startTime = generateRandomStartTime(hoursAgo);
                session.setStartTime(startTime);
                
                if (session.getEndTime() != null && session.getDurationMs() != null) {
                    session.setEndTime(startTime.plusSeconds(session.getDurationMs() / 1000));
                }
                
                sessionRepository.save(session);
                
                List<WorkerEvent> events = createMockWorkerEvents(session);
                workerEventRepository.saveAll(events);
                
                created++;
            } catch (Exception e) {
                log.error("Error creating session for period", e);
            }
        }
        return created;
    }
    
    private LocalDateTime generateRandomStartTime(int maxHoursAgo) {
        int hoursAgo = random.nextInt(maxHoursAgo);
        int minutesAgo = random.nextInt(60);
        return LocalDateTime.now().minusHours(hoursAgo).minusMinutes(minutesAgo);
    }
    
    private DiagnosticSession.SessionStatus generateRandomStatus() {
        int rand = random.nextInt(100);
        if (rand < 70) return DiagnosticSession.SessionStatus.COMPLETED; // 70%
        if (rand < 85) return DiagnosticSession.SessionStatus.FAILED; // 15%
        if (rand < 95) return DiagnosticSession.SessionStatus.IN_PROGRESS; // 10%
        return DiagnosticSession.SessionStatus.INITIATED; // 5%
    }
    
    private String generateRandomError() {
        String[] errors = {
            "Connection timeout to vehicle ECU",
            "Invalid data received from sensor",
            "Communication protocol error",
            "Sensor malfunction detected",
            "Data validation failed",
            "Network connectivity issue",
            "ECU response timeout",
            "Unsupported diagnostic protocol"
        };
        return errors[random.nextInt(errors.length)];
    }
    
    private String generateMockInputData(String workerName) {
        return String.format("{\"worker\": \"%s\", \"timestamp\": \"%s\"}", 
                workerName, LocalDateTime.now());
    }
    
    private String generateMockOutputData(String workerName) {
        return String.format("{\"worker\": \"%s\", \"status\": \"success\", \"dataPoints\": %d}", 
                workerName, random.nextInt(100) + 50);
    }
}
