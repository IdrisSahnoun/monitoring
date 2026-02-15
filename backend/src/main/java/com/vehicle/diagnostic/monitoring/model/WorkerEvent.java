package com.vehicle.diagnostic.monitoring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a worker execution event
 * Tracks individual worker executions within a diagnostic session
 */
@Entity
@Table(name = "worker_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String sessionId;
    
    @Column(nullable = false)
    private String workerName;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private WorkerStatus status;
    
    @Column(nullable = false)
    private LocalDateTime startTime;
    
    @Column
    private LocalDateTime endTime;
    
    @Column
    private Long executionTimeMs;
    
    @Column
    private Integer stepNumber;
    
    @Column(length = 2000)
    private String inputData;
    
    @Column(length = 2000)
    private String outputData;
    
    @Column(length = 2000)
    private String errorDetails;
    
    @Column
    private Integer retryCount;
    
    @Column
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum WorkerStatus {
        STARTED,
        RUNNING,
        COMPLETED,
        FAILED,
        SKIPPED,
        RETRYING
    }
}
