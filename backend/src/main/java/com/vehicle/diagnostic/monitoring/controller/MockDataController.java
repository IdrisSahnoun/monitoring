package com.vehicle.diagnostic.monitoring.controller;

import com.vehicle.diagnostic.monitoring.service.MockDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for managing mock data generation
 * Use this to populate the database with test data while waiting for Azure access
 */
@RestController
@RequestMapping("/mock-data")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class MockDataController {
    
    private final MockDataService mockDataService;
    
    /**
     * Generate a specific number of mock sessions
     * POST /api/mock-data/generate?count=100
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateMockData(
            @RequestParam(defaultValue = "50") int count) {
        
        log.info("POST /mock-data/generate?count={}", count);
        
        if (count < 1 || count > 1000) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Count must be between 1 and 1000");
            return ResponseEntity.badRequest().body(error);
        }
        
        try {
            int created = mockDataService.generateMockData(count);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Mock data generated successfully");
            response.put("sessionsCreated", created);
            response.put("eventsCreated", created * 6); // Average 6 workers per session
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating mock data", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to generate mock data: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Generate comprehensive mock data for different time periods
     * POST /api/mock-data/generate-comprehensive
     */
    @PostMapping("/generate-comprehensive")
    public ResponseEntity<Map<String, Object>> generateComprehensiveMockData() {
        log.info("POST /mock-data/generate-comprehensive");
        
        try {
            Map<String, Integer> stats = mockDataService.generateTimeBasedMockData();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Comprehensive mock data generated successfully");
            response.put("last24h", stats.get("last24h"));
            response.put("last7d", stats.get("last7d"));
            response.put("last30d", stats.get("last30d"));
            response.put("totalSessions", stats.values().stream().mapToInt(Integer::intValue).sum());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating comprehensive mock data", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to generate comprehensive mock data: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Get statistics about current mock data
     * GET /api/mock-data/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getMockDataStats() {
        log.info("GET /mock-data/stats");
        
        try {
            Map<String, Object> stats = mockDataService.getMockDataStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting mock data stats", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get mock data stats: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Clear all mock data from database
     * DELETE /api/mock-data/clear
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearMockData() {
        log.info("DELETE /mock-data/clear");
        
        try {
            mockDataService.clearAllData();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "All mock data cleared successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error clearing mock data", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to clear mock data: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    
    /**
     * Quick setup - Generate default dataset for testing
     * POST /api/mock-data/quick-setup
     */
    @PostMapping("/quick-setup")
    public ResponseEntity<Map<String, Object>> quickSetup() {
        log.info("POST /mock-data/quick-setup");
        
        try {
            // Clear existing data
            mockDataService.clearAllData();
            
            // Generate 100 sessions for testing
            int created = mockDataService.generateMockData(100);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Quick setup completed successfully");
            response.put("sessionsCreated", created);
            response.put("note", "Database cleared and 100 test sessions created");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error in quick setup", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Quick setup failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
