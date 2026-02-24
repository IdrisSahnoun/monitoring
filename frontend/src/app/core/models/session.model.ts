// DiagCloud Session Status Enum
export enum SessionStatus {
  INITIALIZING = 'INITIALIZING',
  STARTING = 'STARTING',
  RUNNING = 'RUNNING',
  CLOSED = 'CLOSED',
  CANCELLED = 'CANCELLED',
  ERROR = 'ERROR',
  SHUTDOWN_REQUESTED = 'SHUTDOWN_REQUESTED'
}

// Diagnostic Session Model
export interface DiagnosticSession {
  id?: string;
  sessionId: string;  // instanceId in DiagCloud
  operationId: string;  // 'starting' or 'shutdown'
  vehicleId: string;
  vehicleVin?: string;
  productId: string;  // Product image (e.g., DBX_V7.2.3)
  userId: string;  // User who initiated
  diagnosticType?: string;
  status: SessionStatus;
  startTime: string;
  endTime?: string;
  durationMs?: number;
  currentWorker?: string;
  completedSteps?: number;
  totalSteps?: number;
  errorMessage?: string;
  createdAt?: string;
  updatedAt?: string;
}

// Session Filter for Search
export interface SessionFilter {
  sessionId?: string;
  vehicleId?: string;
  status?: SessionStatus;
  messageType?: string;
  startDate?: string;
  endDate?: string;
  productId?: string;
  userId?: string;
  operationId?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

// Paginated Response
export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
