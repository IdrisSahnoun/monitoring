package com.vehicle.diagnostic.monitoring.controller;

import com.vehicle.diagnostic.monitoring.dto.SessionDTO;
import com.vehicle.diagnostic.monitoring.service.PdfReportService;
import com.vehicle.diagnostic.monitoring.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * REST Controller for PDF report generation
 */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200")
public class ReportController {
    
    private final SessionService sessionService;
    private final PdfReportService pdfReportService;
    
    /**
     * Generate and download PDF report for a session
     * GET /api/reports/session/{sessionId}/pdf
     */
    @GetMapping("/session/{sessionId}/pdf")
    public ResponseEntity<byte[]> generateSessionReport(@PathVariable String sessionId) {
        log.info("GET /reports/session/{}/pdf", sessionId);
        
        try {
            // Get session data
            SessionDTO session = sessionService.getSession(sessionId);
            
            // Generate PDF
            byte[] pdfData = pdfReportService.generateSessionReport(session);
            
            // Set headers for PDF download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                    "diagnostic_session_" + sessionId + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(pdfData, headers, HttpStatus.OK);
            
        } catch (IOException e) {
            log.error("Error generating PDF report for session: {}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Save PDF report to server file system
     * POST /api/reports/session/{sessionId}/save
     */
    @PostMapping("/session/{sessionId}/save")
    public ResponseEntity<String> saveSessionReport(@PathVariable String sessionId) {
        log.info("POST /reports/session/{}/save", sessionId);
        
        try {
            // Get session data
            SessionDTO session = sessionService.getSession(sessionId);
            
            // Generate PDF
            byte[] pdfData = pdfReportService.generateSessionReport(session);
            
            // Save to file system
            String filePath = pdfReportService.saveReportToFile(pdfData, sessionId);
            
            return ResponseEntity.ok(filePath);
            
        } catch (IOException e) {
            log.error("Error saving PDF report for session: {}", sessionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving report: " + e.getMessage());
        }
    }
}
