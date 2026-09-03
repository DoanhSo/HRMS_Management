package com.ng_doanh.hr_management_system.department.controller;

import com.ng_doanh.hr_management_system.common.constant.ApiPaths;
import com.ng_doanh.hr_management_system.common.constant.SecurityConstants;
import com.ng_doanh.hr_management_system.common.dto.ApiResponse;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import com.ng_doanh.hr_management_system.department.dto.request.DepartmentCreateRequest;
import com.ng_doanh.hr_management_system.department.dto.request.DepartmentUpdateRequest;
import com.ng_doanh.hr_management_system.department.dto.response.DepartmentResponse;
import com.ng_doanh.hr_management_system.department.entity.Department;
import com.ng_doanh.hr_management_system.department.repository.DepartmentRepository;
import com.ng_doanh.hr_management_system.department.service.DepartmentService;
import com.ng_doanh.hr_management_system.report.service.ExcelExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(ApiPaths.DEPARTMENTS_BASE)
@RequiredArgsConstructor
@Tag(name = "Department Management", description = "APIs for managing departments, hierarchy, and department managers")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final DepartmentRepository departmentRepository;
    private final ExcelExportService excelExportService;

    @PostMapping
    @PreAuthorize(SecurityConstants.HAS_ROLE_HR_OR_ADMIN)
    @Operation(summary = "Create department", description = "Creates a new department with unique code and name (HR/Admin only)")
    public ApiResponse<DepartmentResponse> createDepartment(
            @Valid @RequestBody DepartmentCreateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        DepartmentResponse response = departmentService.createDepartment(request);
        return ApiResponse.success(ResponseCode.CREATED, response, httpServletRequest.getRequestURI());
    }

    @GetMapping
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Search departments", description = "Supports pagination and search by keyword or active status")
    public ApiResponse<Page<DepartmentResponse>> searchDepartments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest httpServletRequest
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<DepartmentResponse> response = departmentService.searchDepartments(keyword, active, pageable);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @GetMapping("/export")
    @PreAuthorize(SecurityConstants.HAS_ROLE_MANAGER_OR_ABOVE)
    @Operation(summary = "Export departments to Excel", description = "Exports department list to Excel")
    public ResponseEntity<byte[]> exportDepartments() throws IOException {
        List<Department> depts = departmentRepository.findAll();
        byte[] excelBytes = excelExportService.exportDepartmentsToExcel(depts);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=departments.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @GetMapping("/active")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Get all active departments", description = "Fetches simple unpaginated list of active departments for UI dropdowns")
    public ApiResponse<List<DepartmentResponse>> getAllActiveDepartments(HttpServletRequest httpServletRequest) {
        List<DepartmentResponse> response = departmentService.getAllActiveDepartments();
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @GetMapping("/{id:[0-9]+}")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Get department by ID", description = "Fetches department details by database ID")
    public ApiResponse<DepartmentResponse> getDepartmentById(
            @PathVariable Long id,
            HttpServletRequest httpServletRequest
    ) {
        DepartmentResponse response = departmentService.getDepartmentById(id);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @PutMapping("/{id:[0-9]+}")
    @PreAuthorize(SecurityConstants.HAS_ROLE_HR_OR_ADMIN)
    @Operation(summary = "Update department", description = "Updates department information (HR/Admin only)")
    public ApiResponse<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentUpdateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        DepartmentResponse response = departmentService.updateDepartment(id, request);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @DeleteMapping("/{id:[0-9]+}")
    @PreAuthorize(SecurityConstants.HAS_ROLE_HR_OR_ADMIN)
    @Operation(summary = "Delete department", description = "Deletes a department (HR/Admin only)")
    public ApiResponse<Void> deleteDepartment(
            @PathVariable Long id,
            HttpServletRequest httpServletRequest
    ) {
        departmentService.deleteDepartment(id);
        return ApiResponse.success(ResponseCode.DELETED, httpServletRequest.getRequestURI());
    }
}
