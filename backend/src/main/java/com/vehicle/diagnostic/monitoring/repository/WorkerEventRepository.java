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
    
    List<WorkerEvent> findByWorkerName(String workerName);
    
    List<WorkerEvent> findByWorkerNameAndStatus(String workerName, WorkerStatus status);
    
    @Query("SELECT e FROM WorkerEvent e WHERE e.sessionId = :sessionId AND e.workerName = :workerName")
    List<WorkerEvent> findBySessionIdAndWorkerName(
        @Param("sessionId") String sessionId,
        @Param("workerName") String workerName
    );
    
    @Query("SELECT e FROM WorkerEvent e WHERE e.startTime BETWEEN :startDate AND :endDate")
    List<WorkerEvent> findEventsBetweenDates(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT COUNT(e) FROM WorkerEvent e WHERE e.workerName = :workerName AND e.status = :status")
    Long countByWorkerNameAndStatus(
        @Param("workerName") String workerName,
        @Param("status") WorkerStatus status
    );
    
    @Query("SELECT AVG(e.executionTimeMs) FROM WorkerEvent e WHERE e.workerName = :workerName AND e.status = 'COMPLETED'")
    Double getAverageExecutionTimeByWorker(@Param("workerName") String workerName);
}
