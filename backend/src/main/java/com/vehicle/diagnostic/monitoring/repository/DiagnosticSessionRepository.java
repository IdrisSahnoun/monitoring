package com.vehicle.diagnostic.monitoring.repository;

import com.vehicle.diagnostic.monitoring.model.DiagnosticSession;
import com.vehicle.diagnostic.monitoring.model.DiagnosticSession.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for DiagnosticSession entity
 */
@Repository
public interface DiagnosticSessionRepository extends JpaRepository<DiagnosticSession, String> {
    
    Optional<DiagnosticSession> findBySessionId(String sessionId);
    
    List<DiagnosticSession> findByVehicleId(String vehicleId);
    
    List<DiagnosticSession> findByStatus(SessionStatus status);
    
    Page<DiagnosticSession> findByStatus(SessionStatus status, Pageable pageable);
    
    @Query("SELECT s FROM DiagnosticSession s WHERE s.startTime BETWEEN :startDate AND :endDate")
    List<DiagnosticSession> findSessionsBetweenDates(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT s FROM DiagnosticSession s WHERE " +
           "(:sessionId IS NULL OR s.sessionId = :sessionId) AND " +
           "(:vehicleId IS NULL OR s.vehicleId = :vehicleId) AND " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:startDate IS NULL OR s.startTime >= :startDate) AND " +
           "(:endDate IS NULL OR s.startTime <= :endDate)")
    Page<DiagnosticSession> findByFilters(
        @Param("sessionId") String sessionId,
        @Param("vehicleId") String vehicleId,
        @Param("status") SessionStatus status,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    @Query("SELECT COUNT(s) FROM DiagnosticSession s WHERE s.status = :status")
    Long countByStatus(@Param("status") SessionStatus status);
    
    @Query("SELECT AVG(s.durationMs) FROM DiagnosticSession s WHERE s.status = 'COMPLETED'")
    Double getAverageSessionDuration();
}
