# Vehicle Diagnostic Monitoring System - Backend

> **✨ DiagCloud Integration**: This system is now configured for DiagCloud specifications with proper session statuses, worker message types, operation types, and product IDs. See [DIAGCLOUD_UPDATES.md](DIAGCLOUD_UPDATES.md) for complete changes.

## Overview

This Spring Boot application provides comprehensive monitoring and supervision capabilities for DiagCloud's cloud-based vehicle diagnostic platform. It integrates with Azure Application Insights to track Saga worker execution, collect performance metrics, and generate detailed reports.

## Features

### Core Functionality
- 🔍 **Session Tracking**: Monitor diagnostic sessions in real-time
- 📊 **Worker Monitoring**: Track individual worker executions with detailed metrics
- 📈 **Performance Analytics**: Calculate and visualize performance metrics (latency, success rates, etc.)
- 💾 **Redis Caching**: Reduce API calls and improve response times
- 📄 **PDF Reports**: Generate comprehensive PDF reports for support teams
- 🏥 **Health Checks**: Monitor system health and Azure connectivity

### Technical Features
- Integration with Azure Application Insights via KQL queries
- RESTful API for Angular frontend
- Caching strategy with Redis
- Database persistence (H2 for dev, PostgreSQL for prod)
- Custom event tracking for workers
- Actuator endpoints for monitoring

## Architecture

```
├── config/              # Configuration classes (Redis, CORS, Azure)
├── controller/          # REST API controllers
├── dto/                 # Data Transfer Objects
├── exception/           # Exception handling
├── health/              # Custom health indicators
├── model/               # JPA entities
├── repository/          # Data access layer
├── service/             # Business logic
│   ├── ApplicationInsightsService  # Azure integration
│   ├── SessionService              # Session management
│   ├── DashboardService            # Analytics
│   └── PdfReportService            # Report generation
└── util/                # Utility classes
```

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Redis (local or cloud instance)
- Azure Application Insights account (for production)
- PostgreSQL (for production)

## Getting Started

### 1. Configure Azure Application Insights

Edit `src/main/resources/application.yml` and add your Azure credentials:

```yaml
azure:
  application-insights:
    instrumentation-key: YOUR_INSTRUMENTATION_KEY
    connection-string: YOUR_CONNECTION_STRING
    workspace-id: YOUR_WORKSPACE_ID
```

Or set environment variables:
```bash
export APPINSIGHTS_INSTRUMENTATION_KEY=your-key
export APPINSIGHTS_CONNECTION_STRING=your-connection-string
export APPINSIGHTS_WORKSPACE_ID=your-workspace-id
```

### 2. Install Redis

**Windows (using Chocolatey):**
```powershell
choco install redis-64
redis-server
```

**Or use Docker:**
```bash
docker run -d -p 6379:6379 redis:latest
```

### 3. Build the Project

```bash
mvn clean install
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api`

### 5. Generate Mock Data (For Testing Without Azure)

If you don't have Azure access yet, generate mock data to test all features:

```powershell
# Quick setup - clears DB and creates 100 test sessions
curl -X POST http://localhost:8080/api/mock-data/quick-setup

# Or generate comprehensive dataset (24h, 7d, 30d)
curl -X POST http://localhost:8080/api/mock-data/generate-comprehensive

# Check what was created
curl http://localhost:8080/api/mock-data/stats
```

See [TESTING_GUIDE.md](TESTING_GUIDE.md) for detailed testing instructions.

## API Endpoints

### Sessions
- `GET /api/sessions/{sessionId}` - Get session details
- `POST /api/sessions/search` - Search sessions with filters
- `GET /api/sessions/{sessionId}/events` - Get worker events
- `GET /api/sessions/{sessionId}/metrics` - Get performance metrics
- `GET /api/sessions/vehicle/{vehicleId}` - Get sessions by vehicle

### Dashboard
- `GET /api/dashboard/stats` - Get dashboard statistics
- `GET /api/dashboard/realtime` - Get real-time stats
- `GET /api/dashboard/worker/{messageType}` - Get worker-specific stats (e.g., BOOK_VCI_SERVER)

### Reports
- `GET /api/reports/session/{sessionId}/pdf` - Download PDF report
- `POST /api/reports/session/{sessionId}/save` - Save PDF to server

### Health Checks
- `GET /api/actuator/health` - Overall health status
- `GET /api/actuator/metrics` - Application metrics
- `GET /api/actuator/prometheus` - Prometheus metrics

### Mock Data (Testing Without Azure)
- `POST /api/mock-data/quick-setup` - Clear DB and create 100 test sessions
- `POST /api/mock-data/generate?count=N` - Generate N mock sessions
- `POST /api/mock-data/generate-comprehensive` - Generate data for 24h, 7d, 30d
- `GET /api/mock-data/stats` - Get mock data statistics
- `DELETE /api/mock-data/clear` - Clear all mock data

## Configuration

### Database Configuration

**Development (H2):**
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:monitoring_db
    driver-class-name: org.h2.Driver
```

**Production (PostgreSQL):**
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/monitoring_db
    username: your_username
    password: your_password
    driver-class-name: org.postgresql.Driver
```

