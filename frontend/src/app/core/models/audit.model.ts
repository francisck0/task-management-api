export enum AuditStatus {
  SUCCESS = 'SUCCESS',
  FAILURE = 'FAILURE'
}

export interface AuditLog {
  id: number;
  timestamp: string;
  username: string;
  action: string;
  resource: string;
  resourceId?: number;
  description: string;
  ipAddress?: string;
  userAgent?: string;
  status: AuditStatus;
  errorMessage?: string;
  requestParameters?: string;
  responseData?: string;
  correlationId?: string;
}

export interface AuditStatistics {
  totalOperations: number;
  successfulOperations: number;
  failedOperations: number;
  successRate: number;
  topActions: { [key: string]: number };
  topUsers: { [key: string]: number };
}
