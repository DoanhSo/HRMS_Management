package com.ng_doanh.hr_management_system.notification.controller;

import com.ng_doanh.hr_management_system.common.constant.ApiPaths;
import com.ng_doanh.hr_management_system.common.dto.ApiResponse;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import com.ng_doanh.hr_management_system.common.security.CustomUserDetails;
import com.ng_doanh.hr_management_system.notification.dto.response.NotificationResponse;
import com.ng_doanh.hr_management_system.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.NOTIFICATIONS_BASE)
@RequiredArgsConstructor
@Tag(name = "Realtime Notifications", description = "APIs for in-app persistent notifications, unread count, and read status")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get my notifications", description = "Fetches paginated in-app notifications for the logged in user")
    public ApiResponse<Page<NotificationResponse>> getMyNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpServletRequest
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponse> response = notificationService.getMyNotifications(userDetails.getId(), pageable);
        return ApiResponse.success(ResponseCode.SUCCESS, response, httpServletRequest.getRequestURI());
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notifications count", description = "Returns the total number of unread notifications for badge count")
    public ApiResponse<Long> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpServletRequest
    ) {
        long count = notificationService.getUnreadCount(userDetails.getId());
        return ApiResponse.success(ResponseCode.SUCCESS, count, httpServletRequest.getRequestURI());
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Marks a specific notification as read")
    public ApiResponse<Void> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpServletRequest
    ) {
        notificationService.markAsRead(id, userDetails.getId());
        return ApiResponse.success(ResponseCode.SUCCESS, httpServletRequest.getRequestURI());
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read", description = "Marks all unread notifications for current user as read")
    public ApiResponse<Void> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest httpServletRequest
    ) {
        notificationService.markAllAsRead(userDetails.getId());
        return ApiResponse.success(ResponseCode.SUCCESS, httpServletRequest.getRequestURI());
    }
}
