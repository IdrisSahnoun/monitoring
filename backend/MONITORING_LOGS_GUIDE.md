# Monitoring Logs Guide - Tracking Worker Success/Failure

This guide explains how to monitor worker execution, identify failures, and track session progress using logs and APIs.

## Understanding Worker Status

### Worker Event Statuses
- **STARTED** - Worker has begun execution
- **RUNNING** - Worker is currently processing
- **COMPLETED** - Worker finished successfully ✅
- **FAILED** - Worker encountered an error ❌
- **SKIPPED** - Worker was skipped
- **RETRYING** - Worker is retrying after failure

### Session Statuses
- **INITIATED** - Session created
- **IN_PROGRESS** - Workers are executing
- **COMPLETED** - All workers succeeded ✅
- **FAILED** - One or more workers failed ❌
- **CANCELLED** - Session was cancelled
- **TIMEOUT** - Session exceeded time limit

## Reading Application Logs

### Log Levels and Symbols

The application uses clear symbols to indicate status:

```
✓ or ✅ = Success
✗ or ❌ = Failure
⚠ = Warning
🔍 = Query/Search
📊 = Statistics
⏳ = In Progress
```

### Example Log Output

#### Successful Session Generation
```log
========================================
📊 Generating 10 mock diagnostic sessions...
========================================
✓ Created session 1/10: SESSION-A1B2C3D4 - Status: COMPLETED - Workers: 6/6
  ✓ Worker InitializationWorker completed in 5234ms
  ✓ Worker DataCollectionWorker completed in 8912ms
  ✓ Worker AnalysisWorker completed in 15670ms
  ✓ Worker ValidationWorker completed in 3456ms
  ✓ Worker ReportGenerationWorker completed in 7890ms
  ✓ Worker NotificationWorker completed in 2345ms
✓ Created session 2/10: SESSION-E5F6G7H8 - Status: COMPLETED - Workers: 6/6
========================================
✅ Successfully created 10 mock sessions
   - Completed: 7 (70%)
   - Failed: 2 (20%)
   - In Progress: 1 (10%)
   - Total Worker Events: 60
========================================
```

#### Failed Session Generation
```log
✓ Created session 5/10: SESSION-X9Y8Z7W6 - Status: FAILED - Workers: 4/6
  ✓ Worker InitializationWorker completed in 5123ms
  ✓ Worker DataCollectionWorker completed in 7890ms
  ✓ Worker AnalysisWorker completed in 12345ms
  ⚠ Worker ValidationWorker FAILED: Data validation failed
```

### Querying Session Logs

#### When Fetching a Session
```log
🔍 Fetching session: SESSION-A1B2C3D4
✓ Found session SESSION-A1B2C3D4 - Status: COMPLETED - Duration: 42567ms
✓ All 6 workers completed successfully
```

#### When Session Has Failures
```log
🔍 Fetching session: SESSION-X9Y8Z7W6
✓ Found session SESSION-X9Y8Z7W6 - Status: FAILED - Duration: 28901ms
⚠ Session SESSION-X9Y8Z7W6 has 1 failed worker(s)
  - ValidationWorker failed: Data validation failed
```

#### When Fetching Worker Events
```log
🔍 Fetching worker events for session: SESSION-X9Y8Z7W6
Found 4 worker events for session SESSION-X9Y8Z7W6
  ✓ Step 1: InitializationWorker - COMPLETED in 5123ms
  ✓ Step 2: DataCollectionWorker - COMPLETED in 7890ms
  ✓ Step 3: AnalysisWorker - COMPLETED in 12345ms
  ✗ Step 4: ValidationWorker - FAILED: Data validation failed
```

## Monitoring via API Responses

### 1. Get Session Status

**Request:**
```powershell
curl http://localhost:8080/api/sessions/SESSION-X9Y8Z7W6
```

**Response (Failed Session):**
```json
{
  "sessionId": "SESSION-X9Y8Z7W6",
  "vehicleId": "VEH-003",
  "status": "FAILED",
  "startTime": "2026-02-12T10:30:00",
  "endTime": "2026-02-12T10:30:28",
  "durationMs": 28901,
  "currentWorker": null,
  "completedSteps": 3,
  "totalSteps": 6,
  "progressPercentage": 50.0,
  "errorMessage": "Data validation failed",
  "workerEvents": [...],
  "performanceMetrics": {
    "totalExecutions": 4,
    "successfulExecutions": 3,
    "failedExecutions": 1,
    "successRate": 75.0,
    "failureRate": 25.0
  }
}
```

