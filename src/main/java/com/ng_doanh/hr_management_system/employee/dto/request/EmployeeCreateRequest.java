package com.ng_doanh.hr_management_system.employee.dto.request;

import com.ng_doanh.hr_management_system.common.enums.Gender;
import com.ng_doanh.hr_management_system.employee.enums.EmploymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCreateRequest {

    @Size(max = 20, message = "Employee code must not exceed 20 characters")
    private String employeeCode;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private LocalDate dateOfBirth;

    private Gender gender;

    private String phone;

    private String address;

    @NotNull(message = "Hire date is required")
    private LocalDate hireDate;

    @Builder.Default
    private EmploymentStatus employmentStatus = EmploymentStatus.PROBATION;

    private Long departmentId;

    private Long positionId;

    private Long managerId;

    private Long userId;

    private String profilePictureUrl;
}
