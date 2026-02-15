package com.vehicle.diagnostic.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for diagnostic session response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionDTO {
    
    private String sessionId;
    private String vehicleId;
    private String vehicleVin;
    private String diagnosticType;
    private String status;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;
    
    private Long durationMs;
    private String currentWorker;
    private Integer completedSteps;
    private Integer totalSteps;
    private Double progressPercentage;
    private String errorMessage;
    
    private List<WorkerEventDTO> workerEvents;
    private PerformanceMetrics performanceMetrics;
}
