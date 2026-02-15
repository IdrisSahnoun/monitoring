package com.vehicle.diagnostic.monitoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for Vehicle Diagnostic Monitoring System
 * 
 * This application provides monitoring and supervision capabilities for
 * cloud-based vehicle diagnostic platform, integrating with Azure Application Insights
 * to track worker execution, performance metrics, and generate reports.
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class MonitoringApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitoringApplication.class, args);
    }
}
