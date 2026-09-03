import { apiClient } from './axios';
import {
  ApiResponse,
  EmployeeCreateRequest,
  EmployeeResponse,
  EmployeeSearchParams,
  EmployeeUpdateRequest,
  ImportResultResponse,
  Page,
} from '@/types';

export const employeeApi = {
  search: async (params: EmployeeSearchParams): Promise<Page<EmployeeResponse>> => {
    const res = await apiClient.get<ApiResponse<Page<EmployeeResponse>>>('/api/v1/employees', { params });
    return res.data.data;
  },

  getById: async (id: number): Promise<EmployeeResponse> => {
    const res = await apiClient.get<ApiResponse<EmployeeResponse>>(`/api/v1/employees/${id}`);
    return res.data.data;
  },

  getMyProfile: async (): Promise<EmployeeResponse> => {
    const res = await apiClient.get<ApiResponse<EmployeeResponse>>('/api/v1/employees/me');
    return res.data.data;
  },

  create: async (data: EmployeeCreateRequest): Promise<EmployeeResponse> => {
    const res = await apiClient.post<ApiResponse<EmployeeResponse>>('/api/v1/employees', data);
    return res.data.data;
  },

  update: async (id: number, data: EmployeeUpdateRequest): Promise<EmployeeResponse> => {
    const res = await apiClient.put<ApiResponse<EmployeeResponse>>(`/api/v1/employees/${id}`, data);
    return res.data.data;
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete<ApiResponse<void>>(`/api/v1/employees/${id}`);
  },

  exportEmployees: async (params?: EmployeeSearchParams): Promise<Blob> => {
    const res = await apiClient.get('/api/v1/employees/export', {
      params,
      responseType: 'blob',
    });
    return res.data;
  },

  downloadTemplate: async (): Promise<Blob> => {
    const res = await apiClient.get('/api/v1/employees/template', {
      responseType: 'blob',
    });
    return res.data;
  },

  importEmployees: async (file: File): Promise<ImportResultResponse> => {
    const formData = new FormData();
    formData.append('file', file);
    const res = await apiClient.post<ApiResponse<ImportResultResponse>>('/api/v1/employees/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return res.data.data;
  },
};
