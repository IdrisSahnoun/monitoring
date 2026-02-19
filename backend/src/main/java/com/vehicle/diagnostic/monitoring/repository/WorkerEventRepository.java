package com.vehicle.diagnostic.monitoring.repository;

import com.vehicle.diagnostic.monitoring.model.WorkerEvent;
import com.vehicle.diagnostic.monitoring.model.WorkerEvent.WorkerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for WorkerEvent entity
 */
@Repository
public interface WorkerEventRepository extends JpaRepository<WorkerEvent, String> {
    
    List<WorkerEvent> findBySessionId(String sessionId);
    
    List<WorkerEvent> findBySessionIdOrderByStepNumberAsc(String sessionId);
    
    List<WorkerEvent> findByMessageType(WorkerEvent.MessageType messageType);
    
    List<WorkerEvent> findByMessageTypeAndStatus(WorkerEvent.MessageType messageType, WorkerStatus status);
    
    @Query("SELECT e FROM WorkerEvent e WHERE e.sessionId = :sessionId AND e.messageType = :messageType")
    List<WorkerEvent> findBySessionIdAndMessageType(
        @Param("sessionId") String sessionId,
        @Param("messageType") WorkerEvent.MessageType messageType
    );
    
    @Query("SELECT e FROM WorkerEvent e WHERE e.startTime BETWEEN :startDate AND :endDate")
    List<WorkerEvent> findEventsBetweenDates(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT COUNT(e) FROM WorkerEvent e WHERE e.messageType = :messageType AND e.status = :status")
    Long countByMessageTypeAndStatus(
        @Param("messageType") WorkerEvent.MessageType messageType,
        @Param("status") WorkerStatus status
    );
    
    @Query("SELECT AVG(e.executionTimeMs) FROM WorkerEvent e WHERE e.messageType = :messageType AND e.status = 'COMPLETED'")
    Double getAverageExecutionTimeByMessageType(@Param("messageType") WorkerEvent.MessageType messageType);
}
