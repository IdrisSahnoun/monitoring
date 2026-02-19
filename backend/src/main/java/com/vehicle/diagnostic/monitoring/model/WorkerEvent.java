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
    private String sessionId; // Maps to instanceId in DiagCloud
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageType messageType; // Type of Saga worker
    
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
    
    /**
     * DiagCloud worker/saga message types
     * Represents the different steps in the Starting and Shutdown sagas
     */
    public enum MessageType {
        // Starting Saga Workers
        BOOK_VCI_SERVER,
        CREATE_PRODUCT_INSTANCE,
        DETERMINE_PRODUCT_VERSION,
        SEARCH_LICENSE,
        CONFIGURE_SESSION,
        INITIALIZE_DIAGNOSTICS,
        START_COMMUNICATION,
        VALIDATE_CONNECTION,
        
        // Shutdown Saga Workers
        STOP_COMMUNICATION,
        CLEANUP_RESOURCES,
        RELEASE_VCI_SERVER,
        SAVE_SESSION_DATA,
        SEND_NOTIFICATION,
        
        // Common/Utility Workers
        HEALTH_CHECK,
        LOG_EVENT,
        UPDATE_STATUS
    }
    
    /**
     * Execution status of a worker
     */
    public enum WorkerStatus {
        STARTED,
        RUNNING,
        COMPLETED,
        FAILED,
        SKIPPED,
        RETRYING
    }
}
