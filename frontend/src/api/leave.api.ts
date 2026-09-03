import { apiClient } from './axios';
import {
  ApiResponse,
  LeaveApprovalRequest,
  LeaveBalanceResponse,
  LeaveRequestCreateRequest,
  LeaveRequestResponse,
  LeaveSearchParams,
  LeaveTypeCreateRequest,
  LeaveTypeResponse,
  Page,
} from '@/types';

export const leaveApi = {
  getActiveTypes: async (): Promise<LeaveTypeResponse[]> => {
    const res = await apiClient.get<ApiResponse<LeaveTypeResponse[]>>('/api/v1/leaves/types');
    return res.data.data;
  },

  createType: async (data: LeaveTypeCreateRequest): Promise<LeaveTypeResponse> => {
    const res = await apiClient.post<ApiResponse<LeaveTypeResponse>>('/api/v1/leaves/types', data);
    return res.data.data;
  },

  getMyBalances: async (year?: number): Promise<LeaveBalanceResponse[]> => {
    const res = await apiClient.get<ApiResponse<LeaveBalanceResponse[]>>('/api/v1/leaves/balances/my', {
      params: year ? { year } : {},
    });
    return res.data.data;
  },

  getMyRequests: async (params?: { page?: number; size?: number }): Promise<Page<LeaveRequestResponse>> => {
    const res = await apiClient.get<ApiResponse<Page<LeaveRequestResponse>>>('/api/v1/leaves/requests/my', { params });
    return res.data.data;
  },

  searchRequests: async (params: LeaveSearchParams): Promise<Page<LeaveRequestResponse>> => {
    const res = await apiClient.get<ApiResponse<Page<LeaveRequestResponse>>>('/api/v1/leaves/requests', { params });
    return res.data.data;
  },

  createRequest: async (data: LeaveRequestCreateRequest): Promise<LeaveRequestResponse> => {
    const res = await apiClient.post<ApiResponse<LeaveRequestResponse>>('/api/v1/leaves/requests', data);
    return res.data.data;
  },

  approveRequest: async (id: number): Promise<LeaveRequestResponse> => {
    const res = await apiClient.put<ApiResponse<LeaveRequestResponse>>(`/api/v1/leaves/requests/${id}/approve`);
    return res.data.data;
  },

  rejectRequest: async (id: number, data?: LeaveApprovalRequest): Promise<LeaveRequestResponse> => {
    const res = await apiClient.put<ApiResponse<LeaveRequestResponse>>(`/api/v1/leaves/requests/${id}/reject`, data);
    return res.data.data;
  },

  cancelRequest: async (id: number): Promise<LeaveRequestResponse> => {
    const res = await apiClient.put<ApiResponse<LeaveRequestResponse>>(`/api/v1/leaves/requests/${id}/cancel`);
    return res.data.data;
  },
};
