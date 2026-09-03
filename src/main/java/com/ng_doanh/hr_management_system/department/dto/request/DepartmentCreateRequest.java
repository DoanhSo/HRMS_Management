package com.ng_doanh.hr_management_system.department.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentCreateRequest {

    @NotBlank(message = "Department name is required")
    private String name;

    @Size(max = 20, message = "Department code must not exceed 20 characters")
    private String code;

    private String description;

    private Long managerId;

    private Long parentDepartmentId;
}
