# DiagCloud Integration Updates

## Overview
The monitoring system has been updated to match DiagCloud's exact specifications and domain model.

## Key Changes

### 1. Session Statuses (SessionStatus Enum)
**Old Statuses:**
- INITIATED, IN_PROGRESS, COMPLETED, FAILED, CANCELLED, TIMEOUT

**New DiagCloud Statuses:**
- `INITIALIZING` - Session is being initialized
- `STARTING` - Starting saga workers
- `RUNNING` - Session in progress
- `CLOSED` - Successfully completed
- `CANCELLED` - Cancelled by user
- `ERROR` - Failed with errors
- `SHUTDOWN_REQUESTED` - Shutdown has been requested

### 2. Worker Types (MessageType Enum)
Workers are now identified by `messageType` instead of generic `workerName`.

**Starting Saga Workers:**
- `BOOK_VCI_SERVER`
- `CREATE_PRODUCT_INSTANCE`
- `DETERMINE_PRODUCT_VERSION`
- `SEARCH_LICENSE`
- `CONFIGURE_SESSION`
- `INITIALIZE_DIAGNOSTICS`
- `START_COMMUNICATION`
- `VALIDATE_CONNECTION`

**Shutdown Saga Workers:**
- `STOP_COMMUNICATION`
- `SAVE_SESSION_DATA`
- `CLEANUP_RESOURCES`
- `RELEASE_VCI_SERVER`
- `SEND_NOTIFICATION`

**Common/Utility Workers:**
- `HEALTH_CHECK`
- `LOG_EVENT`
- `UPDATE_STATUS`

### 3. New Fields in DiagnosticSession

**Added:**
- `operationId` (String) - Type of operation: "starting" or "shutdown"
- `productId` (String) - Product image name (e.g., WDB1, VCI_EXE1, DBX_V7.2.3)
- `userId` (String) - User who initiated the session

**Updated:**
- `sessionId` - Now called "instanceId" in DiagCloud but stored as sessionId in database
- `currentWorker` - Now stores MessageType enum name

### 4. WorkerEvent Changes

**Renamed Field:**
- `workerName` → `messageType` (MessageType enum)

### 5. Mock Data Updates

**Products:**
- WDB1, WDB2, VCI_EXE1, VCI_EXE2
- DBX_V7.2.3, DBX_V7.3.1, DBX_V8.0.0
- STAR_DIAG_V5, STAR_DIAG_V6, OEM_TOOL_V3

**Users:**
- operator.oi@company.com
- tech.support@company.com
- admin@company.com
- mechanic1@workshop.com
- mechanic2@workshop.com
- diagnostician@dealer.com
- field.tech@service.com
- qa.tester@company.com

**Error Messages (DiagCloud-specific):**
- "VCI Server booking failed - no available servers"
- "Product instance creation timeout"
- "License not found for product: {productId}"
- "WRS 404 - Product version not available"
- "Communication initialization failed with vehicle ECU"
- "Invalid product configuration for: {productId}"
- "Database connection timeout during session setup"
- "Kafka message delivery failed"
- "Session validation failed - missing required parameters"
- "VCI connection timeout - check network connectivity"

### 6. Status Distribution in Mock Data

**Sessions:**
- 60% CLOSED (successful)
- 15% ERROR (failed)
- 10% RUNNING (in progress)
- 7% STARTING
- 5% INITIALIZING
- 2% CANCELLED
- 1% SHUTDOWN_REQUESTED

## API Examples

### Session Response (Updated)
```json
{
  "sessionId": "INST-A1B2C3D4",
  "operationId": "starting",
  "vehicleId": "VEH-001",
  "vehicleVin": "1HGBH41JXMN109186",
  "productId": "DBX_V7.2.3",
  "userId": "operator.oi@company.com",
  "status": "CLOSED",
  "startTime": "2026-02-20T10:15:30",
  "endTime": "2026-02-20T10:18:45",
  "durationMs": 195000,
  "completedSteps": 8,
  "totalSteps": 8
}
```

### Worker Event Response (Updated)
```json
{
  "eventId": "evt-123",
  "sessionId": "INST-A1B2C3D4",
  "messageType": "BOOK_VCI_SERVER",
  "status": "COMPLETED",
  "startTime": "2026-02-20T10:15:30",
  "endTime": "2026-02-20T10:15:35",
  "executionTimeMs": 5234,
  "stepNumber": 1,
  "retryCount": 0
}
```

### Failed Worker Event Example
```json
{
  "eventId": "evt-456",
  "sessionId": "INST-X9Y8Z7W6",
  "messageType": "SEARCH_LICENSE",
  "status": "FAILED",
  "startTime": "2026-02-20T10:16:00",
  "endTime": "2026-02-20T10:16:10",
  "executionTimeMs": 10000,
  "stepNumber": 4,
  "errorDetails": "License not found for product: DBX_V7.2.3",
  "retryCount": 2
}
```

## Testing with Mock Data

The mock data now generates realistic DiagCloud diagnostic sessions:
- Sessions have both "starting" and "shutdown" operations
- Workers follow DiagCloud saga patterns
- Errors reflect real DiagCloud failure scenarios
- Product IDs match DiagCloud naming conventions
- Users represent different roles in DiagCloud ecosystem

## Migration Notes

### Database Changes
If migrating from previous version:
1. Add new columns: `operation_id`, `product_id`, `user_id`
2. Rename `worker_name` to `message_type` (but stored as enum)
3. Update status enums in existing data

### API Compatibility
- Frontend should expect `messageType` instead of `workerName` in worker events
- Session statuses changed: `COMPLETED` → `CLOSED`, `FAILED` → `ERROR`, `IN_PROGRESS` → `RUNNING`
- New required fields in session creation: `operationId`, `productId`, `userId`

## Next Steps

1. **Test Mock Data**: Run quick-setup and verify new DiagCloud data structure
2. **Update Frontend**: Angular components need to handle new field names and statuses
3. **Azure Integration**: When available, map Application Insights custom events to these fields
4. **Production Migration**: Plan database schema migration for existing data

## Related Documents
- [README.md](README.md) - Full API documentation
- [TESTING_GUIDE.md](TESTING_GUIDE.md) - Testing with mock DiagCloud data
- [KQL_QUERIES.md](KQL_QUERIES.md) - Azure queries for DiagCloud events
- [PROJECT_STRUCTURE.md](PROJECT_STRUCTURE.md) - Code organization