**Key Indicators of Failure:**
- ❌ `status`: "FAILED"
- ❌ `errorMessage`: Contains error details
- ❌ `completedSteps` < `totalSteps`
- ❌ `failedExecutions` > 0

### 2. Get Worker Events

**Request:**
```powershell
curl http://localhost:8080/api/sessions/SESSION-X9Y8Z7W6/events
```

**Response:**
```json
[
  {
    "eventId": "evt-1",
    "workerName": "InitializationWorker",
    "status": "COMPLETED",
    "executionTimeMs": 5123,
    "stepNumber": 1,
    "errorDetails": null
  },
  {
    "eventId": "evt-2",
    "workerName": "DataCollectionWorker",
    "status": "COMPLETED",
    "executionTimeMs": 7890,
    "stepNumber": 2,
    "errorDetails": null
  },
  {
    "eventId": "evt-3",
    "workerName": "AnalysisWorker",
    "status": "COMPLETED",
    "executionTimeMs": 12345,
    "stepNumber": 3,
    "errorDetails": null
  },
  {
    "eventId": "evt-4",
    "workerName": "ValidationWorker",
    "status": "FAILED",
    "executionTimeMs": 3543,
    "stepNumber": 4,
    "errorDetails": "Data validation failed",
    "retryCount": 2
  }
]
```

**Look for:**
- ✅ `status`: "COMPLETED" = Success
- ❌ `status`: "FAILED" = Failure
- ❌ `errorDetails`: Error message
- ⚠️ `retryCount` > 0: Worker was retried

### 3. Dashboard Statistics

**Request:**
```powershell
curl http://localhost:8080/api/dashboard/stats
```

**Response:**
```json
{
  "totalSessions": 100,
  "completedSessions": 70,
  "failedSessions": 15,
  "overallSuccessRate": 70.0,
  "workerMetrics": {
    "ValidationWorker": {
      "totalExecutions": 85,
      "successfulExecutions": 70,
      "failedExecutions": 15,
      "successRate": 82.35,
      "averageExecutionTime": 5234.5
    }
  }
}
```

**Identify Problem Workers:**
- Low `successRate` indicates problematic worker
- High `failedExecutions` shows frequent failures
- Compare `successRate` across workers to find bottlenecks

## Using H2 Console to Query Failures

### 1. Find All Failed Sessions

```sql
SELECT session_id, vehicle_id, error_message, start_time, duration_ms
FROM diagnostic_sessions
WHERE status = 'FAILED'
ORDER BY start_time DESC;
```

### 2. Find All Failed Worker Events

```sql
SELECT e.session_id, e.worker_name, e.error_details, 
       e.execution_time_ms, e.retry_count, e.start_time
FROM worker_events e
WHERE e.status = 'FAILED'
ORDER BY e.start_time DESC;
```

### 3. Count Failures by Worker

```sql
SELECT worker_name, 
       COUNT(*) as total_failures,
       AVG(execution_time_ms) as avg_execution_ms
FROM worker_events
WHERE status = 'FAILED'
GROUP BY worker_name
ORDER BY total_failures DESC;
```

### 4. Get Complete Session Timeline

```sql
SELECT 
    s.session_id,
    s.status as session_status,
    s.error_message as session_error,
    e.step_number,
    e.worker_name,
    e.status as worker_status,
    e.execution_time_ms,
    e.error_details as worker_error
FROM diagnostic_sessions s
LEFT JOIN worker_events e ON s.session_id = e.session_id
WHERE s.session_id = 'SESSION-X9Y8Z7W6'
ORDER BY e.step_number;
```

### 5. Find Sessions with Specific Worker Failures

```sql
SELECT DISTINCT s.session_id, s.vehicle_id, s.status, s.error_message
FROM diagnostic_sessions s
JOIN worker_events e ON s.session_id = e.session_id
WHERE e.worker_name = 'ValidationWorker'
  AND e.status = 'FAILED';
```

## Monitoring Logs in Real-Time

### View Live Logs

**PowerShell:**
```powershell
# Follow application logs
Get-Content logs/monitoring-application.log -Wait -Tail 50
```

**WSL/Linux:**
```bash
tail -f logs/monitoring-application.log
```

