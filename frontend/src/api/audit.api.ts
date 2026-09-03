import { apiClient } from './axios';
import { ApiResponse, AuditLog, Page, PageableParams } from '@/types';

export interface AuditSearchParams extends PageableParams {
  username?: string;
  action?: string;
  entityName?: string;
  startDate?: string;
  endDate?: string;
}

export const auditApi = {
  search: async (params: AuditSearchParams): Promise<Page<AuditLog>> => {
    const res = await apiClient.get<ApiResponse<Page<AuditLog>>>('/api/v1/audit-logs', { params });
    return res.data.data;
  },
};
