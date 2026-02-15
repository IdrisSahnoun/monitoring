# Project Structure - Complete Overview

## 📁 Directory Structure

```
monitoring/
└── backend/
    ├── src/
    │   ├── main/
    │   │   ├── java/com/vehicle/diagnostic/monitoring/
    │   │   │   ├── MonitoringApplication.java          # Main Spring Boot application
    │   │   │   │
    │   │   │   ├── config/                             # Configuration Classes
    │   │   │   │   ├── ApplicationInsightsConfig.java  # Azure AI configuration
    │   │   │   │   ├── CorsConfig.java                 # CORS for Angular frontend
    │   │   │   │   └── RedisConfig.java                # Redis & caching setup
    │   │   │   │
    │   │   │   ├── controller/                         # REST API Controllers
    │   │   │   │   ├── DashboardController.java        # Dashboard & analytics endpoints
    │   │   │   │   ├── MockDataController.java         # Mock data generation (testing)
    │   │   │   │   ├── ReportController.java           # PDF report generation
    │   │   │   │   └── SessionController.java          # Session management API
    │   │   │   │
    │   │   │   ├── dto/                                # Data Transfer Objects
    │   │   │   │   ├── DashboardStatsDTO.java         # Dashboard statistics
    │   │   │   │   ├── PerformanceMetrics.java        # Performance metrics
    │   │   │   │   ├── SessionDTO.java                # Session response
    │   │   │   │   ├── SessionFilterDTO.java          # Query filters
    │   │   │   │   └── WorkerEventDTO.java            # Worker event data
    │   │   │   │
    │   │   │   ├── exception/                          # Exception Handling
    │   │   │   │   ├── GlobalExceptionHandler.java    # Global error handler
    │   │   │   │   └── ResourceNotFoundException.java # 404 exception
    │   │   │   │
    │   │   │   ├── health/                             # Health Indicators
    │   │   │   │   ├── ApplicationInsightsHealthIndicator.java  # AI health
    │   │   │   │   └── WorkerMonitoringHealthIndicator.java     # System health
    │   │   │   │
    │   │   │   ├── model/                              # JPA Entities
    │   │   │   │   ├── DiagnosticSession.java         # Session entity
    │   │   │   │   └── WorkerEvent.java               # Worker event entity
    │   │   │   │
    │   │   │   ├── repository/                         # Data Access Layer
    │   │   │   │   ├── DiagnosticSessionRepository.java  # Session queries
    │   │   │   │   └── WorkerEventRepository.java        # Event queries
    │   │   │   │
    │   │   │   ├── service/                            # Business Logic
    │   │   │   │   ├── ApplicationInsightsService.java # Azure KQL queries
    │   │   │   │   ├── DashboardService.java           # Analytics & stats
    │   │   │   │   ├── MockDataService.java            # Mock data generation
    │   │   │   │   ├── PdfReportService.java           # PDF generation
    │   │   │   │   └── SessionService.java             # Session management
    │   │   │   │
    │   │   │   └── util/                               # Utilities
    │   │   │       └── CustomEventBuilder.java         # Event builder helper
    │   │   │
    │   │   └── resources/
    │   │       └── application.yml                     # Main configuration
    │   │
    │   └── test/
    │       ├── java/com/vehicle/diagnostic/monitoring/
    │       │   └── service/
    │       │       └── SessionServiceTest.java         # Unit tests
    │       └── resources/
    │           └── application-test.yml                # Test configuration
    │
    ├── pom.xml                                         # Maven dependencies
    ├── README.md                                       # Full documentation
    ├── QUICKSTART.md                                   # Quick start guide
    ├── KQL_QUERIES.md                                  # Azure KQL examples
    └── .gitignore                                      # Git ignore rules
```

## 📊 Total Files Created: 34

### Configuration Files (4)
- ✅ pom.xml - Maven dependencies
- ✅ application.yml - Main configuration
- ✅ application-test.yml - Test configuration
- ✅ .gitignore - Git ignore rules

### Java Classes (24)
- ✅ 1 Main Application
- ✅ 3 Configuration classes
- ✅ 4 Controllers (including MockDataController)
- ✅ 5 DTOs
- ✅ 2 Entities
- ✅ 2 Repositories
- ✅ 5 Services (including MockDataService)
- ✅ 2 Health Indicators
- ✅ 2 Exception handlers
- ✅ 1 Utility class
- ✅ 1 Test class

