import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Clock, LogIn, LogOut, Shield } from 'lucide-react';
import { toast } from 'sonner';
import { attendanceApi } from '@/api/attendance.api';
import { useAuthStore } from '@/stores/authStore';
import { PageHeader } from '@/components/shared/PageHeader';
import { DataTable, ColumnDef } from '@/components/shared/DataTable';
import { StatusBadge } from '@/components/shared/StatusBadge';
import { formatDate, formatDateTime } from '@/lib/utils';
import { AttendanceResponse } from '@/types';
import { CheckInConfirmModal } from '../components/CheckInConfirmModal';
import { CheckOutConfirmModal } from '../components/CheckOutConfirmModal';

export const AttendancePage: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const hasAnyRole = useAuthStore((state) => state.hasAnyRole);
  const isManagerOrAbove = hasAnyRole('ADMIN', 'HR', 'MANAGER', 'ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER');

  // Live Digital Clock
  const [currentTime, setCurrentTime] = useState(new Date());
  useEffect(() => {
    const timer = setInterval(() => setCurrentTime(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);

  const [history, setHistory] = useState<AttendanceResponse[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(15);
  const [isLoading, setIsLoading] = useState(true);

  // Today's record
  const [todayRecord, setTodayRecord] = useState<AttendanceResponse | null>(null);
  const [isActionLoading, setIsActionLoading] = useState(false);

  // Modals for confirmation
  const [isOpenCheckInModal, setIsOpenCheckInModal] = useState(false);
  const [isOpenCheckOutModal, setIsOpenCheckOutModal] = useState(false);

  const loadHistory = useCallback(async () => {
    setIsLoading(true);
    try {
      const res = await attendanceApi.getMyHistory({ page: currentPage, size: pageSize });
      setHistory(res.content);
      setTotalElements(res.totalElements);
      setTotalPages(res.totalPages);

      // Check if today already has attendance record
      const todayStr = new Date().toISOString().split('T')[0];
      const todayRec = res.content.find((a) => a.workDate === todayStr);
      setTodayRecord(todayRec || null);
    } catch (err) {
      console.error('Failed to load attendance history:', err);
    } finally {
      setIsLoading(false);
    }
  }, [currentPage, pageSize]);

  useEffect(() => {
    loadHistory();
  }, [loadHistory]);

  const handleCheckInConfirm = async (notes?: string) => {
    setIsActionLoading(true);
    try {
      const res = await attendanceApi.checkIn({ notes });
      toast.success('Chấm công vào làm thành công');
      setTodayRecord(res);
      setIsOpenCheckInModal(false);
      loadHistory();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Không thể chấm công vào');
    } finally {
      setIsActionLoading(false);
    }
  };

  const handleCheckOutConfirm = async (notes?: string) => {
    setIsActionLoading(true);
    try {
      const res = await attendanceApi.checkOut({ notes });
      toast.success('Chấm công kết thúc ca thành công');
      setTodayRecord(res);
      setIsOpenCheckOutModal(false);
      loadHistory();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Không thể chấm công ra');
    } finally {
      setIsActionLoading(false);
    }
  };

  const columns: ColumnDef<AttendanceResponse>[] = [
    {
      key: 'workDate',
      header: t('attendance.workDate'),
      render: (row) => <span className="font-semibold text-gray-900">{formatDate(row.workDate)}</span>,
    },
    {
      key: 'checkIn',
      header: t('attendance.checkInTime'),
      render: (row) => <span className="font-mono text-gray-700">{formatDateTime(row.checkIn)}</span>,
    },
    {
      key: 'checkOut',
      header: t('attendance.checkOutTime'),
      render: (row) => <span className="font-mono text-gray-700">{formatDateTime(row.checkOut)}</span>,
    },
    {
      key: 'totalWorkHours',
      header: t('attendance.workHours'),
      render: (row) => (
        <span className="font-semibold text-gray-900">
          {row.totalWorkHours !== null && row.totalWorkHours !== undefined ? `${row.totalWorkHours}h` : '-'}
        </span>
      ),
    },
    {
      key: 'status',
      header: t('attendance.status'),
      render: (row) => <StatusBadge status={row.status} />,
    },
    {
      key: 'notes',
      header: 'Ghi chú',
      render: (row) => <span className="text-gray-500 text-xs truncate max-w-xs block">{row.notes || '-'}</span>,
    },
  ];

  return (
    <div className="space-y-6 max-w-6xl">
      <PageHeader
        title={t('attendance.title')}
        subtitle={t('attendance.subtitle')}
        action={
          isManagerOrAbove ? (
            <button
              type="button"
              onClick={() => navigate('/attendance/admin')}
              className="px-3.5 py-2 bg-blue-50 text-blue-700 hover:bg-blue-100 font-semibold text-xs rounded-lg transition-colors flex items-center gap-1.5"
            >
              <Shield className="w-4 h-4" />
              <span>{t('attendance.manageAll')}</span>
            </button>
          ) : undefined
        }
      />

      {/* Check In / Check Out Action Card */}
      <div className="bg-white rounded-2xl border border-gray-200/80 p-6 shadow-xs">
        <div className="flex flex-col sm:flex-row items-center justify-between gap-6">
          {/* Live Clock Display */}
          <div className="flex items-center gap-4">
            <div className="w-14 h-14 rounded-2xl bg-blue-50 flex items-center justify-center text-blue-600">
              <Clock className="w-7 h-7" />
            </div>
            <div>
              <div className="text-2xl sm:text-3xl font-black text-gray-900 font-mono tracking-tight">
                {currentTime.toLocaleTimeString('vi-VN')}
              </div>
              <p className="text-xs font-medium text-gray-500 mt-0.5">
                {currentTime.toLocaleDateString('vi-VN', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })}
              </p>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex items-center gap-3 w-full sm:w-auto">
            {/* Check In Button */}
            <button
              type="button"
              disabled={isActionLoading || !!todayRecord?.checkIn}
              onClick={() => setIsOpenCheckInModal(true)}
              className={`flex-1 sm:flex-initial px-5 py-3 rounded-xl font-bold text-xs flex items-center justify-center gap-2 transition-all shadow-xs ${
                todayRecord?.checkIn
                  ? 'bg-gray-100 text-gray-400 cursor-not-allowed border border-gray-200'
                  : 'bg-emerald-600 hover:bg-emerald-700 text-white shadow-emerald-500/20 active:scale-[0.98]'
              }`}
            >
              <LogIn className="w-4 h-4" />
              <span>{todayRecord?.checkIn ? 'Đã Vào Ca' : t('actions.checkIn')}</span>
            </button>

            {/* Check Out Button */}
            <button
              type="button"
              disabled={isActionLoading || !todayRecord?.checkIn || !!todayRecord?.checkOut}
              onClick={() => setIsOpenCheckOutModal(true)}
              className={`flex-1 sm:flex-initial px-5 py-3 rounded-xl font-bold text-xs flex items-center justify-center gap-2 transition-all shadow-xs ${
                !todayRecord?.checkIn || todayRecord?.checkOut
                  ? 'bg-gray-100 text-gray-400 cursor-not-allowed border border-gray-200'
                  : 'bg-indigo-600 hover:bg-indigo-700 text-white shadow-indigo-500/20 active:scale-[0.98]'
              }`}
            >
              <LogOut className="w-4 h-4" />
              <span>{todayRecord?.checkOut ? 'Đã Ra Ca' : t('actions.checkOut')}</span>
            </button>
          </div>
        </div>

        {/* Today's Status Banner */}
        {todayRecord && (
          <div className="mt-5 pt-4 border-t border-gray-100 flex flex-wrap items-center justify-between gap-3 text-xs">
            <div className="flex items-center gap-4">
              <div>
                <span className="text-gray-400">Giờ vào: </span>
                <span className="font-mono font-bold text-gray-800">{formatDateTime(todayRecord.checkIn)}</span>
              </div>
              <div>
                <span className="text-gray-400">Giờ ra: </span>
                <span className="font-mono font-bold text-gray-800">{formatDateTime(todayRecord.checkOut)}</span>
              </div>
              {todayRecord.totalWorkHours ? (
                <div>
                  <span className="text-gray-400">Tổng giờ làm: </span>
                  <span className="font-bold text-blue-600">{todayRecord.totalWorkHours}h</span>
                </div>
              ) : null}
            </div>
            <StatusBadge status={todayRecord.status} />
          </div>
        )}
      </div>

      {/* Confirmation Modals */}
      <CheckInConfirmModal
        isOpen={isOpenCheckInModal}
        onClose={() => setIsOpenCheckInModal(false)}
        onConfirm={handleCheckInConfirm}
        isLoading={isActionLoading}
      />

      <CheckOutConfirmModal
        isOpen={isOpenCheckOutModal}
        onClose={() => setIsOpenCheckOutModal(false)}
        onConfirm={handleCheckOutConfirm}
        checkInTime={todayRecord?.checkIn}
        isLoading={isActionLoading}
      />

      {/* Attendance History */}
      <div className="space-y-3">
        <h3 className="text-sm font-bold text-gray-900">{t('attendance.historyTitle')}</h3>
        <DataTable
          columns={columns}
          data={history}
          totalElements={totalElements}
          totalPages={totalPages}
          currentPage={currentPage}
          pageSize={pageSize}
          onPageChange={setCurrentPage}
          isLoading={isLoading}
        />
      </div>
    </div>
  );
};
