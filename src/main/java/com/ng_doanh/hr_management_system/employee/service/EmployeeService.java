package com.ng_doanh.hr_management_system.employee.service;

import com.ng_doanh.hr_management_system.common.enums.Gender;
import com.ng_doanh.hr_management_system.employee.dto.request.EmployeeCreateRequest;
import com.ng_doanh.hr_management_system.employee.dto.request.EmployeeUpdateRequest;
import com.ng_doanh.hr_management_system.employee.dto.response.EmployeeResponse;
import com.ng_doanh.hr_management_system.employee.enums.EmploymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface EmployeeService {

    EmployeeResponse createEmployee(EmployeeCreateRequest request);

    EmployeeResponse getEmployeeById(Long id);

    EmployeeResponse getEmployeeByCode(String code);

    EmployeeResponse getEmployeeByUserId(Long userId);

    Page<EmployeeResponse> searchEmployees(
            String keyword,
            Long departmentId,
            Long positionId,
            EmploymentStatus status,
            Gender gender,
            LocalDate hireDateFrom,
            LocalDate hireDateTo,
            Pageable pageable
    );

    EmployeeResponse updateEmployee(Long id, EmployeeUpdateRequest request);

    void deleteEmployee(Long id);
}
