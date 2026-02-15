package com.vehicle.diagnostic.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for performance metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceMetrics {
    
    private Double averageExecutionTime;
    private Long minExecutionTime;
    private Long maxExecutionTime;
    private Double successRate;
    private Double failureRate;
    private Integer totalExecutions;
    private Integer successfulExecutions;
    private Integer failedExecutions;
    private Double p50LatencyMs;
    private Double p95LatencyMs;
    private Double p99LatencyMs;
}
