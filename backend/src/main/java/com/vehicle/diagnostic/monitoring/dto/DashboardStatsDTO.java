package com.vehicle.diagnostic.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for dashboard statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    
    private Long totalSessions;
    private Long activeSessions;
    private Long completedSessions;
    private Long failedSessions;
    private Double overallSuccessRate;
    private Double averageSessionDuration;
    
    private Map<String, WorkerMetrics> workerMetrics;
    private Map<String, Long> sessionsByStatus;
    private Map<String, Long> sessionsByHour;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkerMetrics {
        private String workerName;
        private Long totalExecutions;
        private Long successfulExecutions;
        private Long failedExecutions;
        private Double successRate;
        private Double averageExecutionTime;
    }
}
