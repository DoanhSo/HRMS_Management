package com.ng_doanh.hr_management_system.kpi.controller;

import com.ng_doanh.hr_management_system.common.constant.ApiPaths;
import com.ng_doanh.hr_management_system.common.constant.SecurityConstants;
import com.ng_doanh.hr_management_system.common.dto.ApiResponse;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import com.ng_doanh.hr_management_system.common.security.CustomUserDetails;
import com.ng_doanh.hr_management_system.kpi.dto.request.KpiCriteriaCreateRequest;
import com.ng_doanh.hr_management_system.kpi.dto.request.KpiCriteriaUpdateRequest;
import com.ng_doanh.hr_management_system.kpi.dto.request.KpiEvaluationCreateRequest;
import com.ng_doanh.hr_management_system.kpi.dto.response.KpiCriteriaResponse;
import com.ng_doanh.hr_management_system.kpi.dto.response.KpiEvaluationResponse;
import com.ng_doanh.hr_management_system.kpi.enums.KpiEvaluationStatus;
import com.ng_doanh.hr_management_system.kpi.service.KpiService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPaths.KPI_BASE)
@RequiredArgsConstructor
@Tag(name = "KPI Performance Appraisal", description = "Quản lý Đánh giá Hiệu suất, Tiêu chí KPI & Tích hợp Thưởng lương")
public class KpiController {

    private final KpiService kpiService;

    // =========================================================================
    // 1. KPI Criteria Endpoints
    // =========================================================================

    @PostMapping("/criteria")
    @PreAuthorize(SecurityConstants.HAS_ROLE_HR_OR_ADMIN)
    @Operation(summary = "Tạo tiêu chí KPI mới")
    public ApiResponse<KpiCriteriaResponse> createCriteria(
            @Valid @RequestBody KpiCriteriaCreateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        KpiCriteriaResponse response = kpiService.createCriteria(request);
        return ApiResponse.success(ResponseCode.CREATED, response, httpServletRequest.getRequestURI());
    }

    @PutMapping("/criteria/{id}")
    @PreAuthorize(SecurityConstants.HAS_ROLE_HR_OR_ADMIN)
    @Operation(summary = "Cập nhật tiêu chí KPI")
    public ApiResponse<KpiCriteriaResponse> updateCriteria(
            @PathVariable Long id,
            @Valid @RequestBody KpiCriteriaUpdateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        KpiCriteriaResponse response = kpiService.updateCriteria(id, request);
        return ApiResponse.success(ResponseCode.UPDATED, response, httpServletRequest.getRequestURI());
    }

    @DeleteMapping("/criteria/{id}")
    @PreAuthorize(SecurityConstants.HAS_ROLE_HR_OR_ADMIN)
    @Operation(summary = "Xóa tiêu chí KPI")
    public ApiResponse<Void> deleteCriteria(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        kpiService.deleteCriteria(id);
        return ApiResponse.success(ResponseCode.DELETED, null, httpServletRequest.getRequestURI());
    }

    @GetMapping("/criteria/active")
    @Operation(summary = "Lấy tất cả tiêu chí KPI đang hoạt động")
    public ApiResponse<List<KpiCriteriaResponse>> getAllActiveCriteria(HttpServletRequest httpServletRequest) {
        List<KpiCriteriaResponse> list = kpiService.getAllActiveCriteria();
        return ApiResponse.success(ResponseCode.SUCCESS, list, httpServletRequest.getRequestURI());
    }

