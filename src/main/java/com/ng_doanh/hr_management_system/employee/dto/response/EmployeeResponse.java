package com.ng_doanh.hr_management_system.employee.dto.response;

import com.ng_doanh.hr_management_system.common.enums.Gender;
import com.ng_doanh.hr_management_system.employee.enums.EmploymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {

    private Long id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String fullName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String phone;
    private String address;
    private LocalDate hireDate;
    private LocalDate terminationDate;
    private EmploymentStatus employmentStatus;
    private Long departmentId;
    private Long positionId;
    private Long managerId;
    private String managerName;
    private Long userId;
    private String profilePictureUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
