package com.ng_doanh.hr_management_system.notification.service.impl;

import com.ng_doanh.hr_management_system.auth.entity.User;
import com.ng_doanh.hr_management_system.auth.repository.UserRepository;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import com.ng_doanh.hr_management_system.common.exception.BusinessException;
import com.ng_doanh.hr_management_system.notification.dto.response.NotificationResponse;
import com.ng_doanh.hr_management_system.notification.entity.Notification;
import com.ng_doanh.hr_management_system.notification.enums.NotificationType;
import com.ng_doanh.hr_management_system.notification.repository.NotificationRepository;
import com.ng_doanh.hr_management_system.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public void send(Long userId, NotificationType type, String title, String message, String link) {
        if (userId == null) {
            log.warn("Cannot send notification: userId is null");
            return;
        }

        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .link(link)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        NotificationResponse response = toResponse(saved);

        // Send via WebSocket to user
        try {
            userRepository.findById(userId).ifPresent(user -> {
                messagingTemplate.convertAndSendToUser(user.getUsername(), "/queue/notifications", response);
                // Also broadcast on explicit topic
                messagingTemplate.convertAndSend("/topic/users/" + userId + "/notifications", response);
            });
            log.info("Realtime notification sent to user ID {}: {}", userId, title);
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification to user ID {}: {}", userId, e.getMessage());
        }
    }

    @Override
    @Transactional
    public void sendToMultipleUsers(List<Long> userIds, NotificationType type, String title, String message, String link) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        for (Long userId : userIds) {
            send(userId, type, title, message, link);
        }
    }

    @Override
    @Transactional
    public void sendToRole(String role, NotificationType type, String title, String message, String link) {
        String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        List<User> users = userRepository.findByRoleName(roleName);
        if (users.isEmpty()) {
            // Also try without ROLE_ prefix
            users = userRepository.findByRoleName(role);
        }

        for (User user : users) {
            send(user.getId(), type, title, message, link);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ResponseCode.FORBIDDEN);
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .link(notification.getLink())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
