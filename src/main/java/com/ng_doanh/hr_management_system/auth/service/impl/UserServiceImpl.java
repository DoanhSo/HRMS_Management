package com.ng_doanh.hr_management_system.auth.service.impl;

import com.ng_doanh.hr_management_system.audit.annotation.Audited;
import com.ng_doanh.hr_management_system.auth.dto.request.AdminResetPasswordRequest;
import com.ng_doanh.hr_management_system.auth.dto.request.UserCreateRequest;
import com.ng_doanh.hr_management_system.auth.dto.request.UserUpdateRequest;
import com.ng_doanh.hr_management_system.auth.dto.response.RoleResponse;
import com.ng_doanh.hr_management_system.auth.dto.response.UserResponse;
import com.ng_doanh.hr_management_system.auth.entity.Permission;
import com.ng_doanh.hr_management_system.auth.entity.Role;
import com.ng_doanh.hr_management_system.auth.entity.User;
import com.ng_doanh.hr_management_system.auth.repository.RoleRepository;
import com.ng_doanh.hr_management_system.auth.repository.UserRepository;
import com.ng_doanh.hr_management_system.auth.service.UserService;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import com.ng_doanh.hr_management_system.common.exception.BusinessException;
import com.ng_doanh.hr_management_system.employee.entity.Employee;
import com.ng_doanh.hr_management_system.employee.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String keyword, String role, Boolean enabled, Pageable pageable) {
        log.info("Searching users with keyword: {}, role: {}, enabled: {}", keyword, role, enabled);
        Page<User> users = userRepository.searchUsers(keyword, role, enabled, pageable);
        return users.map(this::mapToUserResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.info("Fetching user by ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));
        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    @Audited(action = "CREATE_USER", entity = "User")
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Creating new user with username: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ResponseCode.DUPLICATE_RESOURCE);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ResponseCode.DUPLICATE_RESOURCE);
        }

        Set<Role> roles = new HashSet<>();
        for (String roleName : request.getRoles()) {
            Role role = roleRepository.findByName(roleName)
                    .or(() -> roleRepository.findByName("ROLE_" + roleName))
                    .or(() -> roleRepository.findByName(roleName.replace("ROLE_", "")))
                    .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));
            roles.add(role);
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .accountNonLocked(true)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);

        // Link with employee if requested
        if (request.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));
            employee.setUser(savedUser);
            employeeRepository.save(employee);
        }

        return mapToUserResponse(savedUser);
    }

    @Override
    @Transactional
    @Audited(action = "UPDATE_USER", entity = "User")
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        log.info("Updating user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ResponseCode.DUPLICATE_RESOURCE);
        }

        user.setEmail(request.getEmail());

        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        if (request.getAccountNonLocked() != null) {
            user.setAccountNonLocked(request.getAccountNonLocked());
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (String roleName : request.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .or(() -> roleRepository.findByName("ROLE_" + roleName))
                        .or(() -> roleRepository.findByName(roleName.replace("ROLE_", "")))
                        .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));
                roles.add(role);
            }
            user.setRoles(roles);
        }

        User updatedUser = userRepository.save(user);

        // Update employee linkage
        if (request.getEmployeeId() != null) {
            Optional<Employee> currentEmp = employeeRepository.findByUserId(id);
            if (currentEmp.isPresent() && !currentEmp.get().getId().equals(request.getEmployeeId())) {
                currentEmp.get().setUser(null);
                employeeRepository.save(currentEmp.get());
            }

            Employee targetEmp = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));
            targetEmp.setUser(updatedUser);
            employeeRepository.save(targetEmp);
        }

        return mapToUserResponse(updatedUser);
    }

    @Override
    @Transactional
    @Audited(action = "RESET_PASSWORD_ADMIN", entity = "User")
    public void resetPassword(Long id, AdminResetPasswordRequest request) {
        log.info("Admin resetting password for user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setFailedLoginAttempts(0);
        user.setAccountNonLocked(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    @Audited(action = "TOGGLE_USER_STATUS", entity = "User")
    public UserResponse toggleUserStatus(Long id, boolean enabled) {
        log.info("Toggling user status for user ID: {} to {}", id, enabled);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        user.setEnabled(enabled);
        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    @Override
    @Transactional
    @Audited(action = "DELETE_USER", entity = "User")
    public void deleteUser(Long id) {
        log.info("Deleting user ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        // Unlink employee if linked
        employeeRepository.findByUserId(id).ifPresent(emp -> {
            emp.setUser(null);
            employeeRepository.save(emp);
        });

        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(r -> RoleResponse.builder()
                        .id(r.getId())
                        .name(r.getName())
                        .description(r.getDescription())
                        .build())
                .toList();
    }

    private UserResponse mapToUserResponse(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        Set<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());

        Long empId = null;
        String empCode = null;
        String empName = null;

        Optional<Employee> empOpt = employeeRepository.findByUserId(user.getId());
        if (empOpt.isPresent()) {
            Employee emp = empOpt.get();
            empId = emp.getId();
            empCode = emp.getEmployeeCode();
            empName = emp.getFirstName() + " " + emp.getLastName();
        }

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .accountNonLocked(user.isAccountNonLocked())
                .roles(roles)
                .permissions(permissions)
                .employeeId(empId)
                .employeeCode(empCode)
                .employeeName(empName)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
