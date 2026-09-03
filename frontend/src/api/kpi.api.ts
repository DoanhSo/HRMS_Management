import { apiClient } from './axios';
import {
  ApiResponse,
  Page,
  KpiCriteriaResponse,
  KpiCriteriaCreateRequest,
  KpiCriteriaUpdateRequest,
  KpiEvaluationResponse,
  KpiEvaluationCreateRequest,
  KpiSearchParams,
} from '@/types';

export const kpiApi = {
  // Criteria
  getCriteria: async (params?: { keyword?: string; departmentId?: number; active?: boolean; page?: number; size?: number }) => {
    const response = await apiClient.get<ApiResponse<Page<KpiCriteriaResponse>>>('/api/v1/kpi/criteria', { params });
    return response.data.data;
  },

  getActiveCriteria: async () => {
    const response = await apiClient.get<ApiResponse<KpiCriteriaResponse[]>>('/api/v1/kpi/criteria/active');
    return response.data.data;
  },

  createCriteria: async (data: KpiCriteriaCreateRequest) => {
    const response = await apiClient.post<ApiResponse<KpiCriteriaResponse>>('/api/v1/kpi/criteria', data);
    return response.data.data;
  },

  updateCriteria: async (id: number, data: KpiCriteriaUpdateRequest) => {
    const response = await apiClient.put<ApiResponse<KpiCriteriaResponse>>(`/api/v1/kpi/criteria/${id}`, data);
    return response.data.data;
  },

  deleteCriteria: async (id: number) => {
    const response = await apiClient.delete<ApiResponse<void>>(`/api/v1/kpi/criteria/${id}`);
    return response.data;
  },

  // Evaluations
  getEvaluations: async (params?: KpiSearchParams) => {
    const response = await apiClient.get<ApiResponse<Page<KpiEvaluationResponse>>>('/api/v1/kpi/evaluations', { params });
    return response.data.data;
  },

  getMyEvaluations: async (params?: { page?: number; size?: number }) => {
    const response = await apiClient.get<ApiResponse<Page<KpiEvaluationResponse>>>('/api/v1/kpi/evaluations/my', { params });
    return response.data.data;
  },

  getEvaluationById: async (id: number) => {
    const response = await apiClient.get<ApiResponse<KpiEvaluationResponse>>(`/api/v1/kpi/evaluations/${id}`);
    return response.data.data;
  },

  getEmployeeEvaluation: async (employeeId: number, year: number, month: number) => {
    const response = await apiClient.get<ApiResponse<KpiEvaluationResponse>>(`/api/v1/kpi/evaluations/employee/${employeeId}`, {
      params: { year, month },
    });
    return response.data.data;
  },

  createOrUpdateEvaluation: async (data: KpiEvaluationCreateRequest) => {
    const response = await apiClient.post<ApiResponse<KpiEvaluationResponse>>('/api/v1/kpi/evaluations', data);
    return response.data.data;
  },

  approveEvaluation: async (id: number, feedback?: string) => {
    const response = await apiClient.put<ApiResponse<KpiEvaluationResponse>>(`/api/v1/kpi/evaluations/${id}/approve`, null, {
      params: { feedback },
    });
    return response.data.data;
  },

  rejectEvaluation: async (id: number, feedback?: string) => {
    const response = await apiClient.put<ApiResponse<KpiEvaluationResponse>>(`/api/v1/kpi/evaluations/${id}/reject`, null, {
      params: { feedback },
    });
    return response.data.data;
  },
};
