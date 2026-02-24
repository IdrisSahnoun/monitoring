// DiagCloud Worker Message Types (Saga Workers)
export enum MessageType {
  // Starting Saga Workers
  BOOK_VCI_SERVER = 'BOOK_VCI_SERVER',
  CREATE_PRODUCT_INSTANCE = 'CREATE_PRODUCT_INSTANCE',
  DETERMINE_PRODUCT_VERSION = 'DETERMINE_PRODUCT_VERSION',
  SEARCH_LICENSE = 'SEARCH_LICENSE',
  CONFIGURE_SESSION = 'CONFIGURE_SESSION',
  INITIALIZE_DIAGNOSTICS = 'INITIALIZE_DIAGNOSTICS',
  START_COMMUNICATION = 'START_COMMUNICATION',
  VALIDATE_CONNECTION = 'VALIDATE_CONNECTION',
  
  // Shutdown Saga Workers
  STOP_COMMUNICATION = 'STOP_COMMUNICATION',
  SAVE_SESSION_DATA = 'SAVE_SESSION_DATA',
  CLEANUP_RESOURCES = 'CLEANUP_RESOURCES',
  RELEASE_VCI_SERVER = 'RELEASE_VCI_SERVER',
  SEND_NOTIFICATION = 'SEND_NOTIFICATION',
  
  // Common Workers
  HEALTH_CHECK = 'HEALTH_CHECK',
  LOG_EVENT = 'LOG_EVENT',
  UPDATE_STATUS = 'UPDATE_STATUS'
}

// Worker Status Enum
export enum WorkerStatus {
  STARTED = 'STARTED',
  RUNNING = 'RUNNING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  SKIPPED = 'SKIPPED',
  RETRYING = 'RETRYING'
}

// Worker Event Model
export interface WorkerEvent {
  eventId?: string;
  sessionId: string;
  messageType: MessageType;
  status: WorkerStatus;
  startTime: string;
  endTime?: string;
  executionTimeMs?: number;
  stepNumber?: number;
  inputData?: string;
  outputData?: string;
  errorDetails?: string;
  retryCount?: number;
}

// Performance Metrics
export interface PerformanceMetrics {
  averageExecutionTime: number;
  minExecutionTime: number;
  maxExecutionTime: number;
  p95ExecutionTime: number;
  totalExecutions: number;
  successfulExecutions: number;
  failedExecutions: number;
  successRate: number;
}
