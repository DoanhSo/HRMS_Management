package com.ng_doanh.hr_management_system.auth.mapper;

import com.ng_doanh.hr_management_system.auth.dto.response.UserResponse;
import com.ng_doanh.hr_management_system.auth.entity.Permission;
import com.ng_doanh.hr_management_system.auth.entity.Role;
import com.ng_doanh.hr_management_system.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapRolesToStrings")
    @Mapping(target = "permissions", source = "roles", qualifiedByName = "mapPermissionsToStrings")
    UserResponse toUserResponse(User user);

    @Named("mapRolesToStrings")
    default Set<String> mapRolesToStrings(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    @Named("mapPermissionsToStrings")
    default Set<String> mapPermissionsToStrings(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .filter(role -> role.getPermissions() != null)
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }
}
