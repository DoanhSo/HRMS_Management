import { apiClient } from './axios';
import {
  ApiResponse,
  AttendanceManualRequest,
  AttendanceResponse,
  AttendanceSearchParams,
  CheckInRequest,
  CheckOutRequest,
  ImportResultResponse,
  Page,
} from '@/types';

export const attendanceApi = {
  checkIn: async (data?: CheckInRequest): Promise<AttendanceResponse> => {
    const res = await apiClient.post<ApiResponse<AttendanceResponse>>('/api/v1/attendances/check-in', data || {});
    return res.data.data;
  },

  checkOut: async (data?: CheckOutRequest): Promise<AttendanceResponse> => {
    const res = await apiClient.post<ApiResponse<AttendanceResponse>>('/api/v1/attendances/check-out', data || {});
    return res.data.data;
  },

  getMyHistory: async (params?: { startDate?: string; endDate?: string; page?: number; size?: number }): Promise<Page<AttendanceResponse>> => {
    const res = await apiClient.get<ApiResponse<Page<AttendanceResponse>>>('/api/v1/attendances/my-history', { params });
    return res.data.data;
  },

  search: async (params: AttendanceSearchParams): Promise<Page<AttendanceResponse>> => {
    const res = await apiClient.get<ApiResponse<Page<AttendanceResponse>>>('/api/v1/attendances', { params });
    return res.data.data;
  },

  manualEntry: async (data: AttendanceManualRequest): Promise<AttendanceResponse> => {
    const res = await apiClient.post<ApiResponse<AttendanceResponse>>('/api/v1/attendances/manual', data);
    return res.data.data;
  },

  exportAttendance: async (params?: { startDate?: string; endDate?: string; departmentId?: number }): Promise<Blob> => {
    const res = await apiClient.get('/api/v1/attendances/export', {
      params,
      responseType: 'blob',
    });
    return res.data;
  },

  downloadTemplate: async (): Promise<Blob> => {
    const res = await apiClient.get('/api/v1/attendances/template', {
      responseType: 'blob',
    });
    return res.data;
  },

  importAttendance: async (file: File): Promise<ImportResultResponse> => {
    const formData = new FormData();
    formData.append('file', file);
    const res = await apiClient.post<ApiResponse<ImportResultResponse>>('/api/v1/attendances/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return res.data.data;
  },
};