### Documentation Files (6)
- ✅ README.md - Complete documentation
- ✅ QUICKSTART.md - Getting started guide
- ✅ TESTING_GUIDE.md - Testing with mock data
- ✅ KQL_QUERIES.md - Azure query examples
- ✅ PROJECT_STRUCTURE.md - This file

## 🎯 Key Features Implemented

### ✅ Backend Infrastructure
- Spring Boot 3.2.2 with Java 17
- JPA/Hibernate for database
- H2 (dev) / PostgreSQL (prod) support
- RESTful API architecture
- Exception handling
- Logging configuration

### ✅ Azure Integration (Ready)
- Application Insights SDK integrated
- TelemetryClient configured
- Custom event tracking structure
- KQL query templates prepared
- Health check for Azure connectivity

### ✅ Caching System
- Redis integration
- Multiple cache regions with different TTLs
- Cache configuration per entity type
- RedisTemplate for manual operations

### ✅ Monitoring & Observability
- Spring Boot Actuator
- Custom health indicators
- Prometheus metrics
- Application logging
- Database health checks
- Redis health checks

### ✅ Data Models
- DiagnosticSession entity
- WorkerEvent entity
- Complete DTO structure
- Relationships and indexes

### ✅ Services
- Session management
- Worker event tracking
- Dashboard analytics
- Performance metrics calculation
- PDF report generation

### ✅ REST API Endpoints
```
GET    /api/sessions/{sessionId}              - Get session details
POST   /api/sessions/search                   - Search sessions
GET    /api/sessions/{sessionId}/events       - Get worker events
GET    /api/sessions/{sessionId}/metrics      - Get metrics
GET    /api/sessions/vehicle/{vehicleId}      - Get sessions by vehicle

GET    /api/dashboard/stats                   - Dashboard statistics
GET    /api/dashboard/realtime                - Real-time stats
GET    /api/dashboard/worker/{workerName}     - Worker stats

GET    /api/reports/session/{sessionId}/pdf   - Download PDF report
POST   /api/reports/session/{sessionId}/save  - Save PDF to server

GET    /api/actuator/health                   - Health check
GET    /api/actuator/metrics                  - Metrics
GET    /api/actuator/prometheus               - Prometheus metrics

POST   /api/mock-data/quick-setup             - Generate test data (NEW)
POST   /api/mock-data/generate?count=N        - Generate N sessions (NEW)
POST   /api/mock-data/generate-comprehensive  - Generate 24h/7d/30d data (NEW)
GET    /api/mock-data/stats                   - Mock data statistics (NEW)
DELETE /api/mock-data/clear                   - Clear all mock data (NEW)
```

## 🔄 Workflow & Integration Points

### Current State (Pre-Azure Access - With Mock Data)
```
┌─────────────────┐
│  Spring Boot    │
│   Application   │◄── Mock Data Generator
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
┌───▼───┐ ┌──▼───┐
│ H2 DB │ │ Redis│
│ (Mock)│ │Cache │
└───────┘ └──────┘
```

**You can now test everything locally!**

### Future State (With Azure)
```
┌─────────────────┐      ┌──────────────────┐
│     Angular     │◄────►│   Spring Boot    │
│    Frontend     │      │    Backend       │
└─────────────────┘      └────────┬─────────┘
                                  │
                    ┌─────────────┼─────────────┐
                    │             │             │
              ┌─────▼────┐  ┌─────▼───┐  ┌─────▼────────┐
              │   Azure  │  │  Redis  │  │  PostgreSQL  │
              │ App In.  │  │  Cache  │  │   Database   │
              └──────────┘  └─────────┘  └──────────────┘
```
Testing with Mock Data (Current)
1. ✅ Project structure created
2. ✅ Mock data generation implemented
3. ⏳ Run and test locally
4. ⏳ Generate and explore mock data
5. ⏳ Test all API endpoints
6. ⏳ Verify dashboard calculations
7. ⏳ Test PDFFrontend Development (Can Start Now!)
1. ⏳ Create Angular application
2. ⏳ Connect to mock data APIs
3. ⏳ Implement dashboard UI
4. ⏳ Add charts and visualizations
5. ⏳ Test with realistic mock data
6. ⏳ Implement PDF download
7. ⏳ End-to-end testing with mock backend

### Phase 3:  report generation
8. ⏳ Validment KQL queries in `ApplicationInsightsService`
3. ⏳ Test with real diagnostic data
4. ⏳ Validate custom events
5. ⏳ Configure production settings

### Phase 3: Frontend Development
1. ⏳ Create Angular application
2. ⏳ Implement dashboard UI
3. ⏳ Add charts and visualizations
4. ⏳ Integrate with backend APIs
5. ⏳ Implement PDF download

