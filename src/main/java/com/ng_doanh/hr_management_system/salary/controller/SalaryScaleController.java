package com.ng_doanh.hr_management_system.salary.controller;

import com.ng_doanh.hr_management_system.common.constant.ApiPaths;
import com.ng_doanh.hr_management_system.common.constant.SecurityConstants;
import com.ng_doanh.hr_management_system.common.dto.ApiResponse;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import com.ng_doanh.hr_management_system.salary.dto.request.SalaryScaleCreateRequest;
import com.ng_doanh.hr_management_system.salary.dto.request.SalaryScaleUpdateRequest;
import com.ng_doanh.hr_management_system.salary.dto.response.SalaryScaleResponse;
import com.ng_doanh.hr_management_system.salary.service.SalaryScaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.SALARY_SCALES_BASE)
@RequiredArgsConstructor
@Tag(name = "Salary Scale & Coefficient Management", description = "Quản lý Thang Bảng Lương, Ngạch/Bậc Lương & Hệ Số Lương Cơ Bản")
public class SalaryScaleController {

    private final SalaryScaleService salaryScaleService;

    @PostMapping
    @PreAuthorize(SecurityConstants.HAS_ROLE_HR_OR_ADMIN)
    @Operation(summary = "Tạo ngạch/bậc lương và hệ số lương mới")
    public ApiResponse<SalaryScaleResponse> createSalaryScale(
            @Valid @RequestBody SalaryScaleCreateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        SalaryScaleResponse response = salaryScaleService.createSalaryScale(request);
        return ApiResponse.success(ResponseCode.CREATED, response, httpServletRequest.getRequestURI());
    }

    @PutMapping("/{id}")
    @PreAuthorize(SecurityConstants.HAS_ROLE_HR_OR_ADMIN)
    @Operation(summary = "Cập nhật bậc lương và hệ số lương")
    public ApiResponse<SalaryScaleResponse> updateSalaryScale(
            @PathVariable Long id,
            @Valid @RequestBody SalaryScaleUpdateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        SalaryScaleResponse response = salaryScaleService.updateSalaryScale(id, request);
        return ApiResponse.success(ResponseCode.UPDATED, response, httpServletRequest.getRequestURI());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize(SecurityConstants.HAS_ROLE_HR_OR_ADMIN)
    @Operation(summary = "Xóa ngạch/bậc lương")
    public ApiResponse<Void> deleteSalaryScale(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        salaryScaleService.deleteSalaryScale(id);
        return ApiResponse.success(ResponseCode.DELETED, null, httpServletRequest.getRequestURI());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết bậc lương theo ID")
    public ApiResponse<SalaryScaleResponse> getSalaryScaleById(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        SalaryScaleResponse response = salaryScaleService.getSalaryScaleById(id);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @GetMapping("/active")
    @Operation(summary = "Lấy danh sách tất cả bậc lương đang hoạt động")
    public ApiResponse<List<SalaryScaleResponse>> getAllActiveSalaryScales(HttpServletRequest httpServletRequest) {
        List<SalaryScaleResponse> list = salaryScaleService.getAllActiveSalaryScales();
        return ApiResponse.success(ResponseCode.SUCCESS, list, httpServletRequest.getRequestURI());
    }

    @GetMapping
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Tìm kiếm & Phân trang danh sách thang bảng lương")
    public ApiResponse<Page<SalaryScaleResponse>> searchSalaryScales(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long positionId,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "coefficient", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest httpServletRequest
    ) {
        Page<SalaryScaleResponse> page = salaryScaleService.searchSalaryScales(keyword, positionId, active, pageable);
        return ApiResponse.success(ResponseCode.SUCCESS, page, httpServletRequest.getRequestURI());
    }
}
