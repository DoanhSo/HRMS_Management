package com.ng_doanh.hr_management_system.payroll.controller;

import com.ng_doanh.hr_management_system.common.constant.ApiPaths;
import com.ng_doanh.hr_management_system.common.constant.SecurityConstants;
import com.ng_doanh.hr_management_system.common.dto.ApiResponse;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import com.ng_doanh.hr_management_system.common.exception.BusinessException;
import com.ng_doanh.hr_management_system.common.security.CustomUserDetails;
import com.ng_doanh.hr_management_system.payroll.dto.request.PayrollPeriodCreateRequest;
import com.ng_doanh.hr_management_system.payroll.dto.response.PayrollPeriodResponse;
import com.ng_doanh.hr_management_system.payroll.dto.response.PayslipResponse;
import com.ng_doanh.hr_management_system.payroll.entity.PayrollPeriod;
import com.ng_doanh.hr_management_system.payroll.entity.Payslip;
import com.ng_doanh.hr_management_system.payroll.repository.PayrollPeriodRepository;
import com.ng_doanh.hr_management_system.payroll.repository.PayslipRepository;
import com.ng_doanh.hr_management_system.payroll.service.PayrollService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(ApiPaths.PAYROLL_BASE)
@RequiredArgsConstructor
@Tag(name = "Payroll Management", description = "APIs for monthly payroll periods, automatic calculation, and employee payslips")
public class PayrollController {

    private final PayrollService payrollService;
    private final PayrollPeriodRepository payrollPeriodRepository;
    private final PayslipRepository payslipRepository;
    private final ExcelExportService excelExportService;

    @PostMapping("/periods")
    @PreAuthorize(SecurityConstants.HAS_ROLE_HR_OR_ADMIN)
    @Operation(summary = "Create payroll period", description = "Creates a new monthly payroll period (HR/Admin only)")
    public ApiResponse<PayrollPeriodResponse> createPayrollPeriod(
            @Valid @RequestBody PayrollPeriodCreateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        PayrollPeriodResponse response = payrollService.createPayrollPeriod(request);
        return ApiResponse.success(ResponseCode.CREATED, response, httpServletRequest.getRequestURI());
    }

    @GetMapping("/periods")
    @PreAuthorize(SecurityConstants.HAS_ROLE_HR_OR_ADMIN)
    @Operation(summary = "List all payroll periods", description = "Fetches paginated list of monthly payroll periods (HR/Admin only)")
    public ApiResponse<Page<PayrollPeriodResponse>> getAllPayrollPeriods(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpServletRequest
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PayrollPeriodResponse> response = payrollService.getAllPayrollPeriods(pageable);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @PostMapping("/periods/{id:[0-9]+}/calculate")
    @PreAuthorize(SecurityConstants.HAS_ROLE_HR_OR_ADMIN)
    @Operation(summary = "Calculate payroll for period", description = "Triggers automatic salary calculation for all active employees (HR/Admin only)")
    public ApiResponse<Void> calculatePayrollForPeriod(
            @PathVariable Long id,
            HttpServletRequest httpServletRequest
    ) {
        payrollService.calculatePayrollForPeriod(id);
        return ApiResponse.success(ResponseCode.SUCCESS, httpServletRequest.getRequestURI());
    }

    @PutMapping("/periods/{id:[0-9]+}/approve")
    @PreAuthorize(SecurityConstants.HAS_ROLE_HR_OR_ADMIN)
    @Operation(summary = "Approve payroll period", description = "Locks and approves monthly payroll period and payslips (HR/Admin only)")
    public ApiResponse<Void> approvePayrollPeriod(
            @PathVariable Long id,
            HttpServletRequest httpServletRequest
    ) {
        payrollService.approvePayrollPeriod(id);
        return ApiResponse.success(ResponseCode.SUCCESS, httpServletRequest.getRequestURI());
    }

    @GetMapping("/periods/{id:[0-9]+}/export-excel")
    @PreAuthorize(SecurityConstants.HAS_ROLE_HR_OR_ADMIN)
    @Operation(summary = "Export payroll period sheet to Excel", description = "Exports complete payroll calculation sheet for the period")
    public ResponseEntity<byte[]> exportPayrollPeriodExcel(@PathVariable Long id) throws IOException {
        PayrollPeriod period = payrollPeriodRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        Page<Payslip> payslipsPage = payslipRepository.searchPayslips(id, null, null, Pageable.unpaged());
        byte[] excelBytes = excelExportService.exportPayrollPeriodToExcel(period, payslipsPage.getContent());

        String filename = "payroll_" + period.getYear() + "_" + String.format("%02d", period.getMonth()) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @GetMapping({"/my-records", "/payslips/my"})
    @Operation(summary = "Get my payslips history", description = "Employee views personal monthly payslips")
    public ApiResponse<Page<PayslipResponse>> getMyPayslips(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpServletRequest
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<PayslipResponse> response = payrollService.getMyPayslips(userDetails.getId(), pageable);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @GetMapping("/payslips")
    @PreAuthorize(SecurityConstants.HAS_ROLE_HR_OR_ADMIN)
    @Operation(summary = "Search company payslips", description = "Search company payslips by period, keyword, department (HR/Admin only)")
    public ApiResponse<Page<PayslipResponse>> searchPayslips(
            @RequestParam(required = false) Long periodId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpServletRequest
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<PayslipResponse> response = payrollService.searchPayslips(periodId, keyword, departmentId, pageable);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @GetMapping("/payslips/{id:[0-9]+}")
    @Operation(summary = "Get payslip details", description = "Fetches detailed payslip information by ID")
    public ApiResponse<PayslipResponse> getPayslipById(
            @PathVariable Long id,
            HttpServletRequest httpServletRequest
    ) {
        PayslipResponse response = payrollService.getPayslipById(id);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }
}
