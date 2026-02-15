package com.vehicle.diagnostic.monitoring.controller;

import com.vehicle.diagnostic.monitoring.dto.DashboardStatsDTO;
import com.vehicle.diagnostic.monitoring.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * REST Controller for dashboard and analytics operations
 */
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    /**
     * Get comprehensive dashboard statistics
     * GET /api/dashboard/stats?startDate=2026-02-11T00:00:00&endDate=2026-02-12T23:59:59
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime startDate,
            
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
            LocalDateTime endDate) {
        
        log.info("GET /dashboard/stats from {} to {}", startDate, endDate);
        
        try {
            // Default to last 24 hours if not specified
            if (startDate == null) {
                startDate = LocalDateTime.now().minusHours(24);
            }
            if (endDate == null) {
                endDate = LocalDateTime.now();
            }
            
            DashboardStatsDTO stats = dashboardService.getDashboardStats(startDate, endDate);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching dashboard stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get real-time statistics
     * GET /api/dashboard/realtime
     */
    @GetMapping("/realtime")
    public ResponseEntity<Map<String, Object>> getRealtimeStats() {
        log.info("GET /dashboard/realtime");
        
        try {
            Map<String, Object> stats = dashboardService.getRealtimeStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching realtime stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get worker-specific statistics
     * GET /api/dashboard/worker/{workerName}
     */
    @GetMapping("/worker/{workerName}")
    public ResponseEntity<DashboardStatsDTO.WorkerMetrics> getWorkerStats(
            @PathVariable String workerName) {
        
        log.info("GET /dashboard/worker/{}", workerName);
        
        try {
            DashboardStatsDTO.WorkerMetrics metrics = dashboardService.getWorkerStats(workerName);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error fetching worker stats for: {}", workerName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
