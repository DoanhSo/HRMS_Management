import { apiClient } from './axios';
import {
  ApiResponse,
  Page,
  PageableParams,
  PositionCreateRequest,
  PositionResponse,
  PositionUpdateRequest,
} from '@/types';

export const positionApi = {
  getAllActive: async (): Promise<PositionResponse[]> => {
    const res = await apiClient.get<ApiResponse<PositionResponse[]>>('/api/v1/positions/active');
    return res.data.data;
  },

  getByDepartment: async (departmentId: number): Promise<PositionResponse[]> => {
    const res = await apiClient.get<ApiResponse<PositionResponse[]>>(`/api/v1/positions/department/${departmentId}`);
    return res.data.data;
  },

  search: async (params?: PageableParams & { keyword?: string; departmentId?: number; active?: boolean }): Promise<Page<PositionResponse>> => {
    const res = await apiClient.get<ApiResponse<Page<PositionResponse>>>('/api/v1/positions', { params });
    return res.data.data;
  },

  getById: async (id: number): Promise<PositionResponse> => {
    const res = await apiClient.get<ApiResponse<PositionResponse>>(`/api/v1/positions/${id}`);
    return res.data.data;
  },

  create: async (data: PositionCreateRequest): Promise<PositionResponse> => {
    const res = await apiClient.post<ApiResponse<PositionResponse>>('/api/v1/positions', data);
    return res.data.data;
  },

  update: async (id: number, data: PositionUpdateRequest): Promise<PositionResponse> => {
    const res = await apiClient.put<ApiResponse<PositionResponse>>(`/api/v1/positions/${id}`, data);
    return res.data.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete<ApiResponse<void>>(`/api/v1/positions/${id}`);
  },

  exportPositions: async (): Promise<Blob> => {
    const res = await apiClient.get('/api/v1/positions/export', {
      responseType: 'blob',
    });
    return res.data;
  },
};
