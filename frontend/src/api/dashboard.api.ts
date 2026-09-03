import { apiClient } from './axios';
import {
  ApiResponse,
  AttendanceOverviewResponse,
  DashboardSummaryResponse,
  DepartmentStatsResponse,
  PayrollSummaryResponse,
} from '@/types';

export const dashboardApi = {
  getSummary: async (): Promise<DashboardSummaryResponse> => {
    const res = await apiClient.get<ApiResponse<DashboardSummaryResponse>>('/api/v1/dashboard/summary');
    return res.data.data;
  },

  getAttendanceOverview: async (date?: string): Promise<AttendanceOverviewResponse> => {
    const res = await apiClient.get<ApiResponse<AttendanceOverviewResponse>>('/api/v1/dashboard/attendance-overview', {
      params: date ? { date } : {},
    });
    return res.data.data;
  },

  getDepartmentStats: async (): Promise<DepartmentStatsResponse[]> => {
    const res = await apiClient.get<ApiResponse<DepartmentStatsResponse[]>>('/api/v1/dashboard/department-stats');
    return res.data.data;
  },

  getPayrollSummary: async (): Promise<PayrollSummaryResponse[]> => {
    const res = await apiClient.get<ApiResponse<PayrollSummaryResponse[]>>('/api/v1/dashboard/payroll-summary');
    return res.data.data;
  },
};
