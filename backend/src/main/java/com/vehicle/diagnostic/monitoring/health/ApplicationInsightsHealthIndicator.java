package com.vehicle.diagnostic.monitoring.health;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for Azure Application Insights connectivity
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ApplicationInsightsHealthIndicator implements HealthIndicator {
    
    private final TelemetryClient telemetryClient;
    
    @Override
    public Health health() {
        try {
            // Check if Application Insights is configured
            if (telemetryClient != null && telemetryClient.getContext() != null) {
                String instrumentationKey = telemetryClient.getContext().getInstrumentationKey();
                boolean isConfigured = instrumentationKey != null && !instrumentationKey.isEmpty();
                
                return Health.up()
                        .withDetail("status", isConfigured ? "Connected" : "Not Configured")
                        .withDetail("instrumentationKeySet", isConfigured)
                        .build();
            } else {
                return Health.down()
                        .withDetail("status", "Not configured")
                        .withDetail("message", "TelemetryClient is not properly initialized")
                        .build();
            }
        } catch (Exception e) {
            log.error("Error checking Application Insights health", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
