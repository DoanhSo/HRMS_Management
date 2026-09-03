package com.ng_doanh.hr_management_system.notification.service;

import com.ng_doanh.hr_management_system.notification.dto.response.NotificationResponse;
import com.ng_doanh.hr_management_system.notification.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {

    void send(Long userId, NotificationType type, String title, String message, String link);

    void sendToMultipleUsers(List<Long> userIds, NotificationType type, String title, String message, String link);

    void sendToRole(String role, NotificationType type, String title, String message, String link);

    Page<NotificationResponse> getMyNotifications(Long userId, Pageable pageable);

    long getUnreadCount(Long userId);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);
}