    @GetMapping("/criteria")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Tìm kiếm & Phân trang danh sách tiêu chí KPI")
    public ApiResponse<Page<KpiCriteriaResponse>> searchCriteria(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 20, sort = "weight", direction = Sort.Direction.DESC) Pageable pageable,
            HttpServletRequest httpServletRequest
    ) {
        Page<KpiCriteriaResponse> page = kpiService.searchCriteria(keyword, departmentId, active, pageable);
        return ApiResponse.success(ResponseCode.SUCCESS, page, httpServletRequest.getRequestURI());
    }

    // =========================================================================
    // 2. KPI Evaluation Endpoints
    // =========================================================================

    @PostMapping("/evaluations")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Tạo hoặc Cập nhật phiếu đánh giá KPI của nhân viên")
    public ApiResponse<KpiEvaluationResponse> createOrUpdateEvaluation(
            @Valid @RequestBody KpiEvaluationCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpServletRequest
    ) {
        Long evaluatorUserId = userDetails != null && userDetails.getUser() != null ? userDetails.getUser().getId() : null;
        KpiEvaluationResponse response = kpiService.createOrUpdateEvaluation(request, evaluatorUserId);
        return ApiResponse.success(ResponseCode.CREATED, response, httpServletRequest.getRequestURI());
    }

    @GetMapping("/evaluations/{id}")
    @Operation(summary = "Lấy chi tiết phiếu đánh giá KPI theo ID")
    public ApiResponse<KpiEvaluationResponse> getEvaluationById(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        KpiEvaluationResponse response = kpiService.getEvaluationById(id);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @GetMapping("/evaluations/employee/{employeeId}")
    @Operation(summary = "Lấy phiếu đánh giá KPI của nhân viên theo tháng/năm")
    public ApiResponse<KpiEvaluationResponse> getEmployeeEvaluation(
            @PathVariable Long employeeId,
            @RequestParam Integer year,
            @RequestParam Integer month,
            HttpServletRequest httpServletRequest
    ) {
        KpiEvaluationResponse response = kpiService.getEmployeeEvaluation(employeeId, year, month);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @GetMapping("/evaluations/my")
    @Operation(summary = "Xem lịch sử đánh giá KPI của chính mình")
    public ApiResponse<Page<KpiEvaluationResponse>> getMyEvaluations(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 12) Pageable pageable,
            HttpServletRequest httpServletRequest
    ) {
        Long userId = userDetails.getUser().getId();
        Page<KpiEvaluationResponse> page = kpiService.getMyEvaluations(userId, pageable);
        return ApiResponse.success(ResponseCode.SUCCESS, page, httpServletRequest.getRequestURI());
    }

    @GetMapping("/evaluations")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Tìm kiếm & Lọc danh sách đánh giá KPI toàn công ty")
    public ApiResponse<Page<KpiEvaluationResponse>> searchEvaluations(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) KpiEvaluationStatus status,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable,
            HttpServletRequest httpServletRequest
    ) {
        Page<KpiEvaluationResponse> page = kpiService.searchEvaluations(year, month, status, departmentId, keyword, pageable);
        return ApiResponse.success(ResponseCode.SUCCESS, page, httpServletRequest.getRequestURI());
    }

    @PutMapping("/evaluations/{id}/approve")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Phê duyệt phiếu đánh giá KPI (Xác nhận điểm & Chốt tiền thưởng)")
    public ApiResponse<KpiEvaluationResponse> approveEvaluation(
            @PathVariable Long id,
            @RequestParam(required = false) String feedback,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpServletRequest
    ) {
        Long approverUserId = userDetails != null && userDetails.getUser() != null ? userDetails.getUser().getId() : null;
        KpiEvaluationResponse response = kpiService.approveEvaluation(id, approverUserId, feedback);
        return ApiResponse.success(ResponseCode.UPDATED, response, httpServletRequest.getRequestURI());
    }

    @PutMapping("/evaluations/{id}/reject")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Từ chối phiếu đánh giá KPI (Yêu cầu đánh giá lại)")
    public ApiResponse<KpiEvaluationResponse> rejectEvaluation(
            @PathVariable Long id,
            @RequestParam(required = false) String feedback,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpServletRequest
    ) {
        Long approverUserId = userDetails != null && userDetails.getUser() != null ? userDetails.getUser().getId() : null;
        KpiEvaluationResponse response = kpiService.rejectEvaluation(id, approverUserId, feedback);
        return ApiResponse.success(ResponseCode.UPDATED, response, httpServletRequest.getRequestURI());
    }
}
