import { apiClient } from './axios';

export const reportApi = {
  downloadAttendanceExcel: async (startDate?: string, endDate?: string, departmentId?: number): Promise<Blob> => {
    const res = await apiClient.get('/api/v1/reports/attendance/excel', {
      params: { startDate, endDate, departmentId },
      responseType: 'blob',
    });
    return res.data;
  },

  downloadPayslipPdf: async (payslipId: number): Promise<Blob> => {
    const res = await apiClient.get(`/api/v1/reports/payslips/${payslipId}/pdf`, {
      responseType: 'blob',
    });
    return res.data;
  },
};
