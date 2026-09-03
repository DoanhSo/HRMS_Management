package com.ng_doanh.hr_management_system.dashboard.controller;

import com.ng_doanh.hr_management_system.common.constant.ApiPaths;
import com.ng_doanh.hr_management_system.common.constant.SecurityConstants;
import com.ng_doanh.hr_management_system.common.dto.ApiResponse;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import com.ng_doanh.hr_management_system.dashboard.dto.response.AttendanceOverviewResponse;
import com.ng_doanh.hr_management_system.dashboard.dto.response.DashboardSummaryResponse;
import com.ng_doanh.hr_management_system.dashboard.dto.response.DepartmentStatsResponse;
import com.ng_doanh.hr_management_system.dashboard.dto.response.PayrollSummaryResponse;
import com.ng_doanh.hr_management_system.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(ApiPaths.DASHBOARD_BASE)
@RequiredArgsConstructor
@Tag(name = "Dashboard & Analytics", description = "Executive APIs for HR analytics, attendance rate, department stats, and payroll costs")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @PreAuthorize(SecurityConstants.HAS_ROLE_MANAGER_OR_ABOVE)
    @Operation(summary = "Get overall dashboard summary", description = "Aggregates total employees, active count, departments, positions, and latest payroll cost")
    public ApiResponse<DashboardSummaryResponse> getDashboardSummary(HttpServletRequest httpServletRequest) {
        DashboardSummaryResponse response = dashboardService.getDashboardSummary();
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @GetMapping("/attendance-overview")
    @PreAuthorize(SecurityConstants.HAS_ROLE_MANAGER_OR_ABOVE)
    @Operation(summary = "Get attendance overview for date", description = "Calculates attendance rate, present count, late count, and absent count for a specific date")
    public ApiResponse<AttendanceOverviewResponse> getAttendanceOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletRequest httpServletRequest
    ) {
        AttendanceOverviewResponse response = dashboardService.getAttendanceOverview(date);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @GetMapping("/department-stats")
    @PreAuthorize(SecurityConstants.HAS_ROLE_MANAGER_OR_ABOVE)
    @Operation(summary = "Get department statistics", description = "Fetches headcounts and manager names grouped by department")
    public ApiResponse<List<DepartmentStatsResponse>> getDepartmentStats(HttpServletRequest httpServletRequest) {
        List<DepartmentStatsResponse> response = dashboardService.getDepartmentStats();
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @GetMapping("/payroll-summary")
    @PreAuthorize(SecurityConstants.HAS_ROLE_MANAGER_OR_ABOVE)
    @Operation(summary = "Get monthly payroll summary history", description = "Fetches historical monthly payroll cost trends for executives")
    public ApiResponse<List<PayrollSummaryResponse>> getPayrollSummary(HttpServletRequest httpServletRequest) {
        List<PayrollSummaryResponse> response = dashboardService.getPayrollSummary();
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }
}
