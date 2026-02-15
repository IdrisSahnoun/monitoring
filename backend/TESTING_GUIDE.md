# Testing Guide - Using Mock Data

This guide explains how to test the monitoring system locally using mock data before Azure Application Insights access is available.

## Quick Start with Mock Data

### 1. Start the Application

```powershell
# Make sure Redis is running
redis-server

# Start the application
cd backend
mvn spring-boot:run

# Keep this terminal open to see logs
```

**Important:** Watch the logs as they will show you:
- ✅ When workers complete successfully
- ❌ When workers fail with error details
- 📊 Session creation progress and statistics

See [MONITORING_LOGS_GUIDE.md](MONITORING_LOGS_GUIDE.md) for detailed log interpretation.

### 2. Generate Mock Data

**Option A: Quick Setup (Recommended for first time)**
```powershell
# Clear database and create 100 test sessions
curl -X POST http://localhost:8080/api/mock-data/quick-setup
```

**Watch the logs!** You'll see output like:
```log
========================================
📊 Generating 100 mock diagnostic sessions...
========================================
✓ Created session 1/100: SESSION-A1B2C3D4 - Status: COMPLETED - Workers: 6/6
  ✓ Worker InitializationWorker completed in 5234ms
  ✓ Worker DataCollectionWorker completed in 8912ms
  ...
✓ Created session 5/100: SESSION-X9Y8Z7W6 - Status: FAILED - Workers: 4/6
  ⚠ Worker ValidationWorker FAILED: Data validation failed
========================================
✅ Successfully created 100 mock sessions
   - Completed: 70 (70%)
   - Failed: 15 (15%)
   - In Progress: 10 (10%)
========================================
```

**Option B: Generate Specific Amount**
```powershell
# Generate 50 sessions
curl -X POST "http://localhost:8080/api/mock-data/generate?count=50"
```

**Option C: Comprehensive Dataset (Recommended for full testing)**
```powershell
# Generate data for last 24h, 7d, and 30d
curl -X POST http://localhost:8080/api/mock-data/generate-comprehensive
```

### 3. Verify Data Created

```powershell
# Check mock data statistics
curl http://localhost:8080/api/mock-data/stats
```

Expected response:
```json
{
  "totalSessions": 100,
  "totalEvents": 600,
  "completedSessions": 70,
  "failedSessions": 15,
  "activeSessions": 10
}
```

## Testing All Features

### Understanding Worker Success/Failure

**In the Logs:**
- ✅ `✓` or "COMPLETED" = Worker succeeded
- ❌ `✗` or "FAILED" = Worker failed
- ⚠️ Warning messages show error details


**Check the logs to see worker status:**
```log
🔍 Fetching session: SESSION-X9Y8Z7W6
✓ Found session SESSION-X9Y8Z7W6 - Status: FAILED - Duration: 28901ms
⚠ Session SESSION-X9Y8Z7W6 has 1 failed worker(s)
  - ValidationWorker failed: Data validation failed
```
**In API Responses:**
- `status: "COMPLETED"` = Success
- `status: "FAILED"` = Failure
- `errorDetails` field contains error message
- `failedExecutions` counter in metrics

**See [MONITORING_LOGS_GUIDE.md](MONITORING_LOGS_GUIDE.md) for complete log monitoring guide.**

### 1. Test Session Endpoints

**Get a specific session:**
```powershell
# First, get a session ID from the stats or database
curl http://localhost:8080/api/sessions/SESSION-XXXXX
```

**Search sessions:**
```powershell
# Create a filter request body: filter.json
# {
#   "status": "COMPLETED",
#   "page": 0,
#   "size": 20,
#   "sortBy": "startTime",
#   "sortDirection": "desc"
# }

cur

**Check the logs for detailed execution:**
```log
🔍 Fetching worker events for session: SESSION-X9Y8Z7W6
Found 4 worker events for session SESSION-X9Y8Z7W6
  ✓ Step 1: InitializationWorker - COMPLETED in 5123ms
  ✓ Step 2: DataCollectionWorker - COMPLETED in 7890ms
  ✓ Step 3: AnalysisWorker - COMPLETED in 12345ms
  ✗ Step 4: ValidationWorker - FAILED: Data validation failed
```l -X POST http://localhost:8080/api/sessions/search `
  -H "Content-Type: application/json" `
  -d '@filter.json'
```

**Get worker events for a session:**
```powershell
curl http://localhost:8080/api/sessions/SESSION-XXXXX/events
```

**Get performance metrics:**
```powershell
curl http://localhost:8080/api/sessions/SESSION-XXXXX/metrics
```

### 2. Test Dashboard Endpoints

