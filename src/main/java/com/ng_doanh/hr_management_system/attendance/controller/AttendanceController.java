package com.ng_doanh.hr_management_system.attendance.controller;

import com.ng_doanh.hr_management_system.attendance.dto.request.AttendanceManualRequest;
import com.ng_doanh.hr_management_system.attendance.dto.request.CheckInRequest;
import com.ng_doanh.hr_management_system.attendance.dto.request.CheckOutRequest;
import com.ng_doanh.hr_management_system.attendance.dto.response.AttendanceResponse;
import com.ng_doanh.hr_management_system.attendance.enums.AttendanceStatus;
import com.ng_doanh.hr_management_system.attendance.service.AttendanceService;
import com.ng_doanh.hr_management_system.common.constant.ApiPaths;
import com.ng_doanh.hr_management_system.common.constant.SecurityConstants;
import com.ng_doanh.hr_management_system.common.dto.ApiResponse;
import com.ng_doanh.hr_management_system.common.dto.ImportResultResponse;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import com.ng_doanh.hr_management_system.common.security.CustomUserDetails;
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

@RestController
@RequestMapping(ApiPaths.ATTENDANCES_BASE)
@RequiredArgsConstructor
@Tag(name = "Attendance Management", description = "APIs for employee daily check-in, check-out, and attendance tracking")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final ExcelExportService excelExportService;
    private final ExcelImportService excelImportService;

    @PostMapping("/check-in")
    @Operation(summary = "Check in today", description = "Employee performs daily check-in. System detects if late after 08:30 AM")
    public ApiResponse<AttendanceResponse> checkIn(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody(required = false) CheckInRequest request,
            HttpServletRequest httpServletRequest
    ) {
        AttendanceResponse response = attendanceService.checkIn(userDetails.getId(), request);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @PostMapping("/check-out")
    @Operation(summary = "Check out today", description = "Employee performs daily check-out. Calculates total work hours and early leave")
    public ApiResponse<AttendanceResponse> checkOut(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody(required = false) CheckOutRequest request,
            HttpServletRequest httpServletRequest
    ) {
        AttendanceResponse response = attendanceService.checkOut(userDetails.getId(), request);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @GetMapping("/my-history")
    @Operation(summary = "Get my attendance history", description = "Fetches attendance history of the currently logged in employee")
    public ApiResponse<Page<AttendanceResponse>> getMyAttendanceHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpServletRequest
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("workDate").descending());
        Page<AttendanceResponse> response = attendanceService.getMyAttendanceHistory(userDetails.getId(), startDate, endDate, pageable);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @GetMapping
    @PreAuthorize(SecurityConstants.HAS_ROLE_MANAGER_OR_ABOVE)
    @Operation(summary = "Search company attendances", description = "Supports searching attendances by keyword, department, date range, status (Manager/HR/Admin)")
    public ApiResponse<Page<AttendanceResponse>> searchAttendances(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) AttendanceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "workDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest httpServletRequest
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AttendanceResponse> response = attendanceService.searchAttendances(keyword, departmentId, startDate, endDate, status, pageable);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @GetMapping("/export")
    @PreAuthorize(SecurityConstants.HAS_ROLE_MANAGER_OR_ABOVE)
    @Operation(summary = "Export attendance to Excel", description = "Exports attendance records to Excel sheet")
    public ResponseEntity<byte[]> exportAttendances(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long departmentId
    ) throws IOException {
        LocalDate start = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        byte[] excelBytes = excelExportService.exportMonthlyAttendanceToExcel(start, end, departmentId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance_" + start + "_to_" + end + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @GetMapping("/template")
    @PreAuthorize(SecurityConstants.HAS_ROLE_HR_OR_ADMIN)
    @Operation(summary = "Download attendance import template", description = "Provides Excel template for importing biometric attendance data")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        byte[] excelBytes = excelExportService.generateAttendanceTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance_import_template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @PostMapping("/import")
    @PreAuthorize(SecurityConstants.HAS_ROLE_HR_OR_ADMIN)
    @Operation(summary = "Import attendance from Excel", description = "Imports attendance records from biometric machine Excel file")
    public ApiResponse<ImportResultResponse> importAttendances(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest httpServletRequest
    ) {
        ImportResultResponse result = excelImportService.importAttendances(file);
        return ApiResponse.success(ResponseCode.SUCCESS, result, httpServletRequest.getRequestURI());
    }

    @PostMapping("/manual")
    @PreAuthorize(SecurityConstants.HAS_ROLE_HR_OR_ADMIN)
    @Operation(summary = "Manual attendance entry", description = "HR/Admin manually creates or overrides an employee's attendance record")
    public ApiResponse<AttendanceResponse> manualAttendanceEntry(
            @Valid @RequestBody AttendanceManualRequest request,
            HttpServletRequest httpServletRequest
    ) {
        AttendanceResponse response = attendanceService.manualAttendanceEntry(request);
        return ApiResponse.success(ResponseCode.CREATED, response, httpServletRequest.getRequestURI());
    }
}
