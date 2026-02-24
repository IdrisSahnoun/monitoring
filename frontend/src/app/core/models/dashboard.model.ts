// Dashboard Statistics
export interface DashboardStats {
  totalSessions: number;
  activeSessions: number;
  completedSessions: number;
  failedSessions: number;
  overallSuccessRate: number;
  averageSessionDuration: number;
  workerMetrics: WorkerMetric[];
  sessionsByStatus: StatusCount[];
  sessionsByHour: HourlyCount[];
}

// Worker Metrics
export interface WorkerMetric {
  messageType: string;
  totalExecutions: number;
  successfulExecutions: number;
  failedExecutions: number;
  averageExecutionTime: number;
  successRate: number;
}

// Status Count
export interface StatusCount {
  status: string;
  count: number;
}

// Hourly Count
export interface HourlyCount {
  hour: string;
  count: number;
}

// Real-time Stats
export interface RealtimeStats {
  activeSessions: number;
  completedToday: number;
  failedToday: number;
  timestamp: string;
}

// Mock Data Stats
export interface MockDataStats {
  totalSessions: number;
  totalEvents: number;
  completedSessions: number;
  failedSessions: number;
  activeSessions: number;
}

// Date Range Filter
export interface DateRangeFilter {
  startDate?: string;
  endDate?: string;
}