**Get dashboard statistics:**
```powershell
# Default (last 24 hours)
curl http://localhost:8080/api/dashboard/stats

# Custom date range
curl "http://localhost:8080/api/dashboard/stats?startDate=2026-02-11T00:00:00&endDate=2026-02-12T23:59:59"
``` for a specific session
curl -o report.pdf http://localhost:8080/api/reports/session/SESSION-XXXXX/pdf
```

**The PDF will include:**
- Session status (COMPLETED/FAILED)
- Worker execution timeline with status indicators
- Failed workers highlighted with error details
- Performance metrics including failure rateset real-time statistics:**
```powershell
curl http://localhost:8080/api/dashboard/realtime
```

**Get worker-specific statistics:**
```powershell
curl http://localhost:8080/api/dashboard/worker/InitializationWorker
curl http://localhost:8080/api/dashboard/worker/DataCollectionWorker
curl http://localhost:8080/api/dashboard/worker/AnalysisWorker
```

### 3. Test PDF Report Generation

**Download PDF report:**
```powershell
# Downloads PDF file
curl -o report.pdf http://localhost:8080/api/reports/session/SESSION-XXXXX/pdf
```

**Save PDF report to server:**
```powershell
curl -X POST http://localhost:8080/api/reports/session/SESSION-XXXXX/save
```

### 4. Test Health Checks

**Overall health:**
```powershell
curl http://localhost:8080/api/actuator/health
```

Expected response (when everything is working):
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "redis": {"status": "UP"},
    "applicationInsights": {"status": "UP"},
    "workerMonitoring": {
      "status": "UP",
      "details": {
        "totalSessions": 100,
        "activeSessions": 10
      }
    }
  }
}
```

**Prometheus metrics:**
```powershell
curl http://localhost:8080/api/actuator/metrics
curl http://localhost:8080/api/actuator/prometheus
```

## Using H2 Database Console

The H2 console lets you inspect and query the mock data directly:

1. Open browser: `http://localhost:8080/api/h2-console`
2. Use these settings:
   - **JDBC URL**: `jdbc:h2:mem:monitoring_db`
   - **Username**: `sa`
   - **Password**: (leave empty)
3. Click "Connect"

### Useful SQL Queries

```sql
-- View all sessions
SELECT * FROM diagnostic_sessions ORDER BY start_time DESC;

-- Count sessions by status
SELECT status, COUNT(*) as count 
FROM diagnostic_sessions 
GROUP BY status;
, start_time
FROM diagnostic_sessions 
WHERE status = 'FAILED'
ORDER BY start_time DESC;

-- Get all failed worker events
SELECT session_id, worker_name, status, error_details, 
       execution_time_ms, retry_count
FROM worker_events
WHERE status = 'FAILED'
ORDER BY start_time DESC;

-- Count failures by worker type
SELECT worker_name, 
       COUNT(*) as total_failures,
       AVG(execution_time_ms) as avg_execution_time
FROM worker_events
WHERE status = 'FAILED'
GROUP BY worker_name
ORDER BY total_failures DESC;
, shows full logs)
curl http://localhost:8080/api/sessions/SESSION-XXXXX

# Check logs - you'll see database query
# 🔍 Fetching session: SESSION-XXXXX
# ✓ Found session SESSION-XXXXX...

# Second call - hits cache (faster, shows cache hit)
curl http://localhost:8080/api/sessions/SESSION-XXXXX

# This call will be much faster and skip database query
FROM diagnostic_sessions s
LEFT JOIN worker_events e ON s.session_id = e.session_id
WHERE s.session_id = 'SESSION-XXXXX'
ORDER BY e.step_numberents ORDER BY start_time DESC;

-- Get events for a specific session
SELECT * FROM worker_events 
WHERE session_id = 'SESSION-XXXXX' 
ORDER BY step_number;

-- Calculate average execution time per worker
SELECT worker_name, AVG(execution_time_ms) as avg_time, COUNT(*) as count
FROM worker_events 
WHERE status = 'COMPLETED'
GROUP BY worker_name;

-- Find failed sessions with errors
SELECT session_id, vehicle_id, status, error_message 
FROM diagnostic_sessions 
WHERE status = 'FAILED';
``` (all workers passed) ✅
- **15%** Failed with errors (one or more workers failed) ❌
- **10%** In progress (some workers completed) ⏳
- **5%** Initiated (just started) 🆕

### Common Failure Scenarios
Failed sessions include realistic error messages:
1. "Connection timeout to vehicle ECU"
2. "Invalid data received from sensor"
3. "Communication protocol error"
4. "Sensor malfunction detected"
5. "Data validation failed"
6. "Network connectivity issue"
7. "ECU response timeout"
8. "Unsupported diagnostic protocol"
### Verify Caching Works

```powershell
# First call - hits database (slower)
curl http://localhost:8080/api/sessions/SESSION-XXXXX

# Second call - hits cache (faster)
curl http://localhost:8080/api/sessions/SESSION-XXXXX
```

### Check Redis Keys

```powershell
# Connect to Redis CLI
redis-cli

# List all keys
KEYS *

# View a cached session
GET "sessions::SESSION-XXXXX"

# Check TTL (time to live)
TTL "sessions::SESSION-XXXXX"

# Clear all cache
FLUSHALL
```

## Mock Data Characteristics

The mock data generator creates realistic data with:

### Sessions Distribution
- **70%** Completed successfully
- **15%** Failed with errors
- **10%** In progress
- **5%** Initiated

