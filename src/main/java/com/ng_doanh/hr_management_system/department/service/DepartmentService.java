package com.ng_doanh.hr_management_system.department.service;

import com.ng_doanh.hr_management_system.department.dto.request.DepartmentCreateRequest;
import com.ng_doanh.hr_management_system.department.dto.request.DepartmentUpdateRequest;
import com.ng_doanh.hr_management_system.department.dto.response.DepartmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DepartmentService {

    DepartmentResponse createDepartment(DepartmentCreateRequest request);

    DepartmentResponse getDepartmentById(Long id);

    DepartmentResponse getDepartmentByCode(String code);

    List<DepartmentResponse> getAllActiveDepartments();

    Page<DepartmentResponse> searchDepartments(String keyword, Boolean active, Pageable pageable);

    DepartmentResponse updateDepartment(Long id, DepartmentUpdateRequest request);

    void deleteDepartment(Long id);
}
