import { describe, it, expect, beforeEach } from 'vitest';
import { useNotificationStore } from './notificationStore';

describe('NotificationStore', () => {
  beforeEach(() => {
    useNotificationStore.setState({ notifications: [], unreadCount: 0 });
  });

  it('addNotification should prepend new notification and increment unreadCount', () => {
    useNotificationStore.getState().addNotification({
      id: 1,
      userId: 10,
      type: 'LEAVE_APPROVED',
      title: 'Đã duyệt',
      message: 'Đơn nghỉ phép đã duyệt',
      link: '/leave',
      isRead: false,
      createdAt: '2026-08-20T10:00:00',
    });

    const state = useNotificationStore.getState();
    expect(state.notifications).toHaveLength(1);
    expect(state.unreadCount).toBe(1);
    expect(state.notifications[0].id).toBe(1);
  });

  it('markAsRead should update isRead flag and decrease unread count', () => {
    useNotificationStore.setState({
      notifications: [
        {
          id: 1,
          userId: 10,
          type: 'PAYSLIP_READY',
          title: 'Lương',
          message: 'Đã có phiếu lương',
          isRead: false,
          createdAt: '2026-08-20T10:00:00',
        },
      ],
      unreadCount: 1,
    });

    useNotificationStore.getState().markAsRead(1);

    const state = useNotificationStore.getState();
    expect(state.notifications[0].isRead).toBe(true);
    expect(state.unreadCount).toBe(0);
  });

  it('markAllAsRead should mark all as read and reset count to 0', () => {
    useNotificationStore.setState({
      notifications: [
        { id: 1, userId: 10, type: 'SYSTEM', title: '1', message: '1', isRead: false, createdAt: '' },
        { id: 2, userId: 10, type: 'SYSTEM', title: '2', message: '2', isRead: false, createdAt: '' },
      ],
      unreadCount: 2,
    });

    useNotificationStore.getState().markAllAsRead();

    const state = useNotificationStore.getState();
    expect(state.unreadCount).toBe(0);
    expect(state.notifications.every((n) => n.isRead)).toBe(true);
  });
});
