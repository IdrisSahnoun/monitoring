# Azure Application Insights KQL Queries Reference

This document contains KQL (Kusto Query Language) queries that will be used to query Azure Application Insights once access is available.

## Basic Session Queries

### Get all events for a specific session
```kql
customEvents
| where customDimensions.sessionId == '{sessionId}'
| project timestamp, name, customDimensions, customMeasurements
| order by timestamp asc
```

### Get all worker executions for a session
```kql
customEvents
| where name == "WorkerExecution"
| where customDimensions.sessionId == '{sessionId}'
| project 
    timestamp,
    workerName = tostring(customDimensions.workerName),
    status = tostring(customDimensions.status),
    executionTime = todouble(customMeasurements.executionTimeMs),
    stepNumber = toint(customDimensions.stepNumber)
| order by stepNumber asc
```

## Performance Metrics Queries

### Calculate average execution time per worker
```kql
customEvents
| where name == "WorkerExecution"
| where timestamp between (datetime({startTime}) .. datetime({endTime}))
| summarize 
    avgExecutionTime = avg(todouble(customMeasurements.executionTimeMs)),
    minExecutionTime = min(todouble(customMeasurements.executionTimeMs)),
    maxExecutionTime = max(todouble(customMeasurements.executionTimeMs)),
    totalExecutions = count()
by workerName = tostring(customDimensions.workerName)
```

### Calculate percentiles for latency
```kql
customEvents
| where name == "WorkerExecution"
| where timestamp > ago(24h)
| summarize 
    p50 = percentile(todouble(customMeasurements.executionTimeMs), 50),
    p95 = percentile(todouble(customMeasurements.executionTimeMs), 95),
    p99 = percentile(todouble(customMeasurements.executionTimeMs), 99)
by workerName = tostring(customDimensions.workerName)
```

### Success/Failure Rate
```kql
customEvents
| where name == "WorkerExecution"
| where timestamp > ago(24h)
| summarize 
    total = count(),
    successful = countif(tostring(customDimensions.status) == "COMPLETED"),
    failed = countif(tostring(customDimensions.status) == "FAILED")
by workerName = tostring(customDimensions.workerName)
| extend successRate = (successful * 100.0) / total
| extend failureRate = (failed * 100.0) / total
```

## Dashboard Queries

### Active sessions count
```kql
customEvents
| where name == "SessionStarted" or name == "SessionCompleted"
| where timestamp > ago(1h)
| summarize 
    started = countif(name == "SessionStarted"),
    completed = countif(name == "SessionCompleted")
| extend active = started - completed
```

### Sessions by status over time
```kql
customEvents
| where name in ("SessionStarted", "SessionCompleted", "SessionFailed")
| where timestamp > ago(24h)
| summarize count() by bin(timestamp, 1h), name
| render timechart
```

### Top sessions by duration
```kql
customEvents
| where name == "SessionCompleted"
| where timestamp > ago(24h)
| project 
    sessionId = tostring(customDimensions.sessionId),
    duration = todouble(customMeasurements.durationMs),
    vehicleId = tostring(customDimensions.vehicleId)
| top 10 by duration desc
```

## Error Analysis

### Failed workers with error details
```kql
customEvents
| where name == "WorkerExecution"
| where tostring(customDimensions.status) == "FAILED"
| where timestamp > ago(24h)
| project 
    timestamp,
    sessionId = tostring(customDimensions.sessionId),
    workerName = tostring(customDimensions.workerName),
    errorDetails = tostring(customDimensions.errorDetails),
    retryCount = toint(customDimensions.retryCount)
| order by timestamp desc
```

### Error frequency by worker
```kql
customEvents
| where name == "WorkerExecution"
| where tostring(customDimensions.status) == "FAILED"
| where timestamp > ago(7d)
| summarize errorCount = count() by 
    workerName = tostring(customDimensions.workerName),
    bin(timestamp, 1d)
| render columnchart
```

## Advanced Queries

