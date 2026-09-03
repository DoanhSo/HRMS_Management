import { apiClient } from './axios';
import {
  ApiResponse,
  Page,
  SalaryScaleResponse,
  SalaryScaleCreateRequest,
  SalaryScaleUpdateRequest,
  SalaryScaleSearchParams,
} from '@/types';

export const salaryScaleApi = {
  getSalaryScales: async (params?: SalaryScaleSearchParams) => {
    const response = await apiClient.get<ApiResponse<Page<SalaryScaleResponse>>>('/api/v1/salary-scales', { params });
    return response.data.data;
  },

  getActiveSalaryScales: async () => {
    const response = await apiClient.get<ApiResponse<SalaryScaleResponse[]>>('/api/v1/salary-scales/active');
    return response.data.data;
  },

  getSalaryScaleById: async (id: number) => {
    const response = await apiClient.get<ApiResponse<SalaryScaleResponse>>(`/api/v1/salary-scales/${id}`);
    return response.data.data;
  },

  createSalaryScale: async (data: SalaryScaleCreateRequest) => {
    const response = await apiClient.post<ApiResponse<SalaryScaleResponse>>('/api/v1/salary-scales', data);
    return response.data.data;
  },

  updateSalaryScale: async (id: number, data: SalaryScaleUpdateRequest) => {
    const response = await apiClient.put<ApiResponse<SalaryScaleResponse>>(`/api/v1/salary-scales/${id}`, data);
    return response.data.data;
  },

  deleteSalaryScale: async (id: number) => {
    const response = await apiClient.delete<ApiResponse<void>>(`/api/v1/salary-scales/${id}`);
    return response.data;
  },
};
