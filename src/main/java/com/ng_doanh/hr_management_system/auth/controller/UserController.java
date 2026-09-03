package com.ng_doanh.hr_management_system.auth.controller;

import com.ng_doanh.hr_management_system.auth.dto.request.AdminResetPasswordRequest;
import com.ng_doanh.hr_management_system.auth.dto.request.UserCreateRequest;
import com.ng_doanh.hr_management_system.auth.dto.request.UserUpdateRequest;
import com.ng_doanh.hr_management_system.auth.dto.response.RoleResponse;
import com.ng_doanh.hr_management_system.auth.dto.response.UserResponse;
import com.ng_doanh.hr_management_system.auth.service.UserService;
import com.ng_doanh.hr_management_system.common.constant.ApiPaths;
import com.ng_doanh.hr_management_system.common.constant.SecurityConstants;
import com.ng_doanh.hr_management_system.common.dto.ApiResponse;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.USERS_BASE)
@RequiredArgsConstructor
@PreAuthorize(SecurityConstants.HAS_ROLE_ADMIN)
@Tag(name = "User Management", description = "Admin APIs for managing system user accounts, roles, and security status")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Search users", description = "Searches and lists system users with filters (Admin only)")
    public ApiResponse<Page<UserResponse>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest httpServletRequest
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserResponse> response = userService.searchUsers(keyword, role, enabled, pageable);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @GetMapping("/roles")
    @Operation(summary = "Get all roles", description = "Returns list of all available system roles (Admin only)")
    public ApiResponse<List<RoleResponse>> getAllRoles(HttpServletRequest httpServletRequest) {
        List<RoleResponse> response = userService.getAllRoles();
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @GetMapping("/{id:[0-9]+}")
    @Operation(summary = "Get user by ID", description = "Fetches user details by database ID (Admin only)")
    public ApiResponse<UserResponse> getUserById(
            @PathVariable Long id,
            HttpServletRequest httpServletRequest
    ) {
        UserResponse response = userService.getUserById(id);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @PostMapping
    @Operation(summary = "Create new user", description = "Creates a new system user account (Admin only)")
    public ApiResponse<UserResponse> createUser(
            @Valid @RequestBody UserCreateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        UserResponse response = userService.createUser(request);
        return ApiResponse.success(ResponseCode.CREATED, response, httpServletRequest.getRequestURI());
    }

    @PutMapping("/{id:[0-9]+}")
    @Operation(summary = "Update user", description = "Updates user email, roles, or status (Admin only)")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        UserResponse response = userService.updateUser(id, request);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @PutMapping("/{id:[0-9]+}/reset-password")
    @Operation(summary = "Reset user password", description = "Admin forces reset of a user password (Admin only)")
    public ApiResponse<Void> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody AdminResetPasswordRequest request,
            HttpServletRequest httpServletRequest
    ) {
        userService.resetPassword(id, request);
        return ApiResponse.success(ResponseCode.SUCCESS, httpServletRequest.getRequestURI());
    }

    @PutMapping("/{id:[0-9]+}/status")
    @Operation(summary = "Toggle user status", description = "Enables or disables user account (Admin only)")
    public ApiResponse<UserResponse> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled,
            HttpServletRequest httpServletRequest
    ) {
        UserResponse response = userService.toggleUserStatus(id, enabled);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @DeleteMapping("/{id:[0-9]+}")
    @Operation(summary = "Delete user", description = "Deletes user account (Admin only)")
    public ApiResponse<Void> deleteUser(
            @PathVariable Long id,
            HttpServletRequest httpServletRequest
    ) {
        userService.deleteUser(id);
        return ApiResponse.success(ResponseCode.DELETED, httpServletRequest.getRequestURI());
    }
}
