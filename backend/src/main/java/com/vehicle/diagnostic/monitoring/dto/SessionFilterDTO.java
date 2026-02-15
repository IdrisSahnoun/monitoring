package com.vehicle.diagnostic.monitoring.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for filtering and querying sessions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionFilterDTO {
    
    private String sessionId;
    private String vehicleId;
    private String status;
    private String workerName;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;
    
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}
