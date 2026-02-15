# Quick Start Guide

## First Time Setup

### 1. Install Dependencies

**Java 17:**
```powershell
# Check if Java is installed
java -version

# If not installed, download from https://adoptium.net/
```

**Maven:**
```powershell
# Check if Maven is installed
mvn -version

# If not installed, download from https://maven.apache.org/download.cgi
```

**Redis:**
```powershell
# Option 1: Using Chocolatey
choco install redis-64
redis-server

# Option 2: Using Docker
docker run -d -p 6379:6379 redis:latest

# Option 3: Download from https://github.com/microsoftarchive/redis/releases
```

### 2. Configure the Application

1. Open `src/main/resources/application.yml`

2. Update Azure Application Insights settings (when available):
   ```yaml
   azure:
     application-insights:
       instrumentation-key: YOUR_KEY
       connection-string: YOUR_CONNECTION_STRING
       workspace-id: YOUR_WORKSPACE_ID
   ```

3. For now, you can use placeholder values to test the structure

### 3. Run the Application

```powershell
# Navigate to the backend directory
cd backend

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api`

### 4. Generate Mock Test Data

Since you don't have Azure access yet, generate realistic mock data:

```powershell
# Quick setup (recommended for first time)
curl -X POST http://localhost:8080/api/mock-data/quick-setup

# This will:
# - Clear any existing data
# - Create 100 diagnostic sessions
# - Generate ~600 worker events
# - Include completed, failed, and in-progress sessions
```

**Verify data was created:**
```powershell
curl http://localhost:8080/api/mock-data/stats
```

Expected output:
```json
{
  "totalSessions": 100,
  "totalEvents": 600,
  "completedSessions": 70,
  "failedSessions": 15,
  "activeSessions": 10
}
```

### 5. Test the Endpoints

**Health Check:**
```powershell
curl http://localhost:8080/api/actuator/health
```

**H2 Database Console:**
Open browser: `http://localhost:8080/api/h2-console`
- JDBC URL: `jdbc:h2:mem:monitoring_db`
- Username: `sa`
- Password: (leave empty)

**H2 Database Console:**
Open browser: `http://localhost:8080/api/h2-console`
- JDBC URL: `jdbc:h2:mem:monitoring_db`
- Username: `sa`
- Password: (leave empty)

**Query the mock data:**
```sql
-- View all sessions
SELMock Data Options

### Quick Setup (Recommended)
```powershell
# Clears DB and creates 100 test sessions
curl -X POST http://localhost:8080/api/mock-data/quick-setup
```

### Custom Amount
```powershell
# Generate 50 sessions
curl -X POST "http://localhost:8080/api/mock-data/generate?count=50"

# Generate 500 sessions
curl -X POST "http://localhost:8080/api/mock-data/generate?count=500"
```

### Comprehensive Dataset
```powershell
# Generates distributed data:
# - 50 sessions in last 24 hours
# - 200 sessions in last 7 days  
# - 500 sessions in last 30 days
curl -X POST http://localhost:8080/api/mock-data/generate-comprehensive
```

### Clear All Data
```powershell
curl -X DELETE http://localhost:8080/api/mock-data/clear
```

## ECT * FROM diagnostic_sessions ORDER BY start_time DESC;
Now that you have the backend running with mock data:

1. ✅ Backend running successfully
2. ✅ Mock data generated
3. ✅ All endpoints testable

**Next Steps:**

1. **Explore the data in H2 console** - See what mock data looks like
2. **Test all API endpoints** - Use the examples above or [TESTING_GUIDE.md](TESTING_GUIDE.md)
3. **Test dashboard statistics** - Verify analytics calculations work
4. **Generate PDF reports** - Test report generation with mock sessions
5. **Verify caching** - Make same request twice, second should be faster
6. **Monitor logs** - Watch `logs/monitoring-application.log` for insights
7. **Develop frontend** - Start Angular development with real API data
8. **Wait for Azure access** - Then integrate real Application Insights data

## Detailed Testing

For comprehensive testing workflows, see:
- **[TESTING_GUIDE.md](TESTING_GUIDE.md)** - Complete testing guide with mock data
- **[README.md](README.md)** - Full API documentation
- **[KQL_QUERIES.md](KQL_QUERIES.md)** - Azure queries for future integration

## Ready for Azure Integration

When you get Azure access:ic_sessions GROUP BY status;
```

**Test Specific Endpoints:**
```powershell
# Get dashboard stats
curl http://localhost:8080/api/dashboard/stats

# Get real-time stats
curl http://localhost:8080/api/dashboard/realtime

# Search sessions (create filter.json first)
curl -X POST http://localhost:8080/api/sessions/search `
  -H "Content-Type: application/json" `
  -d '{"page":0,"size":20}'

# Download a PDF report (replace SESSION-ID with actual ID from database)
curl -o report.pdf http://localhost:8080/api/reports/session/SESSION-XXXXX/pdf
```

**For comprehensive testing, see [TESTING_GUIDE.md](TESTING_GUIDE.md)**

## Mock Data Options

## What to Do Next

1. ✅ Backend structure is ready
2. ⏳ Wait for Azure Application Insights access
3. ⏳ Implement KQL queries in `ApplicationInsightsService.java`
4. ⏳ Test with real diagnostic session data
5. ⏳ Develop Angular frontend
6. ⏳ Connect frontend to backend APIs

## Project Structure Overview

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/vehicle/diagnostic/monitoring/
│   │   │   ├── config/          # Redis, CORS, Azure configs
│   │   │   ├── controller/      # REST APIs
│   │   │   ├── dto/             # Request/Response objects
│   │   │   ├── model/           # Database entities
│   │   │   ├── repository/      # Database access
│   │   │   ├── service/         # Business logic
│   │   │   ├── health/          # Health checks
│   │   │   ├── exception/       # Error handling
│   │   │   └── util/            # Helper classes
│   │   └── resources/
│   │       └── application.yml  # Configuration
│   └── test/                    # Unit tests
├── pom.xml                      # Dependencies
└── README.md                    # Full documentation
```

## Key Files to Update When Azure is Available

1. **ApplicationInsightsService.java**
   - Implement KQL queries
   - Query session data
   - Query worker events
   - Calculate metrics

2. **application.yml**
   - Add real Azure credentials
   - Update workspace ID

3. **SessionService.java**
   - Integrate Azure data with local database
   - Sync worker events

## Common Commands

```powershell
# Build project
mvn clean install

# Run application
mvn spring-boot:run

# Run tests
mvn test

# Package for production
mvn clean package

# Run packaged JAR
java -jar target/monitoring-backend-1.0.0.jar

# Check Redis is running
redis-cli ping
```

## Troubleshooting

**Redis not starting?**
- Windows: Check if Redis service is running in Services
- Docker: `docker ps` to see if container is running

**Port 8080 already in use?**
- Change port in `application.yml`:
  ```yaml
  server:
    port: 8081
  ```

**Maven build fails?**
- Check Java version: `java -version` (must be 17+)
- Delete `~/.m2/repository` and rebuild

## Need Help?

- Check logs in `logs/monitoring-application.log`
- Use H2 console to inspect database
- Check actuator endpoints for health status
- Review README.md for detailed documentation
