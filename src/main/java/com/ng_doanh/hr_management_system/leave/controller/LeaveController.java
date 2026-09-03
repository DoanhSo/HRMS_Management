package com.ng_doanh.hr_management_system.leave.controller;

import com.ng_doanh.hr_management_system.common.constant.ApiPaths;
import com.ng_doanh.hr_management_system.common.constant.SecurityConstants;
import com.ng_doanh.hr_management_system.common.dto.ApiResponse;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import com.ng_doanh.hr_management_system.common.security.CustomUserDetails;
import com.ng_doanh.hr_management_system.leave.dto.request.LeaveApprovalRequest;
import com.ng_doanh.hr_management_system.leave.dto.request.LeaveRequestCreateRequest;
import com.ng_doanh.hr_management_system.leave.dto.request.LeaveTypeCreateRequest;
import com.ng_doanh.hr_management_system.leave.dto.response.LeaveBalanceResponse;
import com.ng_doanh.hr_management_system.leave.dto.response.LeaveRequestResponse;
import com.ng_doanh.hr_management_system.leave.dto.response.LeaveTypeResponse;
import com.ng_doanh.hr_management_system.leave.enums.LeaveRequestStatus;
import com.ng_doanh.hr_management_system.leave.service.LeaveService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(ApiPaths.LEAVES_BASE)
@RequiredArgsConstructor
@Tag(name = "Leave Management", description = "APIs for leave types, leave balances, leave applications, and approval workflow")
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping("/types")
    @PreAuthorize(SecurityConstants.HAS_ROLE_HR_OR_ADMIN)
    @Operation(summary = "Create leave type", description = "Creates a new type of leave like Annual, Sick, Maternity (HR/Admin only)")
    public ApiResponse<LeaveTypeResponse> createLeaveType(
            @Valid @RequestBody LeaveTypeCreateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        LeaveTypeResponse response = leaveService.createLeaveType(request);
        return ApiResponse.success(ResponseCode.CREATED, response, httpServletRequest.getRequestURI());
    }

    @GetMapping("/types")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Get all active leave types", description = "Fetches list of active leave types for dropdowns")
    public ApiResponse<List<LeaveTypeResponse>> getAllActiveLeaveTypes(HttpServletRequest httpServletRequest) {
        List<LeaveTypeResponse> response = leaveService.getAllActiveLeaveTypes();
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @GetMapping("/balances/my")
    @Operation(summary = "Get my leave balances", description = "Fetches leave quota balances for currently logged in employee")
    public ApiResponse<List<LeaveBalanceResponse>> getMyLeaveBalances(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Integer year,
            HttpServletRequest httpServletRequest
    ) {
        List<LeaveBalanceResponse> response = leaveService.getMyLeaveBalances(userDetails.getId(), year);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @PostMapping("/requests")
    @Operation(summary = "Create leave request", description = "Employee submits a new leave request application")
    public ApiResponse<LeaveRequestResponse> createLeaveRequest(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody LeaveRequestCreateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        LeaveRequestResponse response = leaveService.createLeaveRequest(userDetails.getId(), request);
        return ApiResponse.success(ResponseCode.CREATED, response, httpServletRequest.getRequestURI());
    }

    @GetMapping("/requests/my")
    @Operation(summary = "Get my leave requests", description = "Fetches personal leave requests history for logged in employee")
    public ApiResponse<Page<LeaveRequestResponse>> getMyLeaveRequests(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpServletRequest
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<LeaveRequestResponse> response = leaveService.getMyLeaveRequests(userDetails.getId(), pageable);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @GetMapping("/requests")
    @PreAuthorize(SecurityConstants.HAS_ROLE_MANAGER_OR_ABOVE)
    @Operation(summary = "Search company leave requests", description = "Search leave applications by keyword, department, status (Manager/HR/Admin)")
    public ApiResponse<Page<LeaveRequestResponse>> searchLeaveRequests(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long leaveTypeId,
            @RequestParam(required = false) LeaveRequestStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest httpServletRequest
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<LeaveRequestResponse> response = leaveService.searchLeaveRequests(keyword, departmentId, leaveTypeId, status, startDate, endDate, pageable);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @PutMapping("/requests/{id}/approve")
    @PreAuthorize(SecurityConstants.HAS_ROLE_MANAGER_OR_ABOVE)
    @Operation(summary = "Approve leave request", description = "Approver approves leave request, deducts balance, and marks attendance as ON_LEAVE")
    public ApiResponse<LeaveRequestResponse> approveLeaveRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpServletRequest
    ) {
        LeaveRequestResponse response = leaveService.approveLeaveRequest(id, userDetails.getId());
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @PutMapping("/requests/{id}/reject")
    @PreAuthorize(SecurityConstants.HAS_ROLE_MANAGER_OR_ABOVE)
    @Operation(summary = "Reject leave request", description = "Approver rejects leave request with rejection reason")
    public ApiResponse<LeaveRequestResponse> rejectLeaveRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody(required = false) LeaveApprovalRequest request,
            HttpServletRequest httpServletRequest
    ) {
        LeaveRequestResponse response = leaveService.rejectLeaveRequest(id, userDetails.getId(), request);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @PutMapping("/requests/{id}/cancel")
    @Operation(summary = "Cancel leave request", description = "Employee cancels their own pending leave request")
    public ApiResponse<LeaveRequestResponse> cancelLeaveRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpServletRequest
    ) {
        LeaveRequestResponse response = leaveService.cancelLeaveRequest(id, userDetails.getId());
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }
}
