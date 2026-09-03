import { apiClient } from './axios';
import { ApiResponse, NotificationResponse, Page, PageableParams } from '@/types';

export const notificationApi = {
  getMyNotifications: async (params?: PageableParams): Promise<Page<NotificationResponse>> => {
    const res = await apiClient.get<ApiResponse<Page<NotificationResponse>>>('/api/v1/notifications', { params });
    return res.data.data;
  },

  getUnreadCount: async (): Promise<number> => {
    const res = await apiClient.get<ApiResponse<number>>('/api/v1/notifications/unread-count');
    return res.data.data;
  },

  markAsRead: async (id: number): Promise<void> => {
    await apiClient.put<ApiResponse<void>>(`/api/v1/notifications/${id}/read`);
  },

  markAllAsRead: async (): Promise<void> => {
    await apiClient.put<ApiResponse<void>>('/api/v1/notifications/read-all');
  },
};
