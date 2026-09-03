import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Plus, Calculator, CheckCheck, Eye, X, Save, FileText } from 'lucide-react';
import { toast } from 'sonner';
import { payrollApi } from '@/api/payroll.api';
import { useAuthStore } from '@/stores/authStore';
import { PageHeader } from '@/components/shared/PageHeader';
import { DataTable, ColumnDef } from '@/components/shared/DataTable';
import { StatusBadge } from '@/components/shared/StatusBadge';
import { FormField } from '@/components/shared/FormField';
import { formatDate } from '@/lib/utils';
import { PayrollPeriodResponse } from '@/types';

export const PayrollPeriodsPage: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const hasAnyRole = useAuthStore((state) => state.hasAnyRole);
  const isHrOrAdmin = hasAnyRole('ADMIN', 'HR', 'ROLE_ADMIN', 'ROLE_HR');

  const [periods, setPeriods] = useState<PayrollPeriodResponse[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(15);
  const [isLoading, setIsLoading] = useState(true);

  // New Period Modal
  const [isOpenNewModal, setIsOpenNewModal] = useState(false);
  const [periodForm, setPeriodForm] = useState({
    name: '',
    year: new Date().getFullYear(),
    month: new Date().getMonth() + 1,
    startDate: '',
    endDate: '',
    workingDays: 22,
  });
  const [isSaving, setIsSaving] = useState(false);

  // Actions loading per row
  const [actionId, setActionId] = useState<number | null>(null);

  const loadPeriods = useCallback(async () => {
    setIsLoading(true);
    try {
      const res = await payrollApi.getPeriods({ page: currentPage, size: pageSize });
      setPeriods(res.content);
      setTotalElements(res.totalElements);
      setTotalPages(res.totalPages);
    } catch (err) {
      console.error('Failed to load payroll periods:', err);
    } finally {
      setIsLoading(false);
    }
  }, [currentPage, pageSize]);

  useEffect(() => {
    loadPeriods();
  }, [loadPeriods]);

  const handleOpenCreate = () => {
    const year = new Date().getFullYear();
    const month = new Date().getMonth() + 1;
    const firstDay = `${year}-${String(month).padStart(2, '0')}-01`;
    const lastDay = new Date(year, month, 0).toISOString().split('T')[0];

    setPeriodForm({
      name: `Kỳ Lương Tháng ${month}/${year}`,
      year,
      month,
      startDate: firstDay,
      endDate: lastDay,
      workingDays: 22,
    });
    setIsOpenNewModal(true);
  };

  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const validatePeriodForm = (): boolean => {
    const errs: Record<string, string> = {};
    if (!periodForm.name?.trim()) {
      errs.name = 'Vui lòng nhập tên kỳ lương';
    } else if (periodForm.name.trim().length < 3) {
      errs.name = 'Tên kỳ lương phải có ít nhất 3 ký tự';
    }

    if (!periodForm.month || periodForm.month < 1 || periodForm.month > 12) {
      errs.month = 'Tháng phải từ 1 đến 12';
    }

    if (!periodForm.year || periodForm.year < 2020 || periodForm.year > 2099) {
      errs.year = 'Năm không hợp lệ (2020 - 2099)';
    }

    if (!periodForm.startDate) {
      errs.startDate = 'Vui lòng chọn ngày bắt đầu';
    }

    if (!periodForm.endDate) {
      errs.endDate = 'Vui lòng chọn ngày kết thúc';
    } else if (periodForm.startDate && new Date(periodForm.endDate) < new Date(periodForm.startDate)) {
      errs.endDate = 'Ngày kết thúc phải bằng hoặc sau ngày bắt đầu';
    }

    if (!periodForm.workingDays || periodForm.workingDays < 1 || periodForm.workingDays > 31) {
      errs.workingDays = 'Số ngày công chuẩn phải từ 1 đến 31';
    }

    setFieldErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleCreatePeriod = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validatePeriodForm()) {
      toast.error('Vui lòng kiểm tra lại các trường thông tin');
      return;
    }

    setIsSaving(true);
    try {
      await payrollApi.createPeriod({
        name: periodForm.name?.trim() || undefined,
        year: Number(periodForm.year),
        month: Number(periodForm.month),
        startDate: periodForm.startDate,
        endDate: periodForm.endDate,
        workingDays: Number(periodForm.workingDays),
      });
      toast.success('Tạo kỳ lương mới thành công');
      setIsOpenNewModal(false);
      setFieldErrors({});
      loadPeriods();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Không thể tạo kỳ lương');
    } finally {
      setIsSaving(false);
    }
  };

  const handleCalculate = async (id: number) => {
    setActionId(id);
    try {
      await payrollApi.calculatePeriod(id);
      toast.success('Đã hoàn thành tính lương tự động cho toàn bộ nhân viên');
      loadPeriods();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Không thể tính lương');
    } finally {
      setActionId(null);
    }
  };

  const handleApprove = async (id: number) => {
    setActionId(id);
    try {
      await payrollApi.approvePeriod(id);
      toast.success('Đã phê duyệt kỳ lương! Thông báo và email đã được gửi đến nhân viên');
      loadPeriods();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Không thể phê duyệt kỳ lương');
    } finally {
      setActionId(null);
    }
  };

  const columns: ColumnDef<PayrollPeriodResponse>[] = [
    {
      key: 'name',
      header: t('payroll.periodName'),
      render: (row) => <span className="font-semibold text-gray-900">{row.name}</span>,
    },
    {
      key: 'monthYear',
      header: t('payroll.monthYear'),
      render: (row) => <span className="font-mono text-xs text-gray-700">T{row.month}/{row.year}</span>,
    },
    {
      key: 'dates',
      header: 'Thời gian áp dụng',
      render: (row) => (
        <span className="text-xs text-gray-600">
          {formatDate(row.startDate)} → {formatDate(row.endDate)}
        </span>
      ),
    },
    {
      key: 'workingDays',
      header: t('payroll.workingDays'),
      render: (row) => <span className="text-xs font-semibold text-gray-800">{row.workingDays} ngày</span>,
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
        const isBusy = actionId === row.id;
        return (
          <div className="flex items-center justify-end gap-2" onClick={(e) => e.stopPropagation()}>
            <button
              type="button"
              title="Xem phiếu lương"
              onClick={() => navigate(`/payroll/payslips?periodId=${row.id}`)}
              className="px-2 py-1 text-xs text-blue-600 hover:bg-blue-50 border border-blue-200 rounded-md transition-colors flex items-center gap-1"
            >
              <Eye className="w-3.5 h-3.5" />
              <span>Bảng lương</span>
            </button>

            {isHrOrAdmin && row.status === 'DRAFT' && (
              <button
                type="button"
                disabled={isBusy}
                onClick={() => handleCalculate(row.id)}
                className="px-2 py-1 text-xs text-amber-700 bg-amber-50 hover:bg-amber-100 border border-amber-200 rounded-md transition-colors flex items-center gap-1"
              >
                <Calculator className="w-3.5 h-3.5" />
                <span>{t('actions.calculate')}</span>
              </button>
            )}

            {isHrOrAdmin && row.status === 'CALCULATED' && (
              <>
                <button
                  type="button"
                  disabled={isBusy}
                  onClick={() => handleCalculate(row.id)}
                  className="px-2 py-1 text-xs text-gray-600 hover:bg-gray-100 border border-gray-200 rounded-md transition-colors"
                >
                  Tính lại
                </button>
                <button
                  type="button"
                  disabled={isBusy}
                  onClick={() => handleApprove(row.id)}
                  className="px-2 py-1 text-xs text-white bg-emerald-600 hover:bg-emerald-700 rounded-md transition-colors flex items-center gap-1 shadow-2xs"
                >
                  <CheckCheck className="w-3.5 h-3.5" />
                  <span>{t('actions.approve')}</span>
                </button>
              </>
            )}
          </div>
        );
      },
    },
  ];

  return (
    <div className="space-y-6 max-w-6xl">
      <PageHeader
        title={t('payroll.title')}
        subtitle={t('payroll.subtitle')}
        action={
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => navigate('/payroll/my-records')}
              className="px-3 py-1.5 text-xs font-medium text-gray-600 hover:text-gray-900 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors flex items-center gap-1.5 shadow-2xs"
            >
              <FileText className="w-3.5 h-3.5" />
              <span>{t('payroll.myPayslips')}</span>
            </button>
            {isHrOrAdmin && (
              <button
                type="button"
                onClick={handleOpenCreate}
                className="px-3.5 py-2 bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold rounded-lg shadow-xs flex items-center gap-1.5 transition-colors"
              >
                <Plus className="w-4 h-4" />
                <span>{t('payroll.newPeriod')}</span>
              </button>
            )}
          </div>
        }
      />

      <DataTable
        columns={columns}
        data={periods}
        totalElements={totalElements}
        totalPages={totalPages}
        currentPage={currentPage}
        pageSize={pageSize}
        onPageChange={setCurrentPage}
        isLoading={isLoading}
        onRowClick={(row) => navigate(`/payroll/payslips?periodId=${row.id}`)}
      />

      {/* New Period Modal */}
      {isOpenNewModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-xs animate-in fade-in">
          <div className="bg-white rounded-xl max-w-md w-full p-6 shadow-xl border border-gray-200/80">
            <div className="flex items-center justify-between border-b border-gray-100 pb-3 mb-4">
              <h3 className="text-sm font-bold text-gray-900">{t('payroll.newPeriod')}</h3>
              <button
                type="button"
                onClick={() => setIsOpenNewModal(false)}
                className="text-gray-400 hover:text-gray-600"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handleCreatePeriod} className="space-y-4" noValidate>
              <FormField label={t('payroll.periodName')} error={fieldErrors.name} required>
                <input
                  type="text"
                  value={periodForm.name}
                  onChange={(e) => {
                    setPeriodForm({ ...periodForm, name: e.target.value });
                    if (fieldErrors.name) setFieldErrors((prev) => ({ ...prev, name: '' }));
                  }}
                  placeholder="VD: Kỳ Lương Tháng 08/2026"
                  className={`w-full px-3 py-2 text-xs bg-white border rounded-lg focus:outline-none focus:ring-2 transition-all ${
                    fieldErrors.name
                      ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                      : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                  }`}
                />
              </FormField>

              <div className="grid grid-cols-2 gap-3">
                <FormField label="Tháng" error={fieldErrors.month} required>
                  <input
                    type="number"
                    min={1}
                    max={12}
                    value={periodForm.month}
                    onChange={(e) => {
                      setPeriodForm({ ...periodForm, month: Number(e.target.value) });
                      if (fieldErrors.month) setFieldErrors((prev) => ({ ...prev, month: '' }));
                    }}
                    className={`w-full px-3 py-2 text-xs bg-white border rounded-lg focus:outline-none focus:ring-2 transition-all ${
                      fieldErrors.month
                        ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                        : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                    }`}
                  />
                </FormField>

                <FormField label="Năm" error={fieldErrors.year} required>
                  <input
                    type="number"
                    min={2020}
                    max={2099}
                    value={periodForm.year}
                    onChange={(e) => {
                      setPeriodForm({ ...periodForm, year: Number(e.target.value) });
                      if (fieldErrors.year) setFieldErrors((prev) => ({ ...prev, year: '' }));
                    }}
                    className={`w-full px-3 py-2 text-xs bg-white border rounded-lg focus:outline-none focus:ring-2 transition-all ${
                      fieldErrors.year
                        ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                        : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                    }`}
                  />
                </FormField>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <FormField label="Từ ngày" error={fieldErrors.startDate} required>
                  <input
                    type="date"
                    value={periodForm.startDate}
                    onChange={(e) => {
                      setPeriodForm({ ...periodForm, startDate: e.target.value });
                      if (fieldErrors.startDate) setFieldErrors((prev) => ({ ...prev, startDate: '' }));
                    }}
                    className={`w-full px-3 py-2 text-xs bg-white border rounded-lg focus:outline-none focus:ring-2 transition-all ${
                      fieldErrors.startDate
                        ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                        : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                    }`}
                  />
                </FormField>

                <FormField label="Đến ngày" error={fieldErrors.endDate} required>
                  <input
                    type="date"
                    value={periodForm.endDate}
                    onChange={(e) => {
                      setPeriodForm({ ...periodForm, endDate: e.target.value });
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

              <FormField label={t('payroll.workingDays')} error={fieldErrors.workingDays} required>
                <input
                  type="number"
                  min={1}
                  max={31}
                  value={periodForm.workingDays}
                  onChange={(e) => {
                    setPeriodForm({ ...periodForm, workingDays: Number(e.target.value) });
                    if (fieldErrors.workingDays) setFieldErrors((prev) => ({ ...prev, workingDays: '' }));
                  }}
                  className={`w-full px-3 py-2 text-xs bg-white border rounded-lg focus:outline-none focus:ring-2 transition-all ${
                    fieldErrors.workingDays
                      ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                      : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                  }`}
                />
              </FormField>

              <div className="flex items-center justify-end gap-2.5 pt-2 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => setIsOpenNewModal(false)}
                  disabled={isSaving}
                  className="px-4 py-2 text-xs font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50"
                >
                  {t('actions.cancel')}
                </button>
                <button
                  type="submit"
                  disabled={isSaving}
                  className="px-4 py-2 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg flex items-center gap-1.5 shadow-xs"
                >
                  {isSaving && <span className="w-3 h-3 border-2 border-white border-t-transparent rounded-full animate-spin" />}
                  <Save className="w-3.5 h-3.5" />
                  <span>{t('actions.save')}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
