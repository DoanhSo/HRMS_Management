import { apiClient } from './axios';
import { ApiResponse, ChangePasswordRequest, LoginRequest, RefreshTokenRequest, TokenResponse, UserResponse } from '@/types';

export const authApi = {
  login: async (data: LoginRequest): Promise<TokenResponse> => {
    const res = await apiClient.post<ApiResponse<TokenResponse>>('/api/v1/auth/login', data);
    return res.data.data;
  },

  refreshToken: async (data: RefreshTokenRequest): Promise<TokenResponse> => {
    const res = await apiClient.post<ApiResponse<TokenResponse>>('/api/v1/auth/refresh-token', data);
    return res.data.data;
  },

  logout: async (refreshToken: string): Promise<void> => {
    await apiClient.post<ApiResponse<void>>('/api/v1/auth/logout', { refreshToken });
  },

  changePassword: async (data: ChangePasswordRequest): Promise<void> => {
    await apiClient.post<ApiResponse<void>>('/api/v1/auth/change-password', data);
  },

  getCurrentUser: async (): Promise<UserResponse> => {
    const res = await apiClient.get<ApiResponse<UserResponse>>('/api/v1/auth/me');
    return res.data.data;
  },
};
