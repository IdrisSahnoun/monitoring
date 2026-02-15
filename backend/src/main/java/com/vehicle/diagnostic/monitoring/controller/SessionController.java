package com.vehicle.diagnostic.monitoring.controller;

import com.vehicle.diagnostic.monitoring.dto.SessionDTO;
import com.vehicle.diagnostic.monitoring.dto.SessionFilterDTO;
import com.vehicle.diagnostic.monitoring.dto.WorkerEventDTO;
import com.vehicle.diagnostic.monitoring.dto.PerformanceMetrics;
import com.vehicle.diagnostic.monitoring.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for diagnostic session operations
 */
@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class SessionController {
    
    private final SessionService sessionService;
    
    /**
     * Get session by ID
     * GET /api/sessions/{sessionId}
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionDTO> getSession(@PathVariable String sessionId) {
        log.info("GET /sessions/{}", sessionId);
        
        try {
            SessionDTO session = sessionService.getSession(sessionId);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            log.error("Error fetching session: {}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get all sessions with filtering and pagination
     * GET /api/sessions?status=COMPLETED&page=0&size=20
     */
    @PostMapping("/search")
    public ResponseEntity<Page<SessionDTO>> searchSessions(@RequestBody SessionFilterDTO filter) {
        log.info("POST /sessions/search with filter: {}", filter);
        
        try {
            Page<SessionDTO> sessions = sessionService.getSessions(filter);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("Error searching sessions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get worker events for a session
     * GET /api/sessions/{sessionId}/events
     */
    @GetMapping("/{sessionId}/events")
    public ResponseEntity<List<WorkerEventDTO>> getWorkerEvents(@PathVariable String sessionId) {
        log.info("GET /sessions/{}/events", sessionId);
        
        try {
            List<WorkerEventDTO> events = sessionService.getWorkerEvents(sessionId);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Error fetching worker events for session: {}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get performance metrics for a session
     * GET /api/sessions/{sessionId}/metrics
     */
    @GetMapping("/{sessionId}/metrics")
    public ResponseEntity<PerformanceMetrics> getSessionMetrics(@PathVariable String sessionId) {
        log.info("GET /sessions/{}/metrics", sessionId);
        
        try {
            PerformanceMetrics metrics = sessionService.getSessionMetrics(sessionId);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error fetching metrics for session: {}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get sessions by vehicle ID
     * GET /api/sessions/vehicle/{vehicleId}
     */
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<Page<SessionDTO>> getSessionsByVehicle(
            @PathVariable String vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("GET /sessions/vehicle/{}", vehicleId);
        
        try {
            SessionFilterDTO filter = SessionFilterDTO.builder()
                    .vehicleId(vehicleId)
                    .page(page)
                    .size(size)
                    .build();
            
            Page<SessionDTO> sessions = sessionService.getSessions(filter);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("Error fetching sessions for vehicle: {}", vehicleId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