### Redis Configuration

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 60000
```

### Cache Configuration

Different caches with custom TTL:
- `sessions` - 10 minutes
- `workerEvents` - 10 minutes
- `performanceMetrics` - 5 minutes
- `dashboardStats` - 5 minutes
- `realtimeStats` - 1 minute
- `workerStats` - 15 minutes

## Azure Application Insights Integration

### Custom Events

The system tracks custom events for each worker:

```java
Map<String, String> properties = new HashMap<>();
properties.put("sessionId", sessionId);
properties.put("messageType", messageType.name()); // DiagCloud worker type
properties.put("status", status);

Map<String, Double> metrics = new HashMap<>();
metrics.put("executionTimeMs", executionTime);

telemetryClient.trackEvent("WorkerExecution", properties, metrics);
```

### KQL Queries (To be implemented)

Example queries for Application Insights:

```kql
// Get all events for a session
customEvents
| where customDimensions.sessionId == '{sessionId}'
| order by timestamp asc

// Calculate worker performance metrics
customEvents
| where name == "WorkerExecution"
| summarize 
    avg(todouble(customDimensions.executionTimeMs)),
    percentile(todouble(customDimensions.executionTimeMs), 50),
    percentile(todouble(customDimensions.executionTimeMs), 95)
by tostring(customDimensions.messageType)
```

## Workers Configuration

Configured workers in the system:
1. **InitializationWorker** - Initialize diagnostic session
2. **DataCollectionWorker** - Collect vehicle data
3. **AnalysisWorker** - Analyze diagnostic data
4. **ValidationWorker** - Validate results
5. **ReportGenerationWorker** - Generate diagnostic report
6. **NotificationWorker** - Send notifications

## Performance Optimization

### Caching Strategy
- Redis-based caching reduces API calls to Application Insights
- Configurable TTL per cache type
- Automatic cache eviction

### Database Optimization
- Indexed queries for fast lookups
- Pagination support for large datasets
- Query optimization with JPA

## PDF Report Generation

Reports include:
- Session information
- Worker execution timeline
- Performance metrics (P50, P95, P99 latency)
- Error details

## Monitoring and Observability

### Health Checks
- Database connectivity
- Redis connectivity
- Azure Application Insights connectivity
- Worker monitoring system status

### Metrics - Testing with Mock Data
The project is fully functional with mock data generation for local testing:

1. ✅ Backend structure complete
2. ✅ Mock data generation available
3. ✅ All APIs testable locally
4. ✅ Dashboard, reports, and analytics working
5. ⏳ Azure Application Insights integration pending

**Get Started:**
1. Run the application
2. Generate mock data: `curl -X POST http://localhost:8080/api/mock-data/quick-setup`
3. Test all endpoints (see [TESTING_GUIDE.md](TESTING_GUIDE.md))
4. Develop and test frontend with mock data
5. Later: Add Azure credentials and implement KQL queries

### When Azure Access is Available
✅ Mock data system ready
3. ✅ All APIs testable locally
4. ⏳ Test all features with mock data
5. ⏳ Develop Angular frontend
6. ⏳ Access Azure Application Insights
7. ⏳ Implement KQL queries
8. ⏳ Integration testing with real data
9## Current Status
The project structure is complete and ready for Azure integration. Once you have access to Azure Application Insights logs:

1. Update Azure credentials in `application.yml`
2. Implement KQL queries in `ApplicationInsightsService`
3. Test with real diagnostic session data
4. Integrate with Angular frontend

### Next Steps
1. ✅ Backend structure created
2. ⏳ Access Azure Application Insights
3. ⏳ Implement KQL queries
4. ⏳ Test with real data
5. ⏳ Develop Angular frontend
6. ⏳ Integration testing
7. ⏳ Production deployment

## Testing

### Unit Tests
Run unit tests with:
```bash
mvn test
```

### Testing with Mock Data
See [TESTING_GUIDE.md](TESTING_GUIDE.md) for comprehensive testing guide including:
- Generating realistic mock data
- Testing all API endpoints
- Dashboard and analytics testing
- PDF report generation
- Performance and caching validation
- Database inspection with H2 console

## Build for Production

```bash
mvn clean package
java -jar target/monitoring-backend-1.0.0.jar
```

## Docker Support (Optional)

Create a `Dockerfile`:
```dockerfile
FROM openjdk:17-slim
COPY target/monitoring-backend-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build and run:
```bash
docker build -t monitoring-backend .
docker run -p 8080:8080 monitoring-backend
```

## Troubleshooting

### Redis Connection Issues
- Ensure Redis is running: `redis-cli ping` should return `PONG`
- Check Redis host and port in configuration

### Azure Connection Issues
- Verify instrumentation key and connection string
- Check network connectivity to Azure
- Review logs in `logs/monitoring-application.log`

### Database Issues
- H2 console available at: `http://localhost:8080/api/h2-console`
- Check database URL and credentials

## Contributing

When Azure access is available, update:
1. `ApplicationInsightsService.java` - Implement KQL queries
2. Add integration tests with real Azure data
3. Update this README with query examples

## License

Proprietary - Vehicle Diagnostic Monitoring System

## Support

For issues or questions, contact the development team.
