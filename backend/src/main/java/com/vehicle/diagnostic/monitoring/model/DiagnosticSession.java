package com.vehicle.diagnostic.monitoring.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a diagnostic session
 * Stores metadata about each diagnostic session for local querying
 */
@Entity
@Table(name = "diagnostic_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false, unique = true)
    private String sessionId; // Called "instanceId" in DiagCloud
    
    @Column(nullable = false)
    private String operationId; // starting or shutdown
    
    @Column(nullable = false)
    private String vehicleId;
    
    @Column
    private String vehicleVin;
    
    @Column
    private String productId; // Product image (e.g., WDB1, VCI_EXE1, DBX_V7.2.3)
    
    @Column
    private String userId; // User who initiated the session
    
    @Column
    private String diagnosticType;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SessionStatus status;
    
    @Column(nullable = false)
    private LocalDateTime startTime;
    
    @Column
    private LocalDateTime endTime;
    
    @Column
    private Long durationMs;
    
    @Column
    private String currentWorker;
    
    @Column
    private Integer completedSteps;
    
    @Column
    private Integer totalSteps;
    
    @Column(length = 2000)
    private String errorMessage;
    
    @Column
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * DiagCloud session statuses
     * Maps to the lifecycle of a diagnostic session in the Saga orchestrator
     */
    public enum SessionStatus {
        INITIALIZING,      // Session is being initialized
        STARTING,          // Starting saga workers
        RUNNING,           // Session in progress
        CLOSED,            // Successfully completed
        CANCELLED,         // Cancelled by user
        ERROR,             // Failed with errors
        SHUTDOWN_REQUESTED // Shutdown has been requested
    }
}
