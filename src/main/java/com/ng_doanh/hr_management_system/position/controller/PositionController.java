package com.ng_doanh.hr_management_system.position.controller;

import com.ng_doanh.hr_management_system.common.constant.ApiPaths;
import com.ng_doanh.hr_management_system.common.constant.SecurityConstants;
import com.ng_doanh.hr_management_system.common.dto.ApiResponse;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import com.ng_doanh.hr_management_system.position.dto.request.PositionCreateRequest;
import com.ng_doanh.hr_management_system.position.dto.request.PositionUpdateRequest;
import com.ng_doanh.hr_management_system.position.dto.response.PositionResponse;
import com.ng_doanh.hr_management_system.position.entity.Position;
import com.ng_doanh.hr_management_system.position.repository.PositionRepository;
import com.ng_doanh.hr_management_system.position.service.PositionService;
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
@RequestMapping(ApiPaths.POSITIONS_BASE)
@RequiredArgsConstructor
@Tag(name = "Position Management", description = "APIs for managing job positions, titles, and salary ranges")
public class PositionController {

    private final PositionService positionService;
    private final PositionRepository positionRepository;
    private final ExcelExportService excelExportService;

    @PostMapping
    @PreAuthorize(SecurityConstants.HAS_ROLE_HR_OR_ADMIN)
    @Operation(summary = "Create position", description = "Creates a new job position with basic salary and salary range (HR/Admin only)")
    public ApiResponse<PositionResponse> createPosition(
            @Valid @RequestBody PositionCreateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        PositionResponse response = positionService.createPosition(request);
        return ApiResponse.success(ResponseCode.CREATED, response, httpServletRequest.getRequestURI());
    }

    @GetMapping
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Search positions", description = "Supports pagination and search by keyword, department, or active status")
    public ApiResponse<Page<PositionResponse>> searchPositions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest httpServletRequest
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PositionResponse> response = positionService.searchPositions(keyword, departmentId, active, pageable);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @GetMapping("/export")
    @PreAuthorize(SecurityConstants.HAS_ROLE_MANAGER_OR_ABOVE)
    @Operation(summary = "Export positions to Excel", description = "Exports position list to Excel")
    public ResponseEntity<byte[]> exportPositions() throws IOException {
        List<Position> positions = positionRepository.findAll();
        byte[] excelBytes = excelExportService.exportPositionsToExcel(positions);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=positions.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @GetMapping("/active")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Get all active positions", description = "Fetches simple unpaginated list of active positions for UI dropdowns")
    public ApiResponse<List<PositionResponse>> getAllActivePositions(HttpServletRequest httpServletRequest) {
        List<PositionResponse> response = positionService.getAllActivePositions();
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @GetMapping("/department/{departmentId:[0-9]+}")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Get positions by department", description = "Fetches active positions under a specific department")
    public ApiResponse<List<PositionResponse>> getPositionsByDepartmentId(
            @PathVariable Long departmentId,
            HttpServletRequest httpServletRequest
    ) {
        List<PositionResponse> response = positionService.getPositionsByDepartmentId(departmentId);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @GetMapping("/{id:[0-9]+}")
    @PreAuthorize(SecurityConstants.HAS_ANY_ROLE)
    @Operation(summary = "Get position by ID", description = "Fetches position details by database ID")
    public ApiResponse<PositionResponse> getPositionById(
            @PathVariable Long id,
            HttpServletRequest httpServletRequest
    ) {
        PositionResponse response = positionService.getPositionById(id);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @PutMapping("/{id:[0-9]+}")
    @PreAuthorize(SecurityConstants.HAS_ROLE_HR_OR_ADMIN)
    @Operation(summary = "Update position", description = "Updates position details and salary ranges (HR/Admin only)")
    public ApiResponse<PositionResponse> updatePosition(
            @PathVariable Long id,
            @Valid @RequestBody PositionUpdateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        PositionResponse response = positionService.updatePosition(id, request);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @DeleteMapping("/{id:[0-9]+}")
    @PreAuthorize(SecurityConstants.HAS_ROLE_HR_OR_ADMIN)
    @Operation(summary = "Delete position", description = "Deletes a position (HR/Admin only)")
    public ApiResponse<Void> deletePosition(
            @PathVariable Long id,
            HttpServletRequest httpServletRequest
    ) {
        positionService.deletePosition(id);
        return ApiResponse.success(ResponseCode.DELETED, httpServletRequest.getRequestURI());
    }
}
