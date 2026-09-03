import { apiClient } from './axios';
import {
  ApiResponse,
  DepartmentCreateRequest,
  DepartmentResponse,
  DepartmentUpdateRequest,
  Page,
  PageableParams,
} from '@/types';

export const departmentApi = {
  getAllActive: async (): Promise<DepartmentResponse[]> => {
    const res = await apiClient.get<ApiResponse<DepartmentResponse[]>>('/api/v1/departments/active');
    return res.data.data;
  },

  search: async (params?: PageableParams & { keyword?: string; active?: boolean }): Promise<Page<DepartmentResponse>> => {
    const res = await apiClient.get<ApiResponse<Page<DepartmentResponse>>>('/api/v1/departments', { params });
    return res.data.data;
  },

  getById: async (id: number): Promise<DepartmentResponse> => {
    const res = await apiClient.get<ApiResponse<DepartmentResponse>>(`/api/v1/departments/${id}`);
    return res.data.data;
  },

  create: async (data: DepartmentCreateRequest): Promise<DepartmentResponse> => {
    const res = await apiClient.post<ApiResponse<DepartmentResponse>>('/api/v1/departments', data);
    return res.data.data;
  },

  update: async (id: number, data: DepartmentUpdateRequest): Promise<DepartmentResponse> => {
    const res = await apiClient.put<ApiResponse<DepartmentResponse>>(`/api/v1/departments/${id}`, data);
    return res.data.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete<ApiResponse<void>>(`/api/v1/departments/${id}`);
  },

  exportDepartments: async (): Promise<Blob> => {
    const res = await apiClient.get('/api/v1/departments/export', {
      responseType: 'blob',
    });
    return res.data;
  },
};
