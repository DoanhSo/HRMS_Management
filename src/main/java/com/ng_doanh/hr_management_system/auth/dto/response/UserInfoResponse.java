package com.ng_doanh.hr_management_system.auth.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserInfoResponse {

    Long id;
    String username;
    String email;
    String fullName;
    String avatarUrl;
    Set<String> roles;
    Set<String> permissions;
}
