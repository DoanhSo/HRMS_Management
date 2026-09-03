import React, { useState, useEffect, useRef } from 'react';
import { Bell, CheckCheck, Calendar, Wallet, Clock, Info } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useNotificationStore } from '@/stores/notificationStore';
import { notificationApi } from '@/api/notification.api';
import { timeAgo } from '@/lib/utils';
import { NotificationResponse, NotificationType } from '@/types';

export const NotificationCenter: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const { notifications, unreadCount, setNotifications, setUnreadCount, markAsRead, markAllAsRead } =
    useNotificationStore();

  // Load initial notifications & count on mount
  useEffect(() => {
    notificationApi
      .getUnreadCount()
      .then(setUnreadCount)
      .catch((err) => console.warn('Failed to load unread notifications count:', err));

    notificationApi
      .getMyNotifications({ page: 0, size: 20 })
      .then((res) => setNotifications(res.content))
      .catch((err) => console.warn('Failed to load notifications list:', err));
  }, [setNotifications, setUnreadCount]);

  // Click outside to close dropdown
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target as Node)) {
        setIsOpen(false);
      }
    };
    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isOpen]);

  const handleNotificationClick = async (n: NotificationResponse) => {
    if (!n.isRead) {
      try {
        await notificationApi.markAsRead(n.id);
        markAsRead(n.id);
      } catch (err) {
        console.warn('Failed to mark notification as read:', err);
      }
    }
    setIsOpen(false);
    if (n.link) {
      navigate(n.link);
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await notificationApi.markAllAsRead();
      markAllAsRead();
    } catch (err) {
      console.warn('Failed to mark all as read:', err);
    }
  };

  const getNotificationIcon = (type: NotificationType) => {
    switch (type) {
      case 'LEAVE_SUBMITTED':
      case 'LEAVE_APPROVED':
      case 'LEAVE_REJECTED':
      case 'LEAVE_CANCELLED':
        return <Calendar className="w-4 h-4 text-blue-600" />;
      case 'PAYSLIP_READY':
        return <Wallet className="w-4 h-4 text-emerald-600" />;
      case 'ATTENDANCE_ABSENT':
        return <Clock className="w-4 h-4 text-rose-600" />;
      case 'SYSTEM':
      default:
        return <Info className="w-4 h-4 text-indigo-600" />;
    }
  };

  return (
    <div className="relative" ref={dropdownRef}>
      {/* Bell Button */}
      <button
        type="button"
        onClick={() => setIsOpen(!isOpen)}
        className="relative p-2 text-gray-500 hover:text-gray-900 rounded-lg hover:bg-gray-100 transition-colors focus:outline-none"
        aria-label={t('notification.title')}
      >
        <Bell className="w-5 h-5" />
        {unreadCount > 0 && (
          <span className="absolute top-1 right-1 flex items-center justify-center min-w-[18px] h-[18px] px-1 text-[10px] font-bold text-white bg-rose-500 rounded-full border-2 border-white shadow-xs">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        )}
      </button>

      {/* Dropdown Panel */}
      {isOpen && (
        <div className="absolute right-0 mt-2 w-80 sm:w-96 bg-white rounded-xl shadow-xl border border-gray-200/80 z-50 overflow-hidden animate-in fade-in-50 zoom-in-95 duration-100">
          {/* Header */}
          <div className="px-4 py-3 border-b border-gray-100 flex items-center justify-between bg-gray-50/50">
            <div className="flex items-center gap-2">
              <h4 className="text-xs font-bold text-gray-900 uppercase tracking-wider">{t('notification.title')}</h4>
              {unreadCount > 0 && (
                <span className="px-1.5 py-0.5 text-[10px] font-medium text-blue-700 bg-blue-50 rounded-full border border-blue-200/60">
                  {unreadCount}
                </span>
              )}
            </div>
            {unreadCount > 0 && (
              <button
                type="button"
                onClick={handleMarkAllRead}
                className="text-[11px] text-blue-600 hover:text-blue-700 font-medium flex items-center gap-1 transition-colors"
              >
                <CheckCheck className="w-3.5 h-3.5" />
                {t('actions.markAllRead')}
              </button>
            )}
          </div>

          {/* List */}
          <div className="max-h-[380px] overflow-y-auto divide-y divide-gray-100">
            {notifications.length === 0 ? (
              <div className="p-8 text-center text-xs text-gray-400">
                <Bell className="w-8 h-8 mx-auto text-gray-300 mb-2 stroke-[1.5]" />
                {t('notification.empty')}
              </div>
            ) : (
              notifications.map((n) => (
                <div
                  key={n.id}
                  onClick={() => handleNotificationClick(n)}
                  className={`p-3.5 transition-colors cursor-pointer flex items-start gap-3 hover:bg-gray-50/80 ${
                    !n.isRead ? 'bg-blue-50/40' : 'bg-white'
                  }`}
                >
                  <div className="p-2 rounded-lg bg-gray-50 border border-gray-100 shrink-0 mt-0.5">
                    {getNotificationIcon(n.type)}
                  </div>
                  <div className="flex-1 min-w-0 space-y-0.5">
                    <div className="flex items-center justify-between gap-1">
                      <p className="text-xs font-semibold text-gray-900 truncate">{n.title}</p>
                      {!n.isRead && <span className="w-2 h-2 rounded-full bg-blue-600 shrink-0" />}
                    </div>
                    <p className="text-xs text-gray-600 line-clamp-2 leading-relaxed">{n.message}</p>
                    <p className="text-[10px] text-gray-400 font-normal">{timeAgo(n.createdAt)}</p>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
};
