import React from 'react';
import { NavLink } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
  LayoutDashboard,
  Users,
  Building2,
  Briefcase,
  Clock,
  CalendarDays,
  CalendarCheck,
  Wallet,
  FileDown,
  Shield,
  ShieldCheck,
  LogOut,
  UserCircle,
  KeyRound,
  UserCheck,
  Award,
  TrendingUp,
  Target,
} from 'lucide-react';
import { useAuthStore } from '@/stores/authStore';
import { cn } from '@/lib/utils';

interface SidebarProps {
  onCloseMobile?: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ onCloseMobile }) => {
  const { t } = useTranslation();
  const { user, logout, hasAnyRole } = useAuthStore();

  const isAdmin = hasAnyRole('ADMIN', 'ROLE_ADMIN');
  const isManagerOrAbove = hasAnyRole('ADMIN', 'HR', 'MANAGER', 'ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER');
  const isHrOrAdmin = hasAnyRole('ADMIN', 'HR', 'ROLE_ADMIN', 'ROLE_HR');

  const navItems = [
    {
      to: '/dashboard',
      label: t('nav.dashboard'),
      icon: LayoutDashboard,
      show: isManagerOrAbove,
    },
    {
      to: '/employees',
      label: t('nav.employees'),
      icon: Users,
      show: isManagerOrAbove,
    },
    {
      to: '/departments',
      label: t('nav.departments'),
      icon: Building2,
      show: true,
    },
    {
      to: '/positions',
      label: t('nav.positions'),
      icon: Briefcase,
      show: true,
    },
    {
      to: '/attendance',
      label: t('nav.attendance'),
      icon: Clock,
      show: true,
    },
    {
      to: '/attendance/admin',
      label: t('nav.attendanceAdmin', 'Bảng chấm công'),
      icon: CalendarCheck,
      show: isManagerOrAbove,
    },
    {
      to: '/leave',
      label: t('nav.leave'),
      icon: CalendarDays,
      show: true,
    },
    {
      to: '/payroll',
      label: t('nav.payroll'),
      icon: Wallet,
      show: true,
    },
    {
      to: '/salary-scales',
      label: t('nav.salaryScales', 'Thang Bảng Lương'),
      icon: TrendingUp,
      show: isHrOrAdmin,
    },
    {
      to: '/kpi',
      label: t('nav.kpi', 'Đánh Giá KPI'),
      icon: Award,
      show: true,
    },
    {
      to: '/kpi/criteria',
      label: t('nav.kpiCriteria', 'Tiêu Chí KPI'),
      icon: Target,
      show: isHrOrAdmin,
    },
    {
      to: '/reports',
      label: t('nav.reports'),
      icon: FileDown,
      show: isManagerOrAbove,
    },
    {
      to: '/audit-logs',
      label: t('nav.auditLogs'),
      icon: Shield,
      show: isHrOrAdmin,
    },
    {
      to: '/users',
      label: t('nav.users', 'Quản lý Tài khoản'),
      icon: UserCheck,
      show: isAdmin,
    },
  ];

  return (
    <aside className="flex flex-col h-full bg-white border-r border-gray-200/80 w-60 select-none">
      {/* Brand Header */}
      <div className="h-16 px-6 flex items-center gap-3 border-b border-gray-100">
        <div className="w-8 h-8 rounded-lg bg-blue-600 flex items-center justify-center text-white font-bold text-sm shadow-xs">
          HR
        </div>
        <div>
          <h2 className="text-sm font-bold text-gray-900 tracking-tight">{t('app.name')}</h2>
          <p className="text-[10px] text-gray-400 font-normal">{t('app.fullName')}</p>
        </div>
      </div>

      {/* Navigation Links */}
      <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
        {navItems
          .filter((item) => item.show)
          .map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.to}
                to={item.to}
                onClick={onCloseMobile}
                className={({ isActive }) =>
                  cn(
                    'flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-xs font-medium transition-all duration-150',
                    isActive
                      ? 'bg-blue-50 text-blue-700 font-semibold shadow-xs'
                      : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                  )
                }
              >
                <Icon className="w-4 h-4 shrink-0" />
                <span>{item.label}</span>
              </NavLink>
            );
          })}
      </nav>

      {/* User Profile & Actions Footer */}
      <div className="p-3 border-t border-gray-100 bg-gray-50/40">
        <div className="p-2 rounded-lg bg-white border border-gray-200/70 shadow-xs mb-2">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-full bg-gray-100 border border-gray-200 flex items-center justify-center text-gray-600 text-xs font-bold uppercase shrink-0">
              {user?.username ? user.username.slice(0, 2) : 'NV'}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-xs font-semibold text-gray-900 truncate">{user?.username}</p>
              <p className="text-[10px] text-gray-500 truncate">{user?.email}</p>
            </div>
          </div>
        </div>

        <div className="space-y-0.5">
          <NavLink
            to="/settings/change-password"
            onClick={onCloseMobile}
            className="flex items-center gap-2.5 px-3 py-2 text-xs font-medium text-gray-600 hover:text-gray-900 hover:bg-gray-100/70 rounded-lg transition-colors"
          >
            <KeyRound className="w-3.5 h-3.5" />
            <span>{t('nav.changePassword')}</span>
          </NavLink>
          <button
            type="button"
            onClick={logout}
            className="w-full flex items-center gap-2.5 px-3 py-2 text-xs font-medium text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"
          >
            <LogOut className="w-3.5 h-3.5" />
            <span>{t('nav.logout')}</span>
          </button>
        </div>
      </div>
    </aside>
  );
};
