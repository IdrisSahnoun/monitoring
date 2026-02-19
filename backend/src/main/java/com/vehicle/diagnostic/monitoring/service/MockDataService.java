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
    
    // DiagCloud products
    private static final String[] PRODUCT_IDS = {
        "WDB1", "WDB2", "VCI_EXE1", "VCI_EXE2",
        "DBX_V7.2.3", "DBX_V7.3.1", "DBX_V8.0.0",
        "STAR_DIAG_V5", "STAR_DIAG_V6", "OEM_TOOL_V3"
    };
    
    // DiagCloud users
    private static final String[] USER_IDS = {
        "operator.oi@company.com", "tech.support@company.com",
        "admin@company.com", "mechanic1@workshop.com",
        "mechanic2@workshop.com", "diagnostician@dealer.com",
        "field.tech@service.com", "qa.tester@company.com"
    };
    
    // Operation types
    private static final String[] OPERATION_TYPES = {
        "starting", "shutdown"
    };
    
    // Starting Saga workers (in order)
    private static final WorkerEvent.MessageType[] STARTING_WORKERS = {
        WorkerEvent.MessageType.BOOK_VCI_SERVER,
        WorkerEvent.MessageType.CREATE_PRODUCT_INSTANCE,
        WorkerEvent.MessageType.DETERMINE_PRODUCT_VERSION,
        WorkerEvent.MessageType.SEARCH_LICENSE,
        WorkerEvent.MessageType.CONFIGURE_SESSION,
        WorkerEvent.MessageType.INITIALIZE_DIAGNOSTICS,
        WorkerEvent.MessageType.START_COMMUNICATION,
        WorkerEvent.MessageType.VALIDATE_CONNECTION
    };
    
    // Shutdown Saga workers (in order)
    private static final WorkerEvent.MessageType[] SHUTDOWN_WORKERS = {
        WorkerEvent.MessageType.STOP_COMMUNICATION,
        WorkerEvent.MessageType.SAVE_SESSION_DATA,
        WorkerEvent.MessageType.CLEANUP_RESOURCES,
        WorkerEvent.MessageType.RELEASE_VCI_SERVER,
        WorkerEvent.MessageType.SEND_NOTIFICATION
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
                if (session.getStatus() == DiagnosticSession.SessionStatus.CLOSED) {
                    completedCount++;
                } else if (session.getStatus() == DiagnosticSession.SessionStatus.ERROR) {
                    failedCount++;
                } else if (session.getStatus() == DiagnosticSession.SessionStatus.RUNNING) {
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
                        log.warn("  ⚠ Worker {} FAILED: {}", event.getMessageType(), event.getErrorDetails());
                    } else {
                        log.debug("  ✓ Worker {} completed in {}ms", event.getMessageType(), event.getExecutionTimeMs());
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
        stats.put("completedSessions", sessionRepository.countByStatus(DiagnosticSession.SessionStatus.CLOSED));
        stats.put("failedSessions", sessionRepository.countByStatus(DiagnosticSession.SessionStatus.ERROR));
        stats.put("activeSessions", sessionRepository.countByStatus(DiagnosticSession.SessionStatus.RUNNING));
        
        return stats;
    }
    
    // Private helper methods
    
    private DiagnosticSession createMockSession() {
        String sessionId = "INST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(); // instanceId in DiagCloud
        String operationId = OPERATION_TYPES[random.nextInt(OPERATION_TYPES.length)];
        String vehicleId = VEHICLE_IDS[random.nextInt(VEHICLE_IDS.length)];
        String vin = VINS[random.nextInt(VINS.length)];
        String productId = PRODUCT_IDS[random.nextInt(PRODUCT_IDS.length)];
        String userId = USER_IDS[random.nextInt(USER_IDS.length)];
        String diagnosticType = DIAGNOSTIC_TYPES[random.nextInt(DIAGNOSTIC_TYPES.length)];
        
        LocalDateTime startTime = generateRandomStartTime(24); // Within last 24 hours
        DiagnosticSession.SessionStatus status = generateRandomStatus();
        
        // Select workers based on operation type
        WorkerEvent.MessageType[] workers = operationId.equals("starting") ? 
                STARTING_WORKERS : SHUTDOWN_WORKERS;
        int totalSteps = workers.length;
        
        LocalDateTime endTime = null;
        Long durationMs = null;
        Integer completedSteps = 0;
        WorkerEvent.MessageType currentWorker = null;
        String errorMessage = null;
        
        if (status == DiagnosticSession.SessionStatus.CLOSED) {
            durationMs = 20000L + random.nextInt(180000); // 20s to 3min
            endTime = startTime.plusSeconds(durationMs / 1000);
            completedSteps = totalSteps;
        } else if (status == DiagnosticSession.SessionStatus.ERROR) {
            durationMs = 5000L + random.nextInt(60000); // 5s to 1min
            endTime = startTime.plusSeconds(durationMs / 1000);
            completedSteps = random.nextInt(totalSteps);
            errorMessage = generateRandomError(productId);
        } else if (status == DiagnosticSession.SessionStatus.RUNNING) {
            completedSteps = 1 + random.nextInt(totalSteps - 1); // At least 1 step completed
            currentWorker = workers[completedSteps]; // Next worker to execute
        } else if (status == DiagnosticSession.SessionStatus.STARTING || 
                   status == DiagnosticSession.SessionStatus.INITIALIZING) {
            completedSteps = 0;
            currentWorker = workers[0];
        }
        
        return DiagnosticSession.builder()
                .sessionId(sessionId)
                .operationId(operationId)
                .vehicleId(vehicleId)
                .vehicleVin(vin)
                .productId(productId)
                .userId(userId)
                .diagnosticType(diagnosticType)
                .status(status)
                .startTime(startTime)
                .endTime(endTime)
                .durationMs(durationMs)
                .currentWorker(currentWorker != null ? currentWorker.name() : null)
                .completedSteps(completedSteps)
                .totalSteps(totalSteps)
                .errorMessage(errorMessage)
                .build();
    }
    
    private List<WorkerEvent> createMockWorkerEvents(DiagnosticSession session) {
        List<WorkerEvent> events = new ArrayList<>();
        
        // Select workers based on operation type
        WorkerEvent.MessageType[] workers = session.getOperationId().equals("starting") ? 
                STARTING_WORKERS : SHUTDOWN_WORKERS;
        
        LocalDateTime eventTime = session.getStartTime();
        int stepsToCreate = session.getCompletedSteps() != null ? session.getCompletedSteps() : 0;
        
        if (session.getStatus() == DiagnosticSession.SessionStatus.CLOSED) {
            stepsToCreate = workers.length;
        }
        
        for (int i = 0; i < stepsToCreate; i++) {
            WorkerEvent.MessageType messageType = workers[i];
            Long executionTime = 2000L + random.nextInt(15000); // 2s to 17s
            
            WorkerEvent.WorkerStatus workerStatus = WorkerEvent.WorkerStatus.COMPLETED;
            String errorDetails = null;
            Integer retryCount = 0;
            
            // Last worker might have failed if session failed
            if (i == stepsToCreate - 1 && session.getStatus() == DiagnosticSession.SessionStatus.ERROR) {
                workerStatus = WorkerEvent.WorkerStatus.FAILED;
                errorDetails = session.getErrorMessage();
                retryCount = random.nextInt(3);
            }
            
            WorkerEvent event = WorkerEvent.builder()
                    .sessionId(session.getSessionId())
                    .messageType(messageType)
                    .status(workerStatus)
                    .startTime(eventTime)
                    .endTime(eventTime.plusSeconds(executionTime / 1000))
                    .executionTimeMs(executionTime)
                    .stepNumber(i + 1)
                    .inputData(generateMockInputData(messageType))
                    .outputData(workerStatus == WorkerEvent.WorkerStatus.COMPLETED ? 
                            generateMockOutputData(messageType) : null)
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
        if (rand < 60) return DiagnosticSession.SessionStatus.CLOSED; // 60% successful
        if (rand < 75) return DiagnosticSession.SessionStatus.ERROR; // 15% failed
        if (rand < 85) return DiagnosticSession.SessionStatus.RUNNING; // 10% running
        if (rand < 92) return DiagnosticSession.SessionStatus.STARTING; // 7% starting
        if (rand < 97) return DiagnosticSession.SessionStatus.INITIALIZING; // 5% initializing
        if (rand < 99) return DiagnosticSession.SessionStatus.CANCELLED; // 2% cancelled
        return DiagnosticSession.SessionStatus.SHUTDOWN_REQUESTED; // 1% shutdown requested
    }
    
    private String generateRandomError(String productId) {
        String[] errors = {
            "VCI Server booking failed - no available servers",
            "Product instance creation timeout",
            "License not found for product: " + productId,
            "WRS 404 - Product version not available",
            "Communication initialization failed with vehicle ECU",
            "Invalid product configuration for: " + productId,
            "Database connection timeout during session setup",
            "Kafka message delivery failed",
            "Session validation failed - missing required parameters",
            "VCI connection timeout - check network connectivity"
        };
        return errors[random.nextInt(errors.length)];
    }
    
    private String generateMockInputData(WorkerEvent.MessageType messageType) {
        return String.format("{\"messageType\": \"%s\", \"timestamp\": \"%s\"}", 
                messageType.name(), LocalDateTime.now());
    }
    
    private String generateMockOutputData(WorkerEvent.MessageType messageType) {
        return String.format("{\"messageType\": \"%s\", \"status\": \"success\", \"executionId\": \"%s\"}", 
                messageType.name(), UUID.randomUUID().toString().substring(0, 8));
    }
}
