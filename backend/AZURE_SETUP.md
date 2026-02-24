# Azure Application Insights Setup Guide

This guide shows you how to get your Azure Application Insights credentials and configure the backend.

## Step 1: Get Your Azure Credentials

### Navigate to Azure Portal
1. Go to [https://portal.azure.com](https://portal.azure.com)
2. Sign in with your Azure account
3. In the search bar, type "Application Insights"
4. Select your Application Insights resource

### Get the Credentials

#### 1. Instrumentation Key
- In your Application Insights resource, look in the left sidebar
- Click on **"Overview"** or **"Properties"**
- Copy the **Instrumentation Key** (format: `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`)

#### 2. Connection String
- In the same Overview page
- Look for **Connection String**
- Copy the entire string (format: `InstrumentationKey=xxx;IngestionEndpoint=https://...`)

#### 3. Workspace ID
- In the left sidebar, find **"Logs"** section
- Click on it to open Log Analytics
- In the top bar, you'll see your workspace name
- Or go to **"Properties"** and find **Workspace ID**

## Step 2: Configure Backend

### Option A: Environment Variables (Recommended for Production)

**Windows PowerShell:**
```powershell
$env:APPINSIGHTS_INSTRUMENTATION_KEY="your-instrumentation-key"
$env:APPINSIGHTS_CONNECTION_STRING="your-connection-string"
$env:APPINSIGHTS_WORKSPACE_ID="your-workspace-id"
```

**Windows Command Prompt:**
```cmd
set APPINSIGHTS_INSTRUMENTATION_KEY=your-instrumentation-key
set APPINSIGHTS_CONNECTION_STRING=your-connection-string
set APPINSIGHTS_WORKSPACE_ID=your-workspace-id
```

**Linux/Mac:**
```bash
export APPINSIGHTS_INSTRUMENTATION_KEY="your-instrumentation-key"
export APPINSIGHTS_CONNECTION_STRING="your-connection-string"
export APPINSIGHTS_WORKSPACE_ID="your-workspace-id"
```

### Option B: Application Configuration File

Edit `backend/src/main/resources/application.yml`:

```yaml
azure:
  application-insights:
    instrumentation-key: ${APPINSIGHTS_INSTRUMENTATION_KEY:your-actual-key-here}
    connection-string: ${APPINSIGHTS_CONNECTION_STRING:InstrumentationKey=xxx...}
    workspace-id: ${APPINSIGHTS_WORKSPACE_ID:your-workspace-id}
```

**⚠️ Security Warning:** Never commit real credentials to Git! Use environment variables or `.env` files (added to `.gitignore`).

## Step 3: Verify Connection

### Test the Health Endpoint

```powershell
# Start the application
cd backend
mvn spring-boot:run

# In another terminal, test the health check
curl http://localhost:8080/api/actuator/health
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "applicationInsights": {
      "status": "UP",
      "details": {
        "instrumentationKey": "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx",
        "connected": true
      }
    }
  }
}
```

### Test KQL Query Manually

Go to Azure Portal → Your Application Insights → Logs, and run:

```kql
// Test query: Get recent custom events
customEvents
| where timestamp > ago(24h)
| where name == "DiagnosticSession" or name == "WorkerExecution"
| take 10
```

If you see data, your Application Insights is collecting events!

## Step 4: Verify DiagCloud Events

Check if DiagCloud is sending events with the correct structure:

```kql
// Check for DiagCloud session events
customEvents
| where name == "DiagnosticSession"
| where timestamp > ago(1h)
| project 
    timestamp,
    sessionId = tostring(customDimensions.instanceId),
    operationId = tostring(customDimensions.operationId),
    productId = tostring(customDimensions.productId),
    userId = tostring(customDimensions.userId),
    status = tostring(customDimensions.status)
| take 20
```

```kql
// Check for worker execution events
customEvents
| where name == "WorkerExecution"
| where timestamp > ago(1h)
| project 
    timestamp,
    sessionId = tostring(customDimensions.instanceId),
    messageType = tostring(customDimensions.messageType),
    status = tostring(customDimensions.status),
    duration = todouble(customDimensions.duration)
| take 20
```

## Expected Event Structure

### DiagnosticSession Event
```json
{
  "name": "DiagnosticSession",
  "timestamp": "2026-02-24T10:15:30Z",
  "customDimensions": {
    "instanceId": "INST-A1B2C3D4",
    "operationId": "starting",
    "productId": "DBX_V7.2.3",
    "userId": "operator.oi@company.com",
    "vehicleId": "VEH-001",
    "status": "CLOSED",
    "durationMs": 195000
  }
}
```

### WorkerExecution Event
```json
{
  "name": "WorkerExecution",
  "timestamp": "2026-02-24T10:15:35Z",
  "customDimensions": {
    "instanceId": "INST-A1B2C3D4",
    "messageType": "BOOK_VCI_SERVER",
    "status": "COMPLETED",
    "duration": 5234,
    "stepNumber": 1
  }
}
```

## Troubleshooting

### Issue: No Data in Application Insights
**Solution:** 
- Check if DiagCloud core-services is instrumented with Application Insights SDK
- Verify the instrumentation key in DiagCloud configuration
- Wait 2-5 minutes for data to appear (Application Insights has a delay)

### Issue: "Unauthorized" or "Forbidden" Errors
**Solution:**
- Verify your Azure credentials are correct
- Check if your account has "Reader" role on the Application Insights resource
- Go to Azure Portal → Application Insights → Access control (IAM) → Check your permissions

### Issue: Backend Cannot Connect to Azure
**Solution:**
- Check your internet connection
- Verify firewall isn't blocking Azure endpoints
- Test connection: `curl https://dc.services.visualstudio.com/v2/track`

### Issue: Wrong Data Format
**Solution:**
- DiagCloud might be using different field names
- Run KQL query to see actual field names:
  ```kql
  customEvents
  | where timestamp > ago(1h)
  | take 1
  | project customDimensions
  ```
- Update `ApplicationInsightsService.java` to match the actual field names

## Next Steps

1. ✅ Get Azure credentials
2. ✅ Configure backend with credentials
3. ✅ Test connection with health endpoint
4. ✅ Verify DiagCloud events structure
5. ⏳ Implement KQL queries in `ApplicationInsightsService.java`
6. ⏳ Test backend APIs with real Azure data
7. ⏳ Build Angular frontend

## Related Documents
- [README.md](README.md) - Main documentation
- [KQL_QUERIES.md](KQL_QUERIES.md) - Sample KQL queries for monitoring
- [DIAGCLOUD_UPDATES.md](DIAGCLOUD_UPDATES.md) - DiagCloud domain model