### Filter Logs for Failures

**PowerShell:**
```powershell
# Show only errors and warnings
Get-Content logs/monitoring-application.log | Select-String -Pattern "ERROR|WARN|FAILED|✗|❌"

# Show specific worker failures
Get-Content logs/monitoring-application.log | Select-String -Pattern "ValidationWorker.*FAILED"
```

**WSL/Linux:**
```bash
# Show only failures
grep -E "ERROR|WARN|FAILED" logs/monitoring-application.log

# Show specific session
grep "SESSION-X9Y8Z7W6" logs/monitoring-application.log
```

## Common Error Messages in Mock Data

The mock data generator randomly assigns these error messages to failed workers:

1. **"Connection timeout to vehicle ECU"** - Network issue
2. **"Invalid data received from sensor"** - Data quality issue
3. **"Communication protocol error"** - Protocol mismatch
4. **"Sensor malfunction detected"** - Hardware issue
5. **"Data validation failed"** - Validation error
6. **"Network connectivity issue"** - Network problem
7. **"ECU response timeout"** - Response timeout
8. **"Unsupported diagnostic protocol"** - Protocol not supported

## Tracking Worker Performance

### 1. View Worker Statistics

**Request:**
```powershell
curl http://localhost:8080/api/dashboard/worker/ValidationWorker
```

**Response:**
```json
{
  "workerName": "ValidationWorker",
  "totalExecutions": 85,
  "successfulExecutions": 70,
  "failedExecutions": 15,
  "successRate": 82.35,
  "averageExecutionTime": 5234.5
}
```

### 2. Compare All Workers

```sql
SELECT 
    worker_name,
    COUNT(*) as total,
    SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed,
    SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) as failed,
    ROUND(AVG(execution_time_ms), 2) as avg_time_ms,
    ROUND(
        (SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) * 100.0) / COUNT(*),
        2
    ) as success_rate
FROM worker_events
GROUP BY worker_name
ORDER BY success_rate ASC;
```

## Best Practices for Monitoring

### During Development
1. ✅ Keep log level at DEBUG to see all worker details
2. ✅ Monitor logs in real-time during testing
3. ✅ Check dashboard statistics after generating mock data
4. ✅ Use H2 console to query specific failures
5. ✅ Review performance metrics for each worker

### For Testing
1. ✅ Generate comprehensive mock data first
2. ✅ Query failed sessions specifically
3. ✅ Verify error messages are propagated correctly
4. ✅ Test retry logic (check `retryCount`)
5. ✅ Validate performance metrics calculations

### For Production (Future)
1. ✅ Set log level to INFO
2. ✅ Monitor Azure Application Insights for failures
3. ✅ Set up alerts for high failure rates
4. ✅ Track worker performance trends
5. ✅ Review error patterns regularly

## Quick Reference Commands

```powershell
# Generate mock data with logging
curl -X POST http://localhost:8080/api/mock-data/quick-setup

# Get mock data statistics
curl http://localhost:8080/api/mock-data/stats

# View a session (check logs for details)
curl http://localhost:8080/api/sessions/SESSION-XXXXX

# Get worker events (check logs for status)
curl http://localhost:8080/api/sessions/SESSION-XXXXX/events

# Get worker performance
curl http://localhost:8080/api/dashboard/worker/ValidationWorker

# View real-time logs
Get-Content logs/monitoring-application.log -Wait -Tail 50

# Filter for failures only
Get-Content logs/monitoring-application.log | Select-String "FAILED|ERROR"
```

## Troubleshooting

**Not seeing detailed logs?**
- Check `application.yml` - ensure `logging.level.com.vehicle.diagnostic: DEBUG`
- Restart application after changing log level

**Logs file not created?**
- Check `logging.file.name` in `application.yml`
- Ensure `logs/` directory exists
- Check write permissions

**Too many logs?**
- Set log level to INFO: `logging.level.com.vehicle.diagnostic: INFO`
- This will hide DEBUG worker completion messages
- Errors and warnings will still show

## Next Steps

1. ✅ Generate mock data and watch the logs
2. ✅ Query sessions with failed status
3. ✅ Check worker events for error details
4. ✅ Use H2 console to explore failure patterns
5. ✅ Verify dashboard statistics match logs
6. ⏳ When Azure is ready, same logging will work with real data!
