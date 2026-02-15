package com.vehicle.diagnostic.monitoring.config;

import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Azure Application Insights
 * 
 * Note: The Application Insights Java agent handles configuration automatically
 * when using Spring Boot 3.x. The connection string should be set in application.yml
 * or as an environment variable APPLICATIONINSIGHTS_CONNECTION_STRING.
 */
@Configuration
public class ApplicationInsightsConfig {
    
    /**
     * Create TelemetryClient bean for tracking custom events
     * The TelemetryClient will be automatically configured by the Application Insights
     * Spring Boot starter based on the connection string in application.yml
     */
    @Bean
    public TelemetryClient telemetryClient() {
        TelemetryClient telemetryClient = new TelemetryClient();
        
        // Set common properties
        telemetryClient.getContext().getComponent().setVersion("1.0.0");
        telemetryClient.getContext().getProperties().put("Application", "Vehicle Diagnostic Monitoring");
        
        return telemetryClient;
    }
}
