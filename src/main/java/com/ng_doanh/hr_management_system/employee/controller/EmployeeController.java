package com.ng_doanh.hr_management_system.employee.controller;

import com.ng_doanh.hr_management_system.common.constant.ApiPaths;
import com.ng_doanh.hr_management_system.common.constant.SecurityConstants;
import com.ng_doanh.hr_management_system.common.dto.ApiResponse;
import com.ng_doanh.hr_management_system.common.dto.ImportResultResponse;
import com.ng_doanh.hr_management_system.common.enums.Gender;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import com.ng_doanh.hr_management_system.common.security.CustomUserDetails;
import com.ng_doanh.hr_management_system.employee.dto.request.EmployeeCreateRequest;
import com.ng_doanh.hr_management_system.employee.dto.request.EmployeeUpdateRequest;
import com.ng_doanh.hr_management_system.employee.dto.response.EmployeeResponse;
import com.ng_doanh.hr_management_system.employee.entity.Employee;
import com.ng_doanh.hr_management_system.employee.enums.EmploymentStatus;
import com.ng_doanh.hr_management_system.employee.repository.EmployeeRepository;
import com.ng_doanh.hr_management_system.employee.service.EmployeeService;
import com.ng_doanh.hr_management_system.report.service.ExcelExportService;
import com.ng_doanh.hr_management_system.report.service.ExcelImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(ApiPaths.EMPLOYEES_BASE)
@RequiredArgsConstructor
@Tag(name = "Employee Management", description = "APIs for managing employees, profiles, and hierarchy")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepository;
    private final ExcelExportService excelExportService;
    private final ExcelImportService excelImportService;

    @PostMapping
    @PreAuthorize(SecurityConstants.HAS_ROLE_HR_OR_ADMIN)
    @Operation(summary = "Create a new employee", description = "Creates employee and auto-generates code like EMP-00001 (HR/Admin only)")
    public ApiResponse<EmployeeResponse> createEmployee(
            @Valid @RequestBody EmployeeCreateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        EmployeeResponse response = employeeService.createEmployee(request);
        return ApiResponse.success(ResponseCode.CREATED, response, httpServletRequest.getRequestURI());
    }

    @GetMapping
    @PreAuthorize(SecurityConstants.HAS_ROLE_MANAGER_OR_ABOVE)
    @Operation(summary = "Search and list employees", description = "Supports pagination, search by keyword, department, position, status, gender, hire date range")
    public ApiResponse<Page<EmployeeResponse>> searchEmployees(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long positionId,
            @RequestParam(required = false) EmploymentStatus status,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hireDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hireDateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest httpServletRequest
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<EmployeeResponse> response = employeeService.searchEmployees(
                keyword,
                departmentId,
                positionId,
                status,
                gender,
                hireDateFrom,
                hireDateTo,
                pageable
        );
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @GetMapping("/export")
    @PreAuthorize(SecurityConstants.HAS_ROLE_MANAGER_OR_ABOVE)
    @Operation(summary = "Export employees to Excel", description = "Exports filtered employee list to Excel sheet")
    public ResponseEntity<byte[]> exportEmployees(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long positionId,
            @RequestParam(required = false) EmploymentStatus status,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hireDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hireDateTo
    ) throws IOException {
        Page<Employee> page = employeeRepository.searchEmployees(
                keyword, departmentId, positionId, status, gender, hireDateFrom, hireDateTo, Pageable.unpaged()
        );
        byte[] excelBytes = excelExportService.exportEmployeesToExcel(page.getContent());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employees_" + LocalDate.now() + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @GetMapping("/template")
    @PreAuthorize(SecurityConstants.HAS_ROLE_HR_OR_ADMIN)
    @Operation(summary = "Download employee import template", description = "Provides standard Excel template for batch employee import")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        byte[] excelBytes = excelExportService.generateEmployeeTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employee_import_template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @PostMapping("/import")
    @PreAuthorize(SecurityConstants.HAS_ROLE_HR_OR_ADMIN)
    @Operation(summary = "Import employees from Excel", description = "Imports employee list from Excel file with row validation")
    public ApiResponse<ImportResultResponse> importEmployees(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest httpServletRequest
    ) {
        ImportResultResponse result = excelImportService.importEmployees(file);
        return ApiResponse.success(ResponseCode.SUCCESS, result, httpServletRequest.getRequestURI());
    }

    @GetMapping("/{id:[0-9]+}")
    @PreAuthorize(SecurityConstants.HAS_ROLE_MANAGER_OR_ABOVE)
    @Operation(summary = "Get employee by ID", description = "Fetches employee details by database ID")
    public ApiResponse<EmployeeResponse> getEmployeeById(
            @PathVariable Long id,
            HttpServletRequest httpServletRequest
    ) {
        EmployeeResponse response = employeeService.getEmployeeById(id);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @GetMapping("/me")
    @Operation(summary = "Get my employee profile", description = "Fetches employee profile of the currently logged in user")
    public ApiResponse<EmployeeResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpServletRequest
    ) {
        EmployeeResponse response = employeeService.getEmployeeByUserId(userDetails.getId());
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @PutMapping("/{id:[0-9]+}")
    @PreAuthorize(SecurityConstants.HAS_ROLE_HR_OR_ADMIN)
    @Operation(summary = "Update employee", description = "Updates employee information (HR/Admin only)")
    public ApiResponse<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @DeleteMapping("/{id:[0-9]+}")
    @PreAuthorize(SecurityConstants.HAS_ROLE_HR_OR_ADMIN)
    @Operation(summary = "Delete employee", description = "Deletes an employee (HR/Admin only)")
    public ApiResponse<Void> deleteEmployee(
            @PathVariable Long id,
            HttpServletRequest httpServletRequest
    ) {
        employeeService.deleteEmployee(id);
        return ApiResponse.success(ResponseCode.DELETED, httpServletRequest.getRequestURI());
    }
}