### Phase 4: Production Deployment
1. ⏳ Configure PostgreSQL
2. ⏳ Set up Azure Redis Cache
3. ⏳ Deploy to Azure App Service
4. ⏳ Configure CI/CD pipeline
5. ⏳ Performance test with Mock Data
- [ ] Maven build succeeds
- [ ] Application starts without errors
- [ ] H2 console accessible
- [ ] Redis connection successful
- [ ] Health endpoint returns UP
- [ ] Generate mock data successfully
- [ ] Query sessions API works
- [ ] Dashboard statistics calculated correctly
- [ ] PDF reports generate
- [ ] Caching improves response times
- [ ] All worker metrics display proper
- [ ] Application starts without errors
- [ ] H2 console accessible
- [ ] Redis connection successful
- [ ] Health endpoint returns UP
- [ ] API endpoints respond correctly

### Azure Integration (When Ready)
- [ ] Application Insights connected
- [ ] Custom events tracked
- [ ] KQL queries return data
- [ ] Caching reduces API calls
- [ ] Performance metrics calculated

### Full System
- [ ] Frontend connects to backend
- [ ] Dashboard displays data
- [ ] PDF reports generate correctly
- [ ] Filters and search work
- [ ] Real-time updates function

## 🛠️ Technologies Used

### Backend
- Java 17
- Spring Boot 3.2.2
- Spring Data JPA
- Spring Cache
- Spring Actuator

### Database
- H2 (Development)
- PostgreSQL (Production)

### Caching
- Redis
- Jedis client

### Azure
- Application Insights SDK
- Azure Monitor Query

### Reporting
- iText PDF 8.0.3

### Utilities
- Lombok with mock data
- **TESTING_GUIDE.md** - Comprehensive testing guide with mock data scenarios
- Jackson
- Micrometer

## 📚 Documentation Map

- **README.md** - Complete documentation, API reference, configuration guide
- **QUICKSTART.md** - Step-by-step setup for first-time users
- **KQL_QUERIES.md** - Azure Application Insights query examples
- **PROJECT_STRUCTURE.md** - This file, overview of entire project

## 🎓 Learning Resources

### Spring Boot
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)

### Azure Application Insights
- [Application Insights Overview](https://learn.microsoft.com/en-us/azure/azure-monitor/app/app-insights-overview)
- [KQL Querwith Mock Data**: Generate test data immediately with quick-setup
2. **Use H2 Console**: Great for debugging database queries and exploring data structure
3. **Test Incrementally**: Start with sessions, then workers, then dashboard
4. **Check Health Endpoints**: Always verify system health before testing
5. **Test Caching**: Use Redis CLI to inspect cached data
6. **Monitor Logs**: Review logs for debugging and understanding data flow
7. **Read TESTING_GUIDE.md**: Follow testing scenarios for comprehensive validation
8. **Develop Frontend in Parallel**: Mock data allows frontend development before Azure
9. **Keep Mock and Real Separate**: Design makes transition to Azure seamless

## 🎯 Getting Started Today

```powershell
# 1. Start the application
cd backend
mvn spring-boot:run

# 2. Generate mock data (in another terminal)
curl -X POST http://localhost:8080/api/mock-data/quick-setup

# 3. Test dashboard
curl http://localhost:8080/api/dashboard/stats

# 4. Explore in H2 console
# Open browser: http://localhost:8080/api/h2-console

# 5. Read testing guide for more
# See TESTING_GUIDE.mdComplete with Mock Data - Ready for Full Testing & Frontend Development

_Next: Generate mock data and start testing all features! See [TESTING_GUIDE.md](TESTING_GUIDE.md) to begin._
```
## 💡 Tips for Development

1. **Start Simple**: Test with H2 and mock data first
2. **Use H2 Console**: Great for debugging database queries
3. **Check Health Endpoints**: Always verify system health
4. **Test Caching**: Use Redis CLI to inspect cached data
5. **Log Everything**: Review logs for debugging
6. **Read KQL_QUERIES.md**: Prepare for Azure integration

## 🤝 Support

If you encounter issues:
1. Check logs in `logs/monitoring-application.log`
2. Verify Redis is running: `redis-cli ping`
3. Check H2 console: `http://localhost:8080/api/h2-console`
4. Review health endpoint: `http://localhost:8080/api/actuator/health`
5. Consult README.md troubleshooting section

---

**Project Status**: ✅ Backend Structure Complete - Ready for Azure Integration
