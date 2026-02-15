package com.vehicle.diagnostic.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for worker event information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerEventDTO {
    
    private String eventId;
    private String sessionId;
    private String workerName;
    private String status;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;
    
    private Long executionTimeMs;
    private Integer stepNumber;
    private String inputData;
    private String outputData;
    private String errorDetails;
    private Integer retryCount;
}
