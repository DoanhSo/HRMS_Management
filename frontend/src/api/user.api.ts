import { apiClient } from './axios';
import {
  AdminResetPasswordRequest,
  ApiResponse,
  Page,
  RoleResponse,
  UserCreateRequest,
  UserManagementResponse,
  UserSearchParams,
  UserUpdateRequest,
} from '@/types';

export const userApi = {
  searchUsers: async (params?: UserSearchParams): Promise<Page<UserManagementResponse>> => {
    const res = await apiClient.get<ApiResponse<Page<UserManagementResponse>>>('/api/v1/users', { params });
    return res.data.data;
  },

  getAllRoles: async (): Promise<RoleResponse[]> => {
    const res = await apiClient.get<ApiResponse<RoleResponse[]>>('/api/v1/users/roles');
    return res.data.data;
  },

  getUserById: async (id: number): Promise<UserManagementResponse> => {
    const res = await apiClient.get<ApiResponse<UserManagementResponse>>(`/api/v1/users/${id}`);
    return res.data.data;
  },

  createUser: async (data: UserCreateRequest): Promise<UserManagementResponse> => {
    const res = await apiClient.post<ApiResponse<UserManagementResponse>>('/api/v1/users', data);
    return res.data.data;
  },

  updateUser: async (id: number, data: UserUpdateRequest): Promise<UserManagementResponse> => {
    const res = await apiClient.put<ApiResponse<UserManagementResponse>>(`/api/v1/users/${id}`, data);
    return res.data.data;
  },

  resetPassword: async (id: number, data: AdminResetPasswordRequest): Promise<void> => {
    await apiClient.put<ApiResponse<void>>(`/api/v1/users/${id}/reset-password`, data);
  },

  toggleUserStatus: async (id: number, enabled: boolean): Promise<UserManagementResponse> => {
    const res = await apiClient.put<ApiResponse<UserManagementResponse>>(`/api/v1/users/${id}/status`, null, {
      params: { enabled },
    });
    return res.data.data;
  },

  deleteUser: async (id: number): Promise<void> => {
    await apiClient.delete<ApiResponse<void>>(`/api/v1/users/${id}`);
  },
};