### Worker Execution Times
- **1-20 seconds** per worker
- **Total session duration**: 10s - 2min for completed
- **Failed sessions**: Stop at random worker with error

### Vehicles & Types
- 10 different vehicle IDs
- 10 different VINs
- 5 diagnostic types (FULL, ENGINE, ELECTRICAL, BRAKE, TRANSMISSION)

### Workers
1. InitializationWorker
2. DataCollectionWorker
3. AnalysisWorker
4. ValidationWorker
5. ReportGenerationWorker
6. NotificationWorker

### Time Distribution
- **Quick Setup**: Random times within last 24 hours
- **Comprehensive**: Distributed across 24h, 7d, and 30d

## Mock Data Management

### Generate More Data
```powershell
# Add 200 more sessions
curl -X POST "http://localhost:8080/api/mock-data/generate?count=200"
```

### Clear All Data
```powershell
# Remove all sessions and events
curl -X DELETE http://localhost:8080/api/mock-data/clear
```

### Reset and Regenerate
```powershell
# Clear and create fresh dataset
curl -X POST http://localhost:8080/api/mock-data/quick-setup
```

## Testing Scenarios

### Scenario 1: Dashboard Testing
1. Generate comprehensive mock data
2. Open dashboard endpoints
3. Verify statistics are calculated correctly
4. Check different time ranges

### Scenario 2: Session Lifecycle Testing
1. Query sessions with different statuses
2. View worker events for each session
3. Generate and review PDF reports
4. Test performance metrics calculation

### Scenario 3: Performance Testing
1. Generate 1000 sessions
2. Test pagination and filtering
3. Verify caching improves response times
4. Check database query performance

### Scenario 4: Error Handling Testing
1. Query non-existent session IDs
2. Test with invalid filters
3. Verify error responses are proper

## Preparing for Azure Integration

While testing with mock data, note down:
Watch the logs**: Keep terminal visible to see worker execution in real-time
3. **Use H2 console**: Great for debugging data issues and finding failed workers
4. **Test caching**: Clear cache and compare response times
5. **Vary data**: Generate multiple datasets to test edge cases
6. **Monitor failures**: Use logs and SQL queries to track which workers fail most
7. **Test all endpoints**: Ensure each API works before Azure integration
8. **Review error messages**: Verify error propagation works correctly

### Monitoring Logs in Real-Time

**PowerShell:**
```powershell
# Follow application logs
Get-Content logs/monitoring-application.log -Wait -Tail 50

# Filter for failures only
Get-Content logs/monitoring-application.log | Select-String "FAILED|ERROR|⚠|❌"
```

**W✅ Worker failures are tracked and logged correctly
6. ✅ Error messages propagate properly
7. ⏳ Integrate with Azure Application Insights
8. ⏳ Develop Angular frontend
9. ⏳ End-to-end testing with real data

## Additional Resources

- **[MONITORING_LOGS_GUIDE.md](MONITORING_LOGS_GUIDE.md)** - Complete guide to reading logs and tracking worker success/failure
- **[README.md](README.md)** - Full API documentation and configuration
- **[QUICKSTART.md](QUICKSTART.md)** - Quick setup instructions
- **[KQL_QUERIES.md](KQL_QUERIES.md)** - Azure queries for future integration
tail -f logs/monitoring-application.log

# Filter for failures
grep -E "FAILED|ERROR" logs/monitoring-application.log
```
5. **Dashboard Metrics**: Fine-tune what metrics to track

## When Azure Access is Available

Simply update `ApplicationInsightsService.java`:

```java
@Service
public class ApplicationInsightsService {
    
    // Replace mock queries with real KQL queries
    public Map<String, Object> querySessionData(String sessionId) {
        String kqlQuery = """
            customEvents
            | where customDimensions.sessionId == '%s'
            | order by timestamp asc
            """.formatted(sessionId);
        
        return executeKqlQuery(kqlQuery);
    }
}
```

The rest of the application will work seamlessly with Azure data!

## Troubleshooting Mock Data

**No data showing up?**
- Check mock data stats: `curl http://localhost:8080/api/mock-data/stats`
- Verify database in H2 console
- Check logs for errors

**Caching issues?**
- Flush Redis: `redis-cli FLUSHALL`
- Restart application to refresh cache configuration

**Performance issues?**
- Reduce mock data count
- Enable pagination in queries
- Check database indexes

## Tips for Effective Testing

1. **Start small**: Use quick-setup for initial testing
2. **Use H2 console**: Great for debugging data issues
3. **Test caching**: Clear cache and compare response times
4. **Vary data**: Generate multiple datasets to test edge cases
5. **Monitor logs**: Watch application logs for insights
6. **Test all endpoints**: Ensure each API works before Azure integration

## Next Steps

After validating with mock data:
1. ✅ All endpoints work correctly
2. ✅ Dashboard displays proper statistics
3. ✅ PDF reports generate successfully
4. ✅ Caching improves performance
5. ⏳ Integrate with Azure Application Insights
6. ⏳ Develop Angular frontend
7. ⏳ End-to-end testing with real data
