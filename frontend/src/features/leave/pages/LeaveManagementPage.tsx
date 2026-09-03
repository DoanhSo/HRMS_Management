import React, { useState, useEffect, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { Plus, Check, X, Ban, Calendar, AlertCircle, Save } from 'lucide-react';
import { toast } from 'sonner';
import { leaveApi } from '@/api/leave.api';
import { useAuthStore } from '@/stores/authStore';
import { PageHeader } from '@/components/shared/PageHeader';
import { DataTable, ColumnDef } from '@/components/shared/DataTable';
import { StatusBadge } from '@/components/shared/StatusBadge';
import { ConfirmDialog } from '@/components/shared/ConfirmDialog';
import { FormField } from '@/components/shared/FormField';
import { formatDate } from '@/lib/utils';
import { departmentApi } from '@/api/department.api';
import { AdvancedFilterBar } from '@/components/shared/AdvancedFilterBar';
import { DepartmentResponse, LeaveBalanceResponse, LeaveRequestResponse, LeaveRequestStatus, LeaveTypeResponse } from '@/types';

export const LeaveManagementPage: React.FC = () => {
  const { t } = useTranslation();
  const hasAnyRole = useAuthStore((state) => state.hasAnyRole);
  const isManagerOrAbove = hasAnyRole('ADMIN', 'HR', 'MANAGER', 'ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER');

  const [activeTab, setActiveTab] = useState<'my' | 'pending' | 'all'>('my');

  // Balances & Options
  const [balances, setBalances] = useState<LeaveBalanceResponse[]>([]);
  const [leaveTypes, setLeaveTypes] = useState<LeaveTypeResponse[]>([]);
  const [departments, setDepartments] = useState<DepartmentResponse[]>([]);

  // Filters
  const [keyword, setKeyword] = useState('');
  const [departmentId, setDepartmentId] = useState<number | undefined>();
  const [selectedLeaveTypeId, setSelectedLeaveTypeId] = useState<number | undefined>();
  const [selectedStatus, setSelectedStatus] = useState<LeaveRequestStatus | undefined>();
  const [startDate, setStartDate] = useState<string>('');
  const [endDate, setEndDate] = useState<string>('');

  // Requests table
  const [requests, setRequests] = useState<LeaveRequestResponse[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(15);
  const [isLoading, setIsLoading] = useState(true);

  // New Request Modal
  const [isOpenNewModal, setIsOpenNewModal] = useState(false);
  const [requestForm, setRequestForm] = useState({
    leaveTypeId: undefined as number | undefined,
    startDate: '',
    endDate: '',
    reason: '',
  });
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Reject Modal
  const [rejectingRequest, setRejectingRequest] = useState<LeaveRequestResponse | null>(null);
  const [rejectionReason, setRejectionReason] = useState('');
  const [isRejecting, setIsRejecting] = useState(false);

  // Action loading per row
  const [actionId, setActionId] = useState<number | null>(null);

  useEffect(() => {
    leaveApi.getMyBalances().then(setBalances).catch(console.warn);
    leaveApi.getActiveTypes().then(setLeaveTypes).catch(console.warn);
    departmentApi.getAllActive().then(setDepartments).catch(console.warn);
  }, []);

  const loadRequests = useCallback(async () => {
    setIsLoading(true);
    try {
      if (activeTab === 'my') {
        const res = await leaveApi.getMyRequests({ page: currentPage, size: pageSize });
        setRequests(res.content);
        setTotalElements(res.totalElements);
        setTotalPages(res.totalPages);
      } else if (activeTab === 'pending') {
        const res = await leaveApi.searchRequests({
          page: currentPage,
          size: pageSize,
          keyword: keyword || undefined,
          departmentId: departmentId,
          leaveTypeId: selectedLeaveTypeId,
          status: 'PENDING',
          startDate: startDate || undefined,
          endDate: endDate || undefined,
        });
        setRequests(res.content);
        setTotalElements(res.totalElements);
        setTotalPages(res.totalPages);
      } else {
        const res = await leaveApi.searchRequests({
          page: currentPage,
          size: pageSize,
          keyword: keyword || undefined,
          departmentId: departmentId,
          leaveTypeId: selectedLeaveTypeId,
          status: selectedStatus,
          startDate: startDate || undefined,
          endDate: endDate || undefined,
        });
        setRequests(res.content);
        setTotalElements(res.totalElements);
        setTotalPages(res.totalPages);
      }
    } catch (err) {
      console.error('Failed to load leave requests:', err);
    } finally {
      setIsLoading(false);
    }
  }, [activeTab, currentPage, pageSize, keyword, departmentId, selectedLeaveTypeId, selectedStatus, startDate, endDate]);

  useEffect(() => {
    loadRequests();
  }, [loadRequests]);

  const validateLeaveForm = (): boolean => {
    const errs: Record<string, string> = {};
    if (!requestForm.leaveTypeId) {
      errs.leaveTypeId = 'Vui lòng chọn loại nghỉ phép';
    }
    if (!requestForm.startDate) {
      errs.startDate = 'Vui lòng chọn ngày bắt đầu';
    }
    if (!requestForm.endDate) {
      errs.endDate = 'Vui lòng chọn ngày kết thúc';
    } else if (requestForm.startDate && new Date(requestForm.endDate) < new Date(requestForm.startDate)) {
      errs.endDate = 'Ngày kết thúc phải bằng hoặc sau ngày bắt đầu';
    }
    if (!requestForm.reason?.trim()) {
      errs.reason = 'Vui lòng nhập lý do xin nghỉ';
    } else if (requestForm.reason.trim().length < 5) {
      errs.reason = 'Lý do xin nghỉ phải có ít nhất 5 ký tự';
    }

    setFieldErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleCreateRequest = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateLeaveForm()) {
      toast.error('Vui lòng kiểm tra lại các trường thông tin');
      return;
    }

    setIsSubmitting(true);
    try {
      await leaveApi.createRequest({
        leaveTypeId: requestForm.leaveTypeId!,
        startDate: requestForm.startDate,
        endDate: requestForm.endDate,
        reason: requestForm.reason.trim(),
      });
      toast.success('Nộp đơn nghỉ phép thành công');
      setIsOpenNewModal(false);
      setRequestForm({ leaveTypeId: undefined, startDate: '', endDate: '', reason: '' });
      setFieldErrors({});
      leaveApi.getMyBalances().then(setBalances).catch(console.warn);
      loadRequests();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Không thể nộp đơn nghỉ phép');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleApprove = async (id: number) => {
    setActionId(id);
    try {
      await leaveApi.approveRequest(id);
      toast.success('Đã phê duyệt đơn nghỉ phép');
      loadRequests();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Không thể phê duyệt đơn');
    } finally {
      setActionId(null);
    }
  };

  const handleRejectConfirm = async () => {
    if (!rejectingRequest) return;
    setIsRejecting(true);
    try {
      await leaveApi.rejectRequest(rejectingRequest.id, {
        rejectionReason: rejectionReason || undefined,
      });
      toast.success('Đã từ chối đơn nghỉ phép');
      setRejectingRequest(null);
      setRejectionReason('');
      loadRequests();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Không thể từ chối đơn');
    } finally {
      setIsRejecting(false);
    }
  };

  const handleCancel = async (id: number) => {
    setActionId(id);
    try {
      await leaveApi.cancelRequest(id);
      toast.success('Đã hủy đơn xin nghỉ');
      loadRequests();
      leaveApi.getMyBalances().then(setBalances).catch(console.warn);
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Không thể hủy đơn');
    } finally {
      setActionId(null);
    }
  };

  const columns: ColumnDef<LeaveRequestResponse>[] = [
    {
      key: 'employeeName',
      header: t('employee.fullName'),
      render: (row) => (
        <div>
          <p className="font-medium text-gray-900">{row.employeeName}</p>
          <p className="text-[10px] text-gray-400 font-mono">{row.employeeCode}</p>
        </div>
      ),
    },
    {
      key: 'leaveTypeName',
      header: t('leave.leaveType'),
      render: (row) => <span className="font-medium text-gray-800">{row.leaveTypeName}</span>,
    },
    {
      key: 'dates',
      header: 'Thời gian nghỉ',
      render: (row) => (
        <div className="text-xs text-gray-700">
          <span>{formatDate(row.startDate)}</span>
          <span className="text-gray-400 mx-1">→</span>
          <span>{formatDate(row.endDate)}</span>
        </div>
      ),
    },
    {
      key: 'totalDays',
      header: t('leave.totalDays'),
      render: (row) => <span className="font-semibold text-gray-900">{row.totalDays} ngày</span>,
    },
    {
      key: 'reason',
      header: t('leave.reason'),
      render: (row) => (
        <span className="text-[11px] text-gray-500 truncate max-w-xs block" title={row.reason || ''}>
          {row.reason || '-'}
        </span>
      ),
    },
    {
      key: 'status',
      header: t('employee.status'),
      render: (row) => <StatusBadge status={row.status} />,
    },
    {
      key: 'actions',
      header: '',
      headerClassName: 'text-right',
      className: 'text-right',
      render: (row) => {
        const isPending = row.status === 'PENDING';
        return (
          <div className="flex items-center justify-end gap-1.5" onClick={(e) => e.stopPropagation()}>
            {/* Manager Approval Actions */}
            {isManagerOrAbove && isPending && (
              <>
                <button
                  type="button"
                  title={t('actions.approve')}
                  disabled={actionId === row.id}
                  onClick={() => handleApprove(row.id)}
                  className="p-1.5 text-emerald-600 hover:bg-emerald-50 rounded-md transition-colors disabled:opacity-40"
                >
                  <Check className="w-4 h-4" />
                </button>
                <button
                  type="button"
                  title={t('actions.reject')}
                  disabled={actionId === row.id}
                  onClick={() => setRejectingRequest(row)}
                  className="p-1.5 text-rose-600 hover:bg-rose-50 rounded-md transition-colors disabled:opacity-40"
                >
                  <X className="w-4 h-4" />
                </button>
              </>
            )}

            {/* Employee Cancel Own Request */}
            {activeTab === 'my' && isPending && (
              <button
                type="button"
                title={t('actions.cancel')}
                disabled={actionId === row.id}
                onClick={() => handleCancel(row.id)}
                className="text-xs text-gray-500 hover:text-rose-600 hover:bg-rose-50 px-2 py-1 rounded transition-colors"
              >
                <Ban className="w-3.5 h-3.5 inline mr-1" />
                <span>{t('actions.cancel')}</span>
              </button>
            )}
          </div>
        );
      },
    },
  ];

  return (
    <div className="space-y-6 max-w-6xl">
      <PageHeader
        title={t('leave.title')}
        subtitle={t('leave.subtitle')}
        action={
          <button
            type="button"
            onClick={() => setIsOpenNewModal(true)}
            className="px-3.5 py-2 bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold rounded-lg shadow-xs flex items-center gap-1.5 transition-colors"
          >
            <Plus className="w-4 h-4" />
            <span>{t('leave.newRequest')}</span>
          </button>
        }
      />

      {/* 1. Leave Balance Cards */}
      <div className="space-y-2">
        <h3 className="text-xs font-bold text-gray-900 uppercase tracking-wider">{t('leave.myBalances')}</h3>
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
          {balances.length === 0 ? (
            <div className="col-span-3 p-4 bg-white rounded-xl border border-gray-200 text-center text-xs text-gray-400">
              Chưa có số dư ngày nghỉ phép cho năm hiện tại
            </div>
          ) : (
            balances.map((b) => {
              const percent = b.totalDays > 0 ? (b.remainingDays / b.totalDays) * 100 : 0;
              return (
                <div
                  key={b.id}
                  className="bg-white p-5 rounded-xl border border-gray-200/80 shadow-[0_1px_3px_rgba(0,0,0,0.05)] space-y-3"
                >
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold text-gray-700">{b.leaveTypeName}</span>
                    <span className="text-xs font-semibold text-blue-600">{b.leaveTypeCode}</span>
                  </div>
                  <div className="flex items-baseline justify-between">
                    <div>
                      <span className="text-2xl font-bold text-gray-900">{b.remainingDays}</span>
                      <span className="text-xs text-gray-500 ml-1">/ {b.totalDays} ngày</span>
                    </div>
                    <span className="text-[11px] text-gray-400 font-medium">Đã dùng: {b.usedDays}</span>
                  </div>
                  {/* Progress Bar */}
                  <div className="w-full h-1.5 bg-gray-100 rounded-full overflow-hidden">
                    <div
                      className="h-full bg-blue-600 rounded-full transition-all duration-300"
                      style={{ width: `${Math.min(100, Math.max(0, percent))}%` }}
                    />
                  </div>
                </div>
              );
            })
          )}
        </div>
      </div>

      {/* 2. Tabs */}
      <div className="flex border-b border-gray-200 gap-6 text-xs font-semibold">
        <button
          type="button"
          onClick={() => {
            setActiveTab('my');
            setCurrentPage(0);
          }}
          className={`pb-3 transition-colors relative ${
            activeTab === 'my' ? 'text-blue-600' : 'text-gray-500 hover:text-gray-900'
          }`}
        >
          {t('leave.myRequests')}
          {activeTab === 'my' && <span className="absolute bottom-0 left-0 right-0 h-0.5 bg-blue-600 rounded-full" />}
        </button>

        {isManagerOrAbove && (
          <>
            <button
              type="button"
              onClick={() => {
                setActiveTab('pending');
                setCurrentPage(0);
              }}
              className={`pb-3 transition-colors relative ${
                activeTab === 'pending' ? 'text-blue-600' : 'text-gray-500 hover:text-gray-900'
              }`}
            >
              {t('leave.pendingApprovals')}
              {activeTab === 'pending' && (
                <span className="absolute bottom-0 left-0 right-0 h-0.5 bg-blue-600 rounded-full" />
              )}
            </button>

            <button
              type="button"
              onClick={() => {
                setActiveTab('all');
                setCurrentPage(0);
              }}
              className={`pb-3 transition-colors relative ${
                activeTab === 'all' ? 'text-blue-600' : 'text-gray-500 hover:text-gray-900'
              }`}
            >
              {t('leave.allRequests')}
              {activeTab === 'all' && (
                <span className="absolute bottom-0 left-0 right-0 h-0.5 bg-blue-600 rounded-full" />
              )}
            </button>
          </>
        )}
      </div>

      {/* Advanced Filter Bar */}
      {activeTab !== 'my' && (
        <AdvancedFilterBar
          placeholder="Tìm kiếm theo Tên nhân viên, Mã nhân viên, Lý do..."
          searchTerm={keyword}
          onSearchChange={(val) => {
            setKeyword(val);
            setCurrentPage(0);
          }}
          activeFilterCount={[
            keyword,
            departmentId,
            selectedLeaveTypeId,
            activeTab === 'all' ? selectedStatus : undefined,
            startDate,
            endDate,
          ].filter((v) => v !== undefined && v !== '').length}
          onResetFilters={() => {
            setKeyword('');
            setDepartmentId(undefined);
            setSelectedLeaveTypeId(undefined);
            setSelectedStatus(undefined);
            setStartDate('');
            setEndDate('');
            setCurrentPage(0);
          }}
        >
          {/* Department Filter */}
          <select
            value={departmentId ?? ''}
            onChange={(e) => {
              setDepartmentId(e.target.value ? Number(e.target.value) : undefined);
              setCurrentPage(0);
            }}
            className="text-xs bg-gray-50 border border-gray-200 rounded-lg px-2.5 py-2 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
          >
            <option value="">🏢 Tất cả phòng ban</option>
            {departments.map((d) => (
              <option key={d.id} value={d.id}>
                {d.name} ({d.code})
              </option>
            ))}
          </select>

          {/* Leave Type Filter */}
          <select
            value={selectedLeaveTypeId ?? ''}
            onChange={(e) => {
              setSelectedLeaveTypeId(e.target.value ? Number(e.target.value) : undefined);
              setCurrentPage(0);
            }}
            className="text-xs bg-gray-50 border border-gray-200 rounded-lg px-2.5 py-2 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
          >
            <option value="">📋 Tất cả loại phép</option>
            {leaveTypes.map((t) => (
              <option key={t.id} value={t.id}>
                {t.name}
              </option>
            ))}
          </select>

          {/* Status Filter (Only on 'all' tab) */}
          {activeTab === 'all' && (
            <select
              value={selectedStatus ?? ''}
              onChange={(e) => {
                setSelectedStatus(e.target.value ? (e.target.value as LeaveRequestStatus) : undefined);
                setCurrentPage(0);
              }}
              className="text-xs bg-gray-50 border border-gray-200 rounded-lg px-2.5 py-2 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
            >
              <option value="">⚡ Tất cả trạng thái</option>
              <option value="PENDING">Chờ duyệt (PENDING)</option>
              <option value="APPROVED">Đã duyệt (APPROVED)</option>
              <option value="REJECTED">Từ chối (REJECTED)</option>
              <option value="CANCELLED">Đã hủy (CANCELLED)</option>
            </select>
          )}

          {/* Date Range Picker */}
          <div className="flex items-center gap-1.5 bg-gray-50 border border-gray-200 rounded-lg px-2 py-1">
            <span className="text-[11px] text-gray-500">Từ:</span>
            <input
              type="date"
              value={startDate}
              onChange={(e) => {
                setStartDate(e.target.value);
                setCurrentPage(0);
              }}
              className="text-xs bg-transparent border-0 p-0 text-gray-700 focus:ring-0"
              title="Từ ngày"
            />
            <span className="text-gray-400 text-xs">Đến:</span>
            <input
              type="date"
              value={endDate}
              onChange={(e) => {
                setEndDate(e.target.value);
                setCurrentPage(0);
              }}
              className="text-xs bg-transparent border-0 p-0 text-gray-700 focus:ring-0"
              title="Đến ngày"
            />
          </div>
        </AdvancedFilterBar>
      )}

      {/* 3. Requests Table */}
      <DataTable
        columns={columns}
        data={requests}
        totalElements={totalElements}
        totalPages={totalPages}
        currentPage={currentPage}
        pageSize={pageSize}
        onPageChange={setCurrentPage}
        isLoading={isLoading}
      />

      {/* Create Leave Request Modal */}
      {isOpenNewModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-xs animate-in fade-in">
          <div className="bg-white rounded-xl max-w-md w-full p-6 shadow-xl border border-gray-200/80">
            <div className="flex items-center justify-between border-b border-gray-100 pb-3 mb-4">
              <h3 className="text-sm font-bold text-gray-900">{t('leave.newRequest')}</h3>
              <button
                type="button"
                onClick={() => setIsOpenNewModal(false)}
                className="text-gray-400 hover:text-gray-600"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handleCreateRequest} className="space-y-4" noValidate>
              <FormField label={t('leave.leaveType')} error={fieldErrors.leaveTypeId} required>
                <select
                  value={requestForm.leaveTypeId ?? ''}
                  onChange={(e) => {
                    setRequestForm({ ...requestForm, leaveTypeId: e.target.value ? Number(e.target.value) : undefined });
                    if (fieldErrors.leaveTypeId) setFieldErrors((prev) => ({ ...prev, leaveTypeId: '' }));
                  }}
                  className={`w-full px-3 py-2 text-xs bg-white border rounded-lg focus:outline-none focus:ring-2 transition-all ${
                    fieldErrors.leaveTypeId
                      ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                      : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                  }`}
                >
                  <option value="">-- {t('form.selectPlaceholder')} --</option>
                  {leaveTypes.map((t) => (
                    <option key={t.id} value={t.id}>
                      {t.name} ({t.code}) - {t.paid ? 'Có hưởng lương' : 'Không hưởng lương'}
                    </option>
                  ))}
                </select>
              </FormField>

              <div className="grid grid-cols-2 gap-3">
                <FormField label={t('leave.startDate')} error={fieldErrors.startDate} required>
                  <input
                    type="date"
                    value={requestForm.startDate}
                    onChange={(e) => {
                      setRequestForm({ ...requestForm, startDate: e.target.value });
                      if (fieldErrors.startDate) setFieldErrors((prev) => ({ ...prev, startDate: '' }));
                    }}
                    className={`w-full px-3 py-2 text-xs bg-white border rounded-lg focus:outline-none focus:ring-2 transition-all ${
                      fieldErrors.startDate
                        ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                        : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                    }`}
                  />
                </FormField>

                <FormField label={t('leave.endDate')} error={fieldErrors.endDate} required>
                  <input
                    type="date"
                    value={requestForm.endDate}
                    onChange={(e) => {
                      setRequestForm({ ...requestForm, endDate: e.target.value });
                      if (fieldErrors.endDate) setFieldErrors((prev) => ({ ...prev, endDate: '' }));
                    }}
                    className={`w-full px-3 py-2 text-xs bg-white border rounded-lg focus:outline-none focus:ring-2 transition-all ${
                      fieldErrors.endDate
                        ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                        : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                    }`}
                  />
                </FormField>
              </div>

              <FormField label={t('leave.reason')} error={fieldErrors.reason} required>
                <textarea
                  value={requestForm.reason}
                  onChange={(e) => {
                    setRequestForm({ ...requestForm, reason: e.target.value });
                    if (fieldErrors.reason) setFieldErrors((prev) => ({ ...prev, reason: '' }));
                  }}
                  rows={3}
                  placeholder="Lý do xin nghỉ phép chi tiết..."
                  className={`w-full px-3 py-2 text-xs bg-white border rounded-lg focus:outline-none focus:ring-2 transition-all ${
                    fieldErrors.reason
                      ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                      : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                  }`}
                />
              </FormField>

              <div className="flex items-center justify-end gap-2.5 pt-2 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => setIsOpenNewModal(false)}
                  disabled={isSubmitting}
                  className="px-4 py-2 text-xs font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50"
                >
                  {t('actions.cancel')}
                </button>
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="px-4 py-2 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg flex items-center gap-1.5 shadow-xs"
                >
                  {isSubmitting && <span className="w-3 h-3 border-2 border-white border-t-transparent rounded-full animate-spin" />}
                  <Save className="w-3.5 h-3.5" />
                  <span>{t('actions.submit')}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Reject Reason Dialog */}
      {rejectingRequest && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-xs animate-in fade-in">
          <div className="bg-white rounded-xl max-w-md w-full p-6 shadow-xl border border-gray-200/80">
            <div className="flex items-start gap-3 mb-4">
              <div className="p-2 rounded-full bg-rose-50 text-rose-600 shrink-0">
                <AlertCircle className="w-5 h-5" />
              </div>
              <div>
                <h3 className="text-sm font-bold text-gray-900">{t('leave.rejectDialogTitle')}</h3>
                <p className="text-xs text-gray-500 mt-0.5">
                  Từ chối đơn của nhân viên {rejectingRequest.employeeName}
                </p>
              </div>
            </div>

            <div className="space-y-3">
              <textarea
                value={rejectionReason}
                onChange={(e) => setRejectionReason(e.target.value)}
                rows={3}
                placeholder={t('leave.rejectReasonPlaceholder')}
                className="w-full px-3 py-2 text-xs bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-rose-500/20 focus:border-rose-500"
              />

              <div className="flex items-center justify-end gap-2.5">
                <button
                  type="button"
                  onClick={() => {
                    setRejectingRequest(null);
                    setRejectionReason('');
                  }}
                  disabled={isRejecting}
                  className="px-4 py-2 text-xs font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50"
                >
                  {t('actions.cancel')}
                </button>
                <button
                  type="button"
                  onClick={handleRejectConfirm}
                  disabled={isRejecting}
                  className="px-4 py-2 text-xs font-semibold text-white bg-rose-600 hover:bg-rose-700 rounded-lg flex items-center gap-1.5 shadow-xs"
                >
                  {isRejecting && <span className="w-3 h-3 border-2 border-white border-t-transparent rounded-full animate-spin" />}
                  <span>{t('actions.reject')}</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
