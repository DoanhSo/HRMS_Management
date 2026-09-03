import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Plus, Eye, Edit2, Trash2, FileSpreadsheet, UploadCloud, Download } from 'lucide-react';
import { toast } from 'sonner';
import { employeeApi } from '@/api/employee.api';
import { departmentApi } from '@/api/department.api';
import { positionApi } from '@/api/position.api';
import { useAuthStore } from '@/stores/authStore';
import { PageHeader } from '@/components/shared/PageHeader';
import { AdvancedFilterBar } from '@/components/shared/AdvancedFilterBar';
import { DataTable, ColumnDef } from '@/components/shared/DataTable';
import { StatusBadge } from '@/components/shared/StatusBadge';
import { ConfirmDialog } from '@/components/shared/ConfirmDialog';
import { ImportExcelModal } from '@/components/shared/ImportExcelModal';
import { formatDate } from '@/lib/utils';
import { DepartmentResponse, EmployeeResponse, EmploymentStatus, Gender, PositionResponse } from '@/types';

export const EmployeeListPage: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const hasAnyRole = useAuthStore((state) => state.hasAnyRole);
  const canManage = hasAnyRole('ADMIN', 'HR', 'ROLE_ADMIN', 'ROLE_HR');

  const [employees, setEmployees] = useState<EmployeeResponse[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(15);
  const [isLoading, setIsLoading] = useState(true);

  // Filters
  const [keyword, setKeyword] = useState('');
  const [departmentId, setDepartmentId] = useState<number | undefined>();
  const [positionId, setPositionId] = useState<number | undefined>();
  const [status, setStatus] = useState<EmploymentStatus | undefined>();
  const [gender, setGender] = useState<Gender | undefined>();
  const [hireDateFrom, setHireDateFrom] = useState<string>('');
  const [hireDateTo, setHireDateTo] = useState<string>('');

  // Dropdown options
  const [departments, setDepartments] = useState<DepartmentResponse[]>([]);
  const [positions, setPositions] = useState<PositionResponse[]>([]);

  // Modals & Export state
  const [isOpenImportModal, setIsOpenImportModal] = useState(false);
  const [isExporting, setIsExporting] = useState(false);
  const [deletingEmployee, setDeletingEmployee] = useState<EmployeeResponse | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  useEffect(() => {
    departmentApi.getAllActive().then(setDepartments).catch(console.warn);
    positionApi.getAllActive().then(setPositions).catch(console.warn);
  }, []);

  // Filter positions when department changes
  const filteredPositions = useMemo(() => {
    if (!departmentId) return positions;
    return positions.filter((p) => p.departmentId === departmentId);
  }, [positions, departmentId]);

  const loadEmployees = useCallback(async () => {
    setIsLoading(true);
    try {
      const res = await employeeApi.search({
        page: currentPage,
        size: pageSize,
        keyword: keyword || undefined,
        departmentId: departmentId,
        positionId: positionId,
        status: status,
        gender: gender,
        hireDateFrom: hireDateFrom || undefined,
        hireDateTo: hireDateTo || undefined,
        sortBy: 'id',
        sortDir: 'desc',
      });
      setEmployees(res.content);
      setTotalElements(res.totalElements);
      setTotalPages(res.totalPages);
    } catch (error) {
      console.error('Failed to load employees:', error);
      toast.error('Không thể tải danh sách nhân viên');
    } finally {
      setIsLoading(false);
    }
  }, [currentPage, pageSize, keyword, departmentId, positionId, status, gender, hireDateFrom, hireDateTo]);

  useEffect(() => {
    loadEmployees();
  }, [loadEmployees]);

  const handleExportExcel = async () => {
    setIsExporting(true);
    try {
      const blob = await employeeApi.exportEmployees({
        keyword: keyword || undefined,
        departmentId: departmentId,
        positionId: positionId,
        status: status,
        gender: gender,
        hireDateFrom: hireDateFrom || undefined,
        hireDateTo: hireDateTo || undefined,
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `danh_sach_nhan_vien_${new Date().toISOString().split('T')[0]}.xlsx`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      toast.success('Đã xuất file Excel nhân viên thành công');
    } catch (err) {
      console.error('Export error:', err);
      toast.error('Không thể xuất file Excel');
    } finally {
      setIsExporting(false);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deletingEmployee) return;
    setIsDeleting(true);
    try {
      await employeeApi.delete(deletingEmployee.id);
      toast.success(t('messages.deleteSuccess'));
      setDeletingEmployee(null);
      loadEmployees();
    } catch (error) {
      console.error('Failed to delete employee:', error);
      toast.error(t('messages.deleteError'));
    } finally {
      setIsDeleting(false);
    }
  };

  const getDeptName = (deptId?: number | null) => {
    if (!deptId) return '---';
    return departments.find((d) => d.id === deptId)?.name || `Dept #${deptId}`;
  };

  const getPosTitle = (posId?: number | null) => {
    if (!posId) return '---';
    return positions.find((p) => p.id === posId)?.title || `Pos #${posId}`;
  };

  const columns: ColumnDef<EmployeeResponse>[] = [
    {
      key: 'employeeCode',
      header: t('employee.code'),
      className: 'font-semibold text-gray-900',
      render: (row) => (
        <span className="font-mono text-xs text-blue-600 bg-blue-50 px-2 py-0.5 rounded">
          {row.employeeCode}
        </span>
      ),
    },
    {
      key: 'fullName',
      header: t('employee.fullName'),
      render: (row) => (
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-blue-600 to-indigo-500 text-white flex items-center justify-center font-medium text-xs">
            {row.firstName?.[0]}
            {row.lastName?.[0]}
          </div>
          <div>
            <p className="font-medium text-gray-900">{row.fullName}</p>
            <p className="text-xs text-gray-500">{row.phone || 'Chưa có SĐT'}</p>
          </div>
        </div>
      ),
    },
    {
      key: 'departmentId',
      header: t('employee.department'),
      render: (row) => (
        <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-slate-100 text-slate-700">
          {getDeptName(row.departmentId)}
        </span>
      ),
    },
    {
      key: 'positionId',
      header: t('employee.position'),
      render: (row) => getPosTitle(row.positionId),
    },
    {
      key: 'hireDate',
      header: t('employee.hireDate'),
      render: (row) => formatDate(row.hireDate),
    },
    {
      key: 'employmentStatus',
      header: t('employee.status'),
      render: (row) => <StatusBadge status={row.employmentStatus} />,
    },
    {
      key: 'actions',
      header: '',
      headerClassName: 'text-right',
      className: 'text-right',
      render: (row) => (
        <div className="flex items-center justify-end gap-1.5" onClick={(e) => e.stopPropagation()}>
          <button
            type="button"
            title={t('actions.viewDetail')}
            onClick={() => navigate(`/employees/${row.id}`)}
            className="p-1.5 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded-md transition-colors"
          >
            <Eye className="w-3.5 h-3.5" />
          </button>
          {canManage && (
            <>
              <button
                type="button"
                title={t('actions.edit')}
                onClick={() => navigate(`/employees/${row.id}/edit`)}
                className="p-1.5 text-gray-500 hover:text-amber-600 hover:bg-amber-50 rounded-md transition-colors"
              >
                <Edit2 className="w-3.5 h-3.5" />
              </button>
              {row.employmentStatus !== 'TERMINATED' && (
                <button
                  type="button"
                  title={t('actions.delete')}
                  onClick={() => setDeletingEmployee(row)}
                  className="p-1.5 text-gray-500 hover:text-rose-600 hover:bg-rose-50 rounded-md transition-colors"
                >
                  <Trash2 className="w-3.5 h-3.5" />
                </button>
              )}
            </>
          )}
        </div>
      ),
    },
  ];

  // Count active filters
  const activeFilterCount = [
    keyword,
    departmentId,
    positionId,
    status,
    gender,
    hireDateFrom,
    hireDateTo,
  ].filter((v) => v !== undefined && v !== '').length;

  const handleResetFilters = () => {
    setKeyword('');
    setDepartmentId(undefined);
    setPositionId(undefined);
    setStatus(undefined);
    setGender(undefined);
    setHireDateFrom('');
    setHireDateTo('');
    setCurrentPage(0);
  };

  return (
    <div className="space-y-4">
      <PageHeader
        title={t('employee.title')}
        subtitle={t('employee.subtitle')}
        action={
          <div className="flex items-center gap-2">
            {/* Export Excel Button */}
            <button
              type="button"
              onClick={handleExportExcel}
              disabled={isExporting}
              className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-medium text-gray-700 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 shadow-xs transition-colors disabled:opacity-50"
            >
              <Download className="w-3.5 h-3.5 text-gray-500" />
              <span>{isExporting ? 'Đang xuất...' : 'Xuất Excel'}</span>
            </button>

            {/* Import Excel Button */}
            {canManage && (
              <button
                type="button"
                onClick={() => setIsOpenImportModal(true)}
                className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-medium text-emerald-700 bg-emerald-50 border border-emerald-200 rounded-lg hover:bg-emerald-100 shadow-xs transition-colors"
              >
                <UploadCloud className="w-3.5 h-3.5 text-emerald-600" />
                <span>Nhập Excel</span>
              </button>
            )}

            {/* Create Button */}
            {canManage && (
              <button
                type="button"
                onClick={() => navigate('/employees/new')}
                className="inline-flex items-center gap-1.5 px-3.5 py-2 text-xs font-semibold text-white bg-blue-600 rounded-lg hover:bg-blue-700 shadow-xs transition-colors"
              >
                <Plus className="w-4 h-4" />
                <span>{t('employee.create')}</span>
              </button>
            )}
          </div>
        }
      />

      {/* Advanced Filter Bar */}
      <AdvancedFilterBar
        placeholder="Tìm kiếm theo Tên, Mã nhân viên, Số điện thoại..."
        searchTerm={keyword}
        onSearchChange={(val) => {
          setKeyword(val);
          setCurrentPage(0);
        }}
        activeFilterCount={activeFilterCount}
        onResetFilters={handleResetFilters}
      >
        {/* Department Filter */}
        <select
          value={departmentId || ''}
          onChange={(e) => {
            setDepartmentId(e.target.value ? Number(e.target.value) : undefined);
            setPositionId(undefined);
            setCurrentPage(0);
          }}
          className="text-xs bg-gray-50 border border-gray-200 rounded-lg px-2.5 py-2 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
        >
          <option value="">🏢 Tất cả phòng ban</option>
          {departments.map((d) => (
            <option key={d.id} value={d.id}>
              {d.name} ({d.code})
            </option>
          ))}
        </select>

        {/* Position Filter */}
        <select
          value={positionId || ''}
          onChange={(e) => {
            setPositionId(e.target.value ? Number(e.target.value) : undefined);
            setCurrentPage(0);
          }}
          className="text-xs bg-gray-50 border border-gray-200 rounded-lg px-2.5 py-2 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
        >
          <option value="">💼 Tất cả chức vụ</option>
          {filteredPositions.map((p) => (
            <option key={p.id} value={p.id}>
              {p.title}
            </option>
          ))}
        </select>

        {/* Status Filter */}
        <select
          value={status || ''}
          onChange={(e) => {
            setStatus((e.target.value as EmploymentStatus) || undefined);
            setCurrentPage(0);
          }}
          className="text-xs bg-gray-50 border border-gray-200 rounded-lg px-2.5 py-2 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
        >
          <option value="">⚡ Tất cả trạng thái</option>
          <option value="ACTIVE">Chính thức (Active)</option>
          <option value="PROBATION">Thử việc (Probation)</option>
          <option value="ON_LEAVE">Nghỉ phép dài hạn</option>
          <option value="TERMINATED">Đã nghỉ việc</option>
        </select>

        {/* Gender Filter */}
        <select
          value={gender || ''}
          onChange={(e) => {
            setGender((e.target.value as Gender) || undefined);
            setCurrentPage(0);
          }}
          className="text-xs bg-gray-50 border border-gray-200 rounded-lg px-2.5 py-2 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
        >
          <option value="">👤 Giới tính</option>
          <option value="MALE">Nam</option>
          <option value="FEMALE">Nữ</option>
          <option value="OTHER">Khác</option>
        </select>

        {/* Hire Date Range */}
        <div className="flex items-center gap-1.5 bg-gray-50 border border-gray-200 rounded-lg px-2 py-1">
          <span className="text-[11px] text-gray-500">Vào làm:</span>
          <input
            type="date"
            value={hireDateFrom}
            onChange={(e) => {
              setHireDateFrom(e.target.value);
              setCurrentPage(0);
            }}
            className="text-xs bg-transparent border-0 p-0 text-gray-700 focus:ring-0"
            title="Từ ngày"
          />
          <span className="text-gray-400 text-xs">-</span>
          <input
            type="date"
            value={hireDateTo}
            onChange={(e) => {
              setHireDateTo(e.target.value);
              setCurrentPage(0);
            }}
            className="text-xs bg-transparent border-0 p-0 text-gray-700 focus:ring-0"
            title="Đến ngày"
          />
        </div>
      </AdvancedFilterBar>

      {/* Table Data */}
      <DataTable
        columns={columns}
        data={employees}
        totalElements={totalElements}
        totalPages={totalPages}
        currentPage={currentPage}
        pageSize={pageSize}
        isLoading={isLoading}
        onPageChange={setCurrentPage}
        onRowClick={(row) => navigate(`/employees/${row.id}`)}
      />

      {/* Delete Confirmation */}
      <ConfirmDialog
        isOpen={!!deletingEmployee}
        title={t('actions.confirm')}
        description={`Bạn có chắc chắn muốn cho nghỉ việc/xóa nhân viên "${deletingEmployee?.fullName}" (${deletingEmployee?.employeeCode})?`}
        confirmText={t('actions.delete')}
        cancelText={t('actions.cancel')}
        isDestructive
        isLoading={isDeleting}
        onConfirm={handleDeleteConfirm}
        onClose={() => setDeletingEmployee(null)}
      />

      {/* Import Excel Modal */}
      <ImportExcelModal
        isOpen={isOpenImportModal}
        onClose={() => setIsOpenImportModal(false)}
        title="Nhập Danh Sách Nhân Viên Từ Excel"
        subtitle="Thêm hàng loạt nhân viên và tự động cấp tài khoản đăng nhập"
        templateFileName="employee_import_template.xlsx"
        onDownloadTemplate={() => employeeApi.downloadTemplate()}
        onImport={(file) => employeeApi.importEmployees(file)}
        onSuccess={loadEmployees}
      />
    </div>
  );
};
