import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { ArrowLeft, Plus, X, Save, Download, UploadCloud, FileSpreadsheet } from 'lucide-react';
import { toast } from 'sonner';
import { attendanceApi } from '@/api/attendance.api';
import { departmentApi } from '@/api/department.api';
import { employeeApi } from '@/api/employee.api';
import { useAuthStore } from '@/stores/authStore';
import { PageHeader } from '@/components/shared/PageHeader';
import { AdvancedFilterBar } from '@/components/shared/AdvancedFilterBar';
import { DataTable, ColumnDef } from '@/components/shared/DataTable';
import { StatusBadge } from '@/components/shared/StatusBadge';
import { FormField } from '@/components/shared/FormField';
import { ImportExcelModal } from '@/components/shared/ImportExcelModal';
import { formatDate, formatDateTime } from '@/lib/utils';
import { AttendanceResponse, AttendanceStatus, DepartmentResponse } from '@/types';

export const AttendanceAdminPage: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const hasAnyRole = useAuthStore((state) => state.hasAnyRole);
  const canManage = hasAnyRole('ADMIN', 'HR', 'ROLE_ADMIN', 'ROLE_HR');

  const [attendances, setAttendances] = useState<AttendanceResponse[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(15);
  const [isLoading, setIsLoading] = useState(true);

  // Filters
  const [keyword, setKeyword] = useState('');
  const [departmentId, setDepartmentId] = useState<number | undefined>();
  const [status, setStatus] = useState<AttendanceStatus | undefined>();
  const [startDate, setStartDate] = useState<string>('');
  const [endDate, setEndDate] = useState<string>('');

  const [departments, setDepartments] = useState<DepartmentResponse[]>([]);
  const [employees, setEmployees] = useState<any[]>([]);

  // Export & Import states
  const [isOpenImportModal, setIsOpenImportModal] = useState(false);
  const [isExporting, setIsExporting] = useState(false);

  // Manual Entry Dialog
  const [isOpenManualModal, setIsOpenManualModal] = useState(false);
  const [manualForm, setManualForm] = useState({
    employeeId: undefined as number | undefined,
    workDate: new Date().toISOString().split('T')[0],
    checkIn: '',
    checkOut: '',
    status: 'PRESENT' as AttendanceStatus,
    notes: '',
  });
  const [isSavingManual, setIsSavingManual] = useState(false);

  useEffect(() => {
    departmentApi.getAllActive().then(setDepartments).catch(console.warn);
    employeeApi.search({ page: 0, size: 100 }).then((res) => setEmployees(res.content)).catch(console.warn);
  }, []);

  const loadAttendances = useCallback(async () => {
    setIsLoading(true);
    try {
      const res = await attendanceApi.search({
        page: currentPage,
        size: pageSize,
        keyword: keyword || undefined,
        departmentId: departmentId,
        status: status,
        startDate: startDate || undefined,
        endDate: endDate || undefined,
        sortBy: 'workDate',
        sortDir: 'desc',
      });
      setAttendances(res.content);
      setTotalElements(res.totalElements);
      setTotalPages(res.totalPages);
    } catch (err) {
      console.error('Failed to load company attendances:', err);
    } finally {
      setIsLoading(false);
    }
  }, [currentPage, pageSize, keyword, departmentId, status, startDate, endDate]);

  useEffect(() => {
    loadAttendances();
  }, [loadAttendances]);

  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const validateManualForm = (): boolean => {
    const errs: Record<string, string> = {};
    if (!manualForm.employeeId) {
      errs.employeeId = 'Vui lòng chọn nhân viên';
    }
    if (!manualForm.workDate) {
      errs.workDate = 'Vui lòng chọn ngày chấm công';
    } else {
      const selected = new Date(manualForm.workDate);
      const today = new Date();
      today.setHours(23, 59, 59, 999);
      if (selected > today) {
        errs.workDate = 'Không thể chấm công cho ngày trong tương lai';
      }
    }

    if (manualForm.checkIn && manualForm.checkOut && manualForm.checkOut <= manualForm.checkIn) {
      errs.checkOut = 'Giờ check-out phải sau giờ check-in';
    }

    setFieldErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleManualSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateManualForm()) {
      toast.error('Vui lòng kiểm tra lại các trường thông tin');
      return;
    }

    setIsSavingManual(true);
    try {
      await attendanceApi.manualEntry({
        employeeId: manualForm.employeeId!,
        workDate: manualForm.workDate,
        checkIn: manualForm.checkIn ? `${manualForm.workDate}T${manualForm.checkIn}:00` : undefined,
        checkOut: manualForm.checkOut ? `${manualForm.workDate}T${manualForm.checkOut}:00` : undefined,
        status: manualForm.status,
        notes: manualForm.notes?.trim() || undefined,
      });
      toast.success('Ghi nhận chấm công thủ công thành công');
      setIsOpenManualModal(false);
      setFieldErrors({});
      loadAttendances();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Không thể lưu chấm công thủ công');
    } finally {
      setIsSavingManual(false);
    }
  };

  const columns: ColumnDef<AttendanceResponse>[] = [
    {
      key: 'employeeCode',
      header: t('employee.employeeCode'),
      render: (row) => <span className="font-semibold text-gray-900 font-mono">{row.employeeCode}</span>,
    },
    {
      key: 'employeeName',
      header: t('employee.fullName'),
      render: (row) => <span className="font-medium text-gray-900">{row.employeeName}</span>,
    },
    {
      key: 'workDate',
      header: t('attendance.workDate'),
      render: (row) => formatDate(row.workDate),
    },
    {
      key: 'checkIn',
      header: t('attendance.checkInTime'),
      render: (row) => (row.checkIn ? formatDateTime(row.checkIn).split(' ')[1] : '-'),
    },
    {
      key: 'checkOut',
      header: t('attendance.checkOutTime'),
      render: (row) => (row.checkOut ? formatDateTime(row.checkOut).split(' ')[1] : '-'),
    },
    {
      key: 'totalWorkHours',
      header: t('attendance.workHours'),
      render: (row) => (row.totalWorkHours ? `${row.totalWorkHours}h` : '-'),
    },
    {
      key: 'status',
      header: t('employee.status'),
      render: (row) => <StatusBadge status={row.status} />,
    },
  ];

  const handleExportExcel = async () => {
    setIsExporting(true);
    try {
      const blob = await attendanceApi.exportAttendance({
        startDate: startDate || undefined,
        endDate: endDate || undefined,
        departmentId: departmentId,
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `bang_cham_cong_${startDate || 'all'}_den_${endDate || 'all'}.xlsx`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      toast.success('Đã xuất bảng chấm công ra file Excel');
    } catch (err) {
      console.error('Export error:', err);
      toast.error('Không thể xuất bảng chấm công');
    } finally {
      setIsExporting(false);
    }
  };

  const hasFilters = !!keyword || departmentId !== undefined || !!status || !!startDate || !!endDate;

  return (
    <div className="space-y-4 max-w-6xl">
      <PageHeader
        title={t('attendance.allAttendance')}
        subtitle="Tra cứu và điều chỉnh dữ liệu chấm công toàn công ty"
        action={
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => navigate('/attendance')}
              className="px-3 py-2 text-xs font-medium text-gray-600 hover:text-gray-900 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors flex items-center gap-1.5 shadow-2xs"
            >
              <ArrowLeft className="w-3.5 h-3.5" />
              <span>Chấm công cá nhân</span>
            </button>

            {/* Export Excel */}
            <button
              type="button"
              onClick={handleExportExcel}
              disabled={isExporting}
              className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-medium text-gray-700 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 shadow-xs transition-colors disabled:opacity-50"
            >
              <Download className="w-3.5 h-3.5 text-gray-500" />
              <span>{isExporting ? 'Đang xuất...' : 'Xuất Bảng Công'}</span>
            </button>

            {/* Import Biometric Excel */}
            {canManage && (
              <button
                type="button"
                onClick={() => setIsOpenImportModal(true)}
                className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-medium text-emerald-700 bg-emerald-50 border border-emerald-200 rounded-lg hover:bg-emerald-100 shadow-xs transition-colors"
              >
                <UploadCloud className="w-3.5 h-3.5 text-emerald-600" />
                <span>Nhập Máy Chấm Công</span>
              </button>
            )}

            {canManage && (
              <button
                type="button"
                onClick={() => setIsOpenManualModal(true)}
                className="px-3.5 py-2 bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold rounded-lg shadow-xs flex items-center gap-1.5 transition-colors"
              >
                <Plus className="w-4 h-4" />
                <span>{t('actions.manualEntry')}</span>
              </button>
            )}
          </div>
        }
      />

      {/* Advanced Filter Bar */}
      <AdvancedFilterBar
        searchTerm={keyword}
        onSearchChange={(val) => {
          setKeyword(val);
          setCurrentPage(0);
        }}
        placeholder="Tìm kiếm theo Mã, Họ tên nhân viên..."
        activeFilterCount={[
          keyword,
          departmentId,
          status,
          startDate,
          endDate,
        ].filter((v) => v !== undefined && v !== '').length}
        onResetFilters={() => {
          setKeyword('');
          setDepartmentId(undefined);
          setStatus(undefined);
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

        {/* Status Filter */}
        <select
          value={status ?? ''}
          onChange={(e) => {
            setStatus(e.target.value ? (e.target.value as AttendanceStatus) : undefined);
            setCurrentPage(0);
          }}
          className="text-xs bg-gray-50 border border-gray-200 rounded-lg px-2.5 py-2 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
        >
          <option value="">⚡ Tất cả trạng thái</option>
          <option value="PRESENT">Đúng giờ (PRESENT)</option>
          <option value="LATE">Đi muộn (LATE)</option>
          <option value="EARLY_LEAVE">Về sớm (EARLY_LEAVE)</option>
          <option value="LATE_AND_EARLY_LEAVE">Muộn & Về sớm</option>
          <option value="ABSENT">Vắng mặt (ABSENT)</option>
          <option value="ON_LEAVE">Nghỉ phép (ON_LEAVE)</option>
        </select>

        {/* Date Presets */}
        <div className="flex items-center gap-1">
          <button
            type="button"
            onClick={() => {
              const today = new Date().toISOString().split('T')[0];
              setStartDate(today);
              setEndDate(today);
              setCurrentPage(0);
            }}
            className="px-2 py-1.5 text-[11px] font-medium bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-lg transition-colors"
          >
            Hôm nay
          </button>
          <button
            type="button"
            onClick={() => {
              const now = new Date();
              const firstDay = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().split('T')[0];
              const lastDay = new Date(now.getFullYear(), now.getMonth() + 1, 0).toISOString().split('T')[0];
              setStartDate(firstDay);
              setEndDate(lastDay);
              setCurrentPage(0);
            }}
            className="px-2 py-1.5 text-[11px] font-medium bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-lg transition-colors"
          >
            Tháng này
          </button>
        </div>

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

      <DataTable
        columns={columns}
        data={attendances}
        totalElements={totalElements}
        totalPages={totalPages}
        currentPage={currentPage}
        pageSize={pageSize}
        onPageChange={setCurrentPage}
        isLoading={isLoading}
      />

      {/* Manual Attendance Entry Modal */}
      {isOpenManualModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-xs animate-in fade-in">
          <div className="bg-white rounded-xl max-w-md w-full p-6 shadow-xl border border-gray-200/80">
            <div className="flex items-center justify-between border-b border-gray-100 pb-3 mb-4">
              <h3 className="text-sm font-bold text-gray-900">{t('actions.manualEntry')}</h3>
              <button
                type="button"
                onClick={() => setIsOpenManualModal(false)}
                className="text-gray-400 hover:text-gray-600"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handleManualSave} className="space-y-4" noValidate>
              <FormField label={t('nav.employees')} error={fieldErrors.employeeId} required>
                <select
                  value={manualForm.employeeId ?? ''}
                  onChange={(e) => {
                    setManualForm({ ...manualForm, employeeId: e.target.value ? Number(e.target.value) : undefined });
                    if (fieldErrors.employeeId) setFieldErrors((prev) => ({ ...prev, employeeId: '' }));
                  }}
                  className={`w-full px-3 py-2 text-xs bg-white border rounded-lg focus:outline-none focus:ring-2 transition-all ${
                    fieldErrors.employeeId
                      ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                      : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                  }`}
                >
                  <option value="">-- {t('form.selectPlaceholder')} --</option>
                  {employees.map((emp) => (
                    <option key={emp.id} value={emp.id}>
                      {emp.firstName} {emp.lastName} ({emp.employeeCode})
                    </option>
                  ))}
                </select>
              </FormField>

              <FormField label={t('attendance.workDate')} error={fieldErrors.workDate} required>
                <input
                  type="date"
                  value={manualForm.workDate}
                  onChange={(e) => {
                    setManualForm({ ...manualForm, workDate: e.target.value });
                    if (fieldErrors.workDate) setFieldErrors((prev) => ({ ...prev, workDate: '' }));
                  }}
                  className={`w-full px-3 py-2 text-xs bg-white border rounded-lg focus:outline-none focus:ring-2 transition-all ${
                    fieldErrors.workDate
                      ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                      : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                  }`}
                />
              </FormField>

              <div className="grid grid-cols-2 gap-3">
                <FormField label={t('attendance.checkInTime')}>
                  <input
                    type="time"
                    value={manualForm.checkIn}
                    onChange={(e) => setManualForm({ ...manualForm, checkIn: e.target.value })}
                    className="w-full px-3 py-2 text-xs bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                  />
                </FormField>
                <FormField label={t('attendance.checkOutTime')} error={fieldErrors.checkOut}>
                  <input
                    type="time"
                    value={manualForm.checkOut}
                    onChange={(e) => {
                      setManualForm({ ...manualForm, checkOut: e.target.value });
                      if (fieldErrors.checkOut) setFieldErrors((prev) => ({ ...prev, checkOut: '' }));
                    }}
                    className={`w-full px-3 py-2 text-xs bg-white border rounded-lg focus:outline-none focus:ring-2 transition-all ${
                      fieldErrors.checkOut
                        ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                        : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                    }`}
                  />
                </FormField>
              </div>

              <FormField label={t('employee.status')} required>
                <select
                  value={manualForm.status}
                  onChange={(e) => setManualForm({ ...manualForm, status: e.target.value as AttendanceStatus })}
                  className="w-full px-3 py-2 text-xs bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                >
                  <option value="PRESENT">{t('status.PRESENT')}</option>
                  <option value="LATE">{t('status.LATE')}</option>
                  <option value="EARLY_LEAVE">{t('status.EARLY_LEAVE')}</option>
                  <option value="ABSENT">{t('status.ABSENT')}</option>
                  <option value="ON_LEAVE">{t('status.ON_LEAVE')}</option>
                </select>
              </FormField>

              <FormField label={t('attendance.notes')}>
                <textarea
                  value={manualForm.notes}
                  onChange={(e) => setManualForm({ ...manualForm, notes: e.target.value })}
                  rows={2}
                  placeholder="Lý do bổ sung công..."
                  className="w-full px-3 py-2 text-xs bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                />
              </FormField>

              <div className="flex items-center justify-end gap-2.5 pt-2 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => setIsOpenManualModal(false)}
                  disabled={isSavingManual}
                  className="px-4 py-2 text-xs font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50"
                >
                  {t('actions.cancel')}
                </button>
                <button
                  type="submit"
                  disabled={isSavingManual}
                  className="px-4 py-2 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg flex items-center gap-1.5 shadow-xs"
                >
                  {isSavingManual && <span className="w-3 h-3 border-2 border-white border-t-transparent rounded-full animate-spin" />}
                  <Save className="w-3.5 h-3.5" />
                  <span>{t('actions.save')}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Biometric Excel Import Modal */}
      <ImportExcelModal
        isOpen={isOpenImportModal}
        onClose={() => setIsOpenImportModal(false)}
        title="Nhập Dữ Liệu Máy Chấm Công (Biometric Import)"
        subtitle="Hỗ trợ nạp dữ liệu quẹt thẻ và tự động tính toán giờ làm, đi muộn, về sớm"
        templateFileName="attendance_import_template.xlsx"
        onDownloadTemplate={() => attendanceApi.downloadTemplate()}
        onImport={(file) => attendanceApi.importAttendance(file)}
        onSuccess={loadAttendances}
      />
    </div>
  );
};
