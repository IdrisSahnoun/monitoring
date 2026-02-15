package com.vehicle.diagnostic.monitoring.util;

import com.vehicle.diagnostic.monitoring.model.DiagnosticSession;
import com.vehicle.diagnostic.monitoring.model.WorkerEvent;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for creating custom events to send to Application Insights
 */
public class CustomEventBuilder {
    
    /**
     * Build custom event properties for a diagnostic session
     */
    public static Map<String, String> buildSessionEventProperties(DiagnosticSession session) {
        Map<String, String> properties = new HashMap<>();
        
        properties.put("sessionId", session.getSessionId());
        properties.put("vehicleId", session.getVehicleId());
        properties.put("status", session.getStatus().name());
        properties.put("diagnosticType", session.getDiagnosticType());
        
        if (session.getCurrentWorker() != null) {
            properties.put("currentWorker", session.getCurrentWorker());
        }
        
        if (session.getErrorMessage() != null) {
            properties.put("errorMessage", session.getErrorMessage());
        }
        
        return properties;
    }
    
    /**
     * Build custom event properties for a worker execution
     */
    public static Map<String, String> buildWorkerEventProperties(WorkerEvent event) {
        Map<String, String> properties = new HashMap<>();
        
        properties.put("sessionId", event.getSessionId());
        properties.put("workerName", event.getWorkerName());
        properties.put("status", event.getStatus().name());
        properties.put("stepNumber", String.valueOf(event.getStepNumber()));
        
        if (event.getRetryCount() != null && event.getRetryCount() > 0) {
            properties.put("retryCount", String.valueOf(event.getRetryCount()));
        }
        
        if (event.getErrorDetails() != null) {
            properties.put("errorDetails", event.getErrorDetails());
        }
        
        return properties;
    }
    
    /**
     * Build custom event metrics for a worker execution
     */
    public static Map<String, Double> buildWorkerEventMetrics(WorkerEvent event) {
        Map<String, Double> metrics = new HashMap<>();
        
        if (event.getExecutionTimeMs() != null) {
            metrics.put("executionTimeMs", event.getExecutionTimeMs().doubleValue());
        }
        
        if (event.getRetryCount() != null) {
            metrics.put("retryCount", event.getRetryCount().doubleValue());
        }
        
        return metrics;
    }
    
    /**
     * Build custom event metrics for a session
     */
    public static Map<String, Double> buildSessionEventMetrics(DiagnosticSession session) {
        Map<String, Double> metrics = new HashMap<>();
        
        if (session.getDurationMs() != null) {
            metrics.put("durationMs", session.getDurationMs().doubleValue());
        }
        
        if (session.getCompletedSteps() != null) {
            metrics.put("completedSteps", session.getCompletedSteps().doubleValue());
        }
        
        if (session.getTotalSteps() != null) {
            metrics.put("totalSteps", session.getTotalSteps().doubleValue());
            
            if (session.getCompletedSteps() != null) {
                double progress = (session.getCompletedSteps() * 100.0) / session.getTotalSteps();
                metrics.put("progressPercentage", progress);
            }
        }
        
        return metrics;
    }
}
