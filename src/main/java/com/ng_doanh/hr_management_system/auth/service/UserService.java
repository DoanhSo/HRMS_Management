package com.ng_doanh.hr_management_system.auth.service;

import com.ng_doanh.hr_management_system.auth.dto.request.AdminResetPasswordRequest;
import com.ng_doanh.hr_management_system.auth.dto.request.UserCreateRequest;
import com.ng_doanh.hr_management_system.auth.dto.request.UserUpdateRequest;
import com.ng_doanh.hr_management_system.auth.dto.response.RoleResponse;
import com.ng_doanh.hr_management_system.auth.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    Page<UserResponse> searchUsers(String keyword, String role, Boolean enabled, Pageable pageable);

    UserResponse getUserById(Long id);

    UserResponse createUser(UserCreateRequest request);

    UserResponse updateUser(Long id, UserUpdateRequest request);

    void resetPassword(Long id, AdminResetPasswordRequest request);

    UserResponse toggleUserStatus(Long id, boolean enabled);

    void deleteUser(Long id);

    List<RoleResponse> getAllRoles();
}
