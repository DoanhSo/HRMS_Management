package com.ng_doanh.hr_management_system.audit.controller;

import com.ng_doanh.hr_management_system.audit.entity.AuditLog;
import com.ng_doanh.hr_management_system.audit.repository.AuditLogRepository;
import com.ng_doanh.hr_management_system.common.constant.SecurityConstants;
import com.ng_doanh.hr_management_system.common.dto.ApiResponse;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "System activity and security auditing APIs (Admin only)")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    @PreAuthorize(SecurityConstants.HAS_ROLE_HR_OR_ADMIN)
    @Operation(summary = "Search system audit logs", description = "Fetches paginated activity audit logs with filters")
    public ApiResponse<Page<AuditLog>> searchAuditLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpServletRequest
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AuditLog> auditLogs = auditLogRepository.searchAuditLogs(username, action, entityName, startDate, endDate, pageable);
        return ApiResponse.success(ResponseCode.SUCCESS, auditLogs, httpServletRequest.getRequestURI());
    }
}
