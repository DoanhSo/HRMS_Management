import { apiClient } from './axios';
import {
  ApiResponse,
  Page,
  PageableParams,
  PayrollPeriodCreateRequest,
  PayrollPeriodResponse,
  PayslipResponse,
} from '@/types';

export const payrollApi = {
  getPeriods: async (params?: PageableParams): Promise<Page<PayrollPeriodResponse>> => {
    const res = await apiClient.get<ApiResponse<Page<PayrollPeriodResponse>>>('/api/v1/payroll/periods', { params });
    return res.data.data;
  },

  createPeriod: async (data: PayrollPeriodCreateRequest): Promise<PayrollPeriodResponse> => {
    const res = await apiClient.post<ApiResponse<PayrollPeriodResponse>>('/api/v1/payroll/periods', data);
    return res.data.data;
  },

  calculatePeriod: async (id: number): Promise<void> => {
    await apiClient.post<ApiResponse<void>>(`/api/v1/payroll/periods/${id}/calculate`);
  },

  approvePeriod: async (id: number): Promise<void> => {
    await apiClient.put<ApiResponse<void>>(`/api/v1/payroll/periods/${id}/approve`);
  },

  getMyPayslips: async (params?: PageableParams): Promise<Page<PayslipResponse>> => {
    const res = await apiClient.get<ApiResponse<Page<PayslipResponse>>>('/api/v1/payroll/my-records', { params });
    return res.data.data;
  },

  searchPayslips: async (params?: PageableParams & { periodId?: number; keyword?: string; departmentId?: number }): Promise<Page<PayslipResponse>> => {
    const res = await apiClient.get<ApiResponse<Page<PayslipResponse>>>('/api/v1/payroll/payslips', { params });
    return res.data.data;
  },

  getPayslipById: async (id: number): Promise<PayslipResponse> => {
    const res = await apiClient.get<ApiResponse<PayslipResponse>>(`/api/v1/payroll/payslips/${id}`);
    return res.data.data;
  },

  exportPayrollPeriodExcel: async (periodId: number): Promise<Blob> => {
    const res = await apiClient.get(`/api/v1/payroll/periods/${periodId}/export-excel`, {
      responseType: 'blob',
    });
    return res.data;
  },
};
