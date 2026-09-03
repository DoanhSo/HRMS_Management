package com.ng_doanh.hr_management_system.notification.service;

import com.ng_doanh.hr_management_system.auth.entity.User;
import com.ng_doanh.hr_management_system.auth.repository.UserRepository;
import com.ng_doanh.hr_management_system.common.exception.BusinessException;
import com.ng_doanh.hr_management_system.notification.dto.response.NotificationResponse;
import com.ng_doanh.hr_management_system.notification.entity.Notification;
import com.ng_doanh.hr_management_system.notification.enums.NotificationType;
import com.ng_doanh.hr_management_system.notification.repository.NotificationRepository;
import com.ng_doanh.hr_management_system.notification.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Unit Tests")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User user;
    private Notification notification;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .build();
        user.setId(10L);

        notification = Notification.builder()
                .userId(10L)
                .type(NotificationType.LEAVE_APPROVED)
                .title("Đơn nghỉ phép đã duyệt")
                .message("Đơn nghỉ phép của bạn đã được phê duyệt")
                .link("/leave")
                .isRead(false)
                .build();
        notification.setId(1L);
    }

    @Test
    @DisplayName("Send notification persists to DB and sends WebSocket message")
    void send_Success() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        notificationService.send(10L, NotificationType.LEAVE_APPROVED, "Đơn nghỉ phép đã duyệt", "Nội dung", "/leave");

        verify(notificationRepository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSendToUser(eq("testuser"), eq("/queue/notifications"), any(NotificationResponse.class));
    }

    @Test
    @DisplayName("Send to multiple users loops through each user")
    void sendToMultipleUsers_Success() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        notificationService.sendToMultipleUsers(List.of(10L, 20L), NotificationType.PAYSLIP_READY, "Lương", "Nội dung", "/payroll");

        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Mark notification as read successfully")
    void markAsRead_Success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.markAsRead(1L, 10L);

        assertThat(notification.isRead()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("Mark notification as read throws Forbidden when notification belongs to another user")
    void markAsRead_Forbidden_WhenOtherUser() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.markAsRead(1L, 999L))
                .isInstanceOf(BusinessException.class);

        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Mark all as read executes repository batch update")
    void markAllAsRead_Success() {
        notificationService.markAllAsRead(10L);
        verify(notificationRepository).markAllAsReadByUserId(10L);
    }
}
