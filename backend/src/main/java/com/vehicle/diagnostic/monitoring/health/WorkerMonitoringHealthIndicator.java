package com.vehicle.diagnostic.monitoring.health;

import com.vehicle.diagnostic.monitoring.repository.DiagnosticSessionRepository;
import com.vehicle.diagnostic.monitoring.repository.WorkerEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for worker monitoring system
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WorkerMonitoringHealthIndicator implements HealthIndicator {
    
    private final DiagnosticSessionRepository sessionRepository;
    private final WorkerEventRepository workerEventRepository;
    
    @Override
    public Health health() {
        try {
            // Check database connectivity by counting sessions
            long sessionCount = sessionRepository.count();
            long eventCount = workerEventRepository.count();
            
            // Check for active sessions
            long activeSessions = sessionRepository.countByStatus(
                    com.vehicle.diagnostic.monitoring.model.DiagnosticSession.SessionStatus.IN_PROGRESS);
            
            return Health.up()
                    .withDetail("status", "Operational")
                    .withDetail("totalSessions", sessionCount)
                    .withDetail("totalEvents", eventCount)
                    .withDetail("activeSessions", activeSessions)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error checking worker monitoring health", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
