package com.ng_doanh.hr_management_system.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private boolean enabled;
    private boolean accountNonLocked;
    private Set<String> roles;
    private Set<String> permissions;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
