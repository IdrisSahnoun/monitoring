package com.vehicle.diagnostic.monitoring.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.vehicle.diagnostic.monitoring.dto.SessionDTO;
import com.vehicle.diagnostic.monitoring.dto.WorkerEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Service for generating PDF reports
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PdfReportService {
    
    @Value("${monitoring.pdf.output-dir:reports}")
    private String outputDir;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Generate PDF report for a diagnostic session
     * @param session The session data
     * @return PDF as byte array
     */
    public byte[] generateSessionReport(SessionDTO session) throws IOException {
        log.info("Generating PDF report for session: {}", session.getSessionId());
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);
        
        try {
            // Title
            document.add(new Paragraph("Vehicle Diagnostic Session Report")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));
            
            // Session Information
            addSessionInfo(document, session);
            
            // Worker Events Timeline
            addWorkerEventsTable(document, session);
            
            // Performance Metrics
            addPerformanceMetrics(document, session);
            
            // Footer
            document.add(new Paragraph("Report generated: " + LocalDateTime.now().format(DATE_FORMATTER))
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(20));
            
        } finally {
            document.close();
        }
        
        return baos.toByteArray();
    }
    
    /**
     * Save PDF report to file system
     */
    public String saveReportToFile(byte[] pdfData, String sessionId) throws IOException {
        String fileName = String.format("session_%s_%s.pdf", 
                sessionId, 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
        
        String filePath = outputDir + "/" + fileName;
        
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(pdfData);
        }
        
        log.info("PDF report saved to: {}", filePath);
        return filePath;
    }
    
    private void addSessionInfo(Document document, SessionDTO session) {
        document.add(new Paragraph("Session Information")
                .setFontSize(14)
                .setBold()
                .setMarginTop(10));
        
        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .setWidth(UnitValue.createPercentValue(100));
        
        addInfoRow(table, "Session ID", session.getSessionId());
        addInfoRow(table, "Vehicle ID", session.getVehicleId());
        addInfoRow(table, "Status", session.getStatus());
        addInfoRow(table, "Start Time", session.getStartTime() != null ? session.getStartTime().format(DATE_FORMATTER) : "N/A");
        addInfoRow(table, "End Time", session.getEndTime() != null ? session.getEndTime().format(DATE_FORMATTER) : "N/A");
        addInfoRow(table, "Duration", session.getDurationMs() != null ? session.getDurationMs() + " ms" : "N/A");
        addInfoRow(table, "Progress", session.getProgressPercentage() != null ? String.format("%.2f%%", session.getProgressPercentage()) : "N/A");
        
        if (session.getErrorMessage() != null && !session.getErrorMessage().isEmpty()) {
            addInfoRow(table, "Error", session.getErrorMessage());
        }
        
        document.add(table);
    }
    
    private void addWorkerEventsTable(Document document, SessionDTO session) {
        document.add(new Paragraph("Worker Execution Timeline")
                .setFontSize(14)
                .setBold()
                .setMarginTop(20));
        
        if (session.getWorkerEvents() == null || session.getWorkerEvents().isEmpty()) {
            document.add(new Paragraph("No worker events available.").setItalic());
            return;
        }
        
        Table table = new Table(UnitValue.createPercentArray(new float[]{5, 25, 15, 20, 20, 15}))
                .setWidth(UnitValue.createPercentValue(100));
        
        // Header
        addHeaderCell(table, "Step");
        addHeaderCell(table, "Worker");
        addHeaderCell(table, "Status");
        addHeaderCell(table, "Start Time");
        addHeaderCell(table, "End Time");
        addHeaderCell(table, "Duration (ms)");
        
        // Data rows
        for (WorkerEventDTO event : session.getWorkerEvents()) {
            table.addCell(new Cell().add(new Paragraph(String.valueOf(event.getStepNumber()))));
            table.addCell(new Cell().add(new Paragraph(event.getWorkerName())));
            table.addCell(new Cell().add(new Paragraph(event.getStatus())));
            table.addCell(new Cell().add(new Paragraph(event.getStartTime() != null ? event.getStartTime().format(DATE_FORMATTER) : "N/A")));
            table.addCell(new Cell().add(new Paragraph(event.getEndTime() != null ? event.getEndTime().format(DATE_FORMATTER) : "N/A")));
            table.addCell(new Cell().add(new Paragraph(event.getExecutionTimeMs() != null ? String.valueOf(event.getExecutionTimeMs()) : "N/A")));
        }
        
        document.add(table);
    }
    
    private void addPerformanceMetrics(Document document, SessionDTO session) {
        if (session.getPerformanceMetrics() == null) {
            return;
        }
        
        document.add(new Paragraph("Performance Metrics")
                .setFontSize(14)
                .setBold()
                .setMarginTop(20));
        
        Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));
        
        addInfoRow(table, "Total Executions", String.valueOf(session.getPerformanceMetrics().getTotalExecutions()));
        addInfoRow(table, "Successful Executions", String.valueOf(session.getPerformanceMetrics().getSuccessfulExecutions()));
        addInfoRow(table, "Failed Executions", String.valueOf(session.getPerformanceMetrics().getFailedExecutions()));
        addInfoRow(table, "Success Rate", String.format("%.2f%%", session.getPerformanceMetrics().getSuccessRate()));
        addInfoRow(table, "Average Execution Time", String.format("%.2f ms", session.getPerformanceMetrics().getAverageExecutionTime()));
        addInfoRow(table, "P50 Latency", String.format("%.2f ms", session.getPerformanceMetrics().getP50LatencyMs()));
        addInfoRow(table, "P95 Latency", String.format("%.2f ms", session.getPerformanceMetrics().getP95LatencyMs()));
        addInfoRow(table, "P99 Latency", String.format("%.2f ms", session.getPerformanceMetrics().getP99LatencyMs()));
        
        document.add(table);
    }
    
    private void addInfoRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold()));
        table.addCell(new Cell().add(new Paragraph(value != null ? value : "N/A")));
    }
    
    private void addHeaderCell(Table table, String text) {
        table.addHeaderCell(new Cell()
                .add(new Paragraph(text).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setTextAlignment(TextAlignment.CENTER));
    }
}
