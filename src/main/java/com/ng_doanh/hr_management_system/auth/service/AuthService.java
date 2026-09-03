package com.ng_doanh.hr_management_system.auth.service;

import com.ng_doanh.hr_management_system.auth.dto.request.ChangePasswordRequest;
import com.ng_doanh.hr_management_system.auth.dto.request.LoginRequest;
import com.ng_doanh.hr_management_system.auth.dto.request.RefreshTokenRequest;
import com.ng_doanh.hr_management_system.auth.dto.response.TokenResponse;
import com.ng_doanh.hr_management_system.auth.dto.response.UserResponse;

public interface AuthService {

    TokenResponse login(LoginRequest request);

    TokenResponse refreshToken(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    UserResponse getCurrentUser(String username);
}
