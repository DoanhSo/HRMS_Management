package com.ng_doanh.hr_management_system.auth.controller;

import com.ng_doanh.hr_management_system.auth.dto.request.ChangePasswordRequest;
import com.ng_doanh.hr_management_system.auth.dto.request.LoginRequest;
import com.ng_doanh.hr_management_system.auth.dto.request.RefreshTokenRequest;
import com.ng_doanh.hr_management_system.auth.dto.response.TokenResponse;
import com.ng_doanh.hr_management_system.auth.dto.response.UserResponse;
import com.ng_doanh.hr_management_system.auth.service.AuthService;
import com.ng_doanh.hr_management_system.common.constant.ApiPaths;
import com.ng_doanh.hr_management_system.common.dto.ApiResponse;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import com.ng_doanh.hr_management_system.common.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.AUTH_BASE)
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication & Authorization Management APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login to system", description = "Authenticates user and returns JWT access token & refresh token")
    public ApiResponse<TokenResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        TokenResponse tokenResponse = authService.login(request);
        return ApiResponse.success(ResponseCode.SUCCESS, tokenResponse, httpServletRequest.getRequestURI());
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token", description = "Generates a new access token using a valid refresh token")
    public ApiResponse<TokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpServletRequest
    ) {
        TokenResponse tokenResponse = authService.refreshToken(request);
        return ApiResponse.success(ResponseCode.SUCCESS, tokenResponse, httpServletRequest.getRequestURI());
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Revokes user refresh token")
    public ApiResponse<Void> logout(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpServletRequest
    ) {
        authService.logout(request);
        return ApiResponse.success(ResponseCode.SUCCESS, httpServletRequest.getRequestURI());
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Changes password for current logged in user")
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpServletRequest
    ) {
        authService.changePassword(userDetails.getId(), request);
        return ApiResponse.success(ResponseCode.SUCCESS, httpServletRequest.getRequestURI());
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", description = "Returns profile details of the authenticated user")
    public ApiResponse<UserResponse> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpServletRequest
    ) {
        UserResponse userResponse = authService.getCurrentUser(userDetails.getUsername());
        return ApiResponse.success(ResponseCode.SUCCESS, userResponse, httpServletRequest.getRequestURI());
    }
}