### Worker execution timeline for a session
```kql
customEvents
| where name == "WorkerExecution"
| where customDimensions.sessionId == '{sessionId}'
| project 
    startTime = timestamp,
    workerName = tostring(customDimensions.workerName),
    executionTime = todouble(customMeasurements.executionTimeMs)
| extend endTime = startTime + totimespan(executionTime * 10000) // Convert ms to ticks
| project workerName, startTime, endTime, executionTime
| order by startTime asc
```

### Session funnel analysis
```kql
let sessionId = '{sessionId}';
customEvents
| where customDimensions.sessionId == sessionId
| where name == "WorkerExecution"
| summarize by 
    step = tostring(customDimensions.workerName),
    status = tostring(customDimensions.status)
| order by step asc
```

### Correlation between retry count and execution time
```kql
customEvents
| where name == "WorkerExecution"
| where timestamp > ago(7d)
| project 
    retryCount = toint(customDimensions.retryCount),
    executionTime = todouble(customMeasurements.executionTimeMs)
| where isnotnull(retryCount)
| summarize avgExecutionTime = avg(executionTime) by retryCount
| render columnchart
```

## Time-based Analysis

### Hourly session distribution
```kql
customEvents
| where name == "SessionStarted"
| where timestamp > ago(7d)
| extend hour = hourofday(timestamp)
| summarize sessionCount = count() by hour
| order by hour asc
```

### Peak usage times
```kql
customEvents
| where name == "WorkerExecution"
| where timestamp > ago(30d)
| summarize executionCount = count() by bin(timestamp, 1h)
| top 10 by executionCount desc
| project timestamp, executionCount
```

### Average session duration by day of week
```kql
customEvents
| where name == "SessionCompleted"
| where timestamp > ago(30d)
| extend dayOfWeek = dayofweek(timestamp)
| summarize avgDuration = avg(todouble(customMeasurements.durationMs))
by dayOfWeek
| order by dayOfWeek asc
```

## Vehicle-specific Queries

### Sessions by vehicle
```kql
customEvents
| where name == "SessionStarted"
| where timestamp > ago(7d)
| summarize sessionCount = count() by 
    vehicleId = tostring(customDimensions.vehicleId)
| order by sessionCount desc
| take 20
```

### Vehicle diagnostic patterns
```kql
customEvents
| where name == "SessionCompleted"
| where timestamp > ago(30d)
| project 
    vehicleId = tostring(customDimensions.vehicleId),
    diagnosticType = tostring(customDimensions.diagnosticType),
    status = tostring(customDimensions.status),
    duration = todouble(customMeasurements.durationMs)
| summarize 
    sessionCount = count(),
    avgDuration = avg(duration),
    successRate = (countif(status == "COMPLETED") * 100.0) / count()
by vehicleId, diagnosticType
```

## How to Use These Queries

### In ApplicationInsightsService.java:

```java
public List<Map<String, Object>> queryWorkerEvents(String sessionId) {
    String kqlQuery = String.format("""
        customEvents
        | where name == "WorkerExecution"
        | where customDimensions.sessionId == '%s'
        | project timestamp, workerName=customDimensions.workerName, 
                status=customDimensions.status, duration=customMeasurements.executionTimeMs
        | order by timestamp asc
        """, sessionId);
    
    return executeKqlQuery(kqlQuery);
}
```

### Using Azure Monitor Query Client:

```java
import com.azure.monitor.query.LogsQueryClient;
import com.azure.monitor.query.LogsQueryClientBuilder;
import com.azure.monitor.query.models.*;

LogsQueryClient client = new LogsQueryClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();

LogsQueryResult results = client.queryWorkspace(
    workspaceId, 
    kqlQuery, 
    QueryTimeInterval.LAST_DAY
);
```

## Testing Queries

1. Go to Azure Portal
2. Navigate to Application Insights resource
3. Click on "Logs" in the left menu
4. Paste and test queries
5. Adjust as needed for your custom dimensions/measurements

## Notes

- Replace `{sessionId}`, `{startTime}`, `{endTime}` with actual values
- Adjust time ranges (`ago(24h)`, `ago(7d)`) based on requirements
- Add more custom dimensions as needed for your use case
- Use `bin()` function for time bucketing in charts
- Use `render` for visualization (timechart, columnchart, piechart)
