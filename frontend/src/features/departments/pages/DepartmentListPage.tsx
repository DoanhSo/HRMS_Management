import React, { useState, useEffect, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { Plus, Edit2, Trash2, X, Save, Download } from 'lucide-react';
import { toast } from 'sonner';
import { departmentApi } from '@/api/department.api';
import { employeeApi } from '@/api/employee.api';
import { useAuthStore } from '@/stores/authStore';
import { PageHeader } from '@/components/shared/PageHeader';
import { AdvancedFilterBar } from '@/components/shared/AdvancedFilterBar';
import { DataTable, ColumnDef } from '@/components/shared/DataTable';
import { StatusBadge } from '@/components/shared/StatusBadge';
import { ConfirmDialog } from '@/components/shared/ConfirmDialog';
import { FormField } from '@/components/shared/FormField';
import { DepartmentResponse, EmployeeResponse } from '@/types';

export const DepartmentListPage: React.FC = () => {
  const { t } = useTranslation();
  const hasAnyRole = useAuthStore((state) => state.hasAnyRole);
  const canManage = hasAnyRole('ADMIN', 'HR', 'ROLE_ADMIN', 'ROLE_HR');

  const [departments, setDepartments] = useState<DepartmentResponse[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(15);
  const [isLoading, setIsLoading] = useState(true);
  const [keyword, setKeyword] = useState('');
  const [active, setActive] = useState<boolean | undefined>();

  // Dialog Form
  const [isOpenModal, setIsOpenModal] = useState(false);
  const [editingDept, setEditingDept] = useState<DepartmentResponse | null>(null);
  const [formData, setFormData] = useState({
    code: '',
    name: '',
    description: '',
    managerId: undefined as number | undefined,
    parentDepartmentId: undefined as number | undefined,
  });
  const [isSaving, setIsSaving] = useState(false);

  // Managers & Parents
  const [employees, setEmployees] = useState<any[]>([]);

  // Delete
  const [deletingDept, setDeletingDept] = useState<DepartmentResponse | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  useEffect(() => {
    employeeApi.search({ page: 0, size: 100 }).then((res) => setEmployees(res.content)).catch(console.warn);
  }, []);

  const loadDepartments = useCallback(async () => {
    setIsLoading(true);
    try {
      const res = await departmentApi.search({
        page: currentPage,
        size: pageSize,
        keyword: keyword || undefined,
        active: active,
      });
      setDepartments(res.content);
      setTotalElements(res.totalElements);
      setTotalPages(res.totalPages);
    } catch (err) {
      console.error('Failed to load departments:', err);
      toast.error('Không thể tải danh sách phòng ban');
    } finally {
      setIsLoading(false);
    }
  }, [currentPage, pageSize, keyword, active]);

  useEffect(() => {
    loadDepartments();
  }, [loadDepartments]);

  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const handleOpenCreate = () => {
    setEditingDept(null);
    setFieldErrors({});
    setFormData({
      code: '',
      name: '',
      description: '',
      managerId: undefined,
      parentDepartmentId: undefined,
    });
    setIsOpenModal(true);
  };

  const handleOpenEdit = (dept: DepartmentResponse) => {
    setEditingDept(dept);
    setFieldErrors({});
    setFormData({
      code: dept.code,
      name: dept.name,
      description: dept.description || '',
      managerId: dept.managerId || undefined,
      parentDepartmentId: dept.parentDepartmentId || undefined,
    });
    setIsOpenModal(true);
  };

  const validateDept = (): boolean => {
    const errs: Record<string, string> = {};
    if (!editingDept) {
      if (!formData.code.trim()) {
        errs.code = 'Mã phòng ban không được để trống';
      } else if (formData.code.trim().length < 2) {
        errs.code = 'Mã phòng ban phải có ít nhất 2 ký tự';
      } else if (!/^[A-Z0-9_-]+$/.test(formData.code.trim())) {
        errs.code = 'Mã phòng ban chỉ gồm chữ hoa, số và dấu gạch nối (VD: IT_DEV)';
      }
    }

    if (!formData.name.trim()) {
      errs.name = 'Tên phòng ban không được để trống';
    } else if (formData.name.trim().length < 2) {
      errs.name = 'Tên phòng ban phải có ít nhất 2 ký tự';
    }

    setFieldErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateDept()) {
      toast.error('Vui lòng kiểm tra lại các trường thông tin');
      return;
    }

    setIsSaving(true);
    try {
      if (editingDept) {
        await departmentApi.update(editingDept.id, {
          name: formData.name.trim(),
          description: formData.description?.trim() || undefined,
          managerId: formData.managerId,
          parentDepartmentId: formData.parentDepartmentId,
        });
        toast.success('Cập nhật phòng ban thành công');
      } else {
        await departmentApi.create({
          code: formData.code.trim().toUpperCase(),
          name: formData.name.trim(),
          description: formData.description?.trim() || undefined,
          managerId: formData.managerId,
          parentDepartmentId: formData.parentDepartmentId,
        });
        toast.success('Thêm phòng ban thành công');
      }
      setIsOpenModal(false);
      loadDepartments();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Lỗi khi lưu phòng ban');
    } finally {
      setIsSaving(false);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deletingDept) return;
    setIsDeleting(true);
    try {
      await departmentApi.delete(deletingDept.id);
      toast.success('Xóa phòng ban thành công');
      setDeletingDept(null);
      loadDepartments();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Không thể xóa phòng ban');
    } finally {
      setIsDeleting(false);
    }
  };

  const columns: ColumnDef<DepartmentResponse>[] = [
    {
      key: 'code',
      header: t('department.code'),
      render: (row) => <span className="font-semibold text-gray-900 font-mono">{row.code}</span>,
    },
    {
      key: 'name',
      header: t('department.name'),
      render: (row) => (
        <div>
          <p className="font-medium text-gray-900">{row.name}</p>
          {row.description && <p className="text-[10px] text-gray-400 truncate max-w-xs">{row.description}</p>}
        </div>
      ),
    },
    {
      key: 'managerName',
      header: t('department.manager'),
      render: (row) => row.managerName || <span className="text-gray-400">-</span>,
    },
    {
      key: 'employeeCount',
      header: t('department.employeeCount'),
      render: (row) => (
        <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold bg-gray-100 text-gray-700">
          {row.employeeCount ?? 0}
        </span>
      ),
    },
    {
      key: 'active',
      header: t('employee.status'),
      render: (row) => <StatusBadge status={row.active ? 'ACTIVE' : 'INACTIVE'} />,
    },
    {
      key: 'actions',
      header: '',
      headerClassName: 'text-right',
      className: 'text-right',
      render: (row) => (
        canManage && (
          <div className="flex items-center justify-end gap-1.5" onClick={(e) => e.stopPropagation()}>
            <button
              type="button"
              title={t('actions.edit')}
              onClick={() => handleOpenEdit(row)}
              className="p-1.5 text-gray-500 hover:text-amber-600 hover:bg-amber-50 rounded-md transition-colors"
            >
              <Edit2 className="w-3.5 h-3.5" />
            </button>
            <button
              type="button"
              title={t('actions.delete')}
              onClick={() => setDeletingDept(row)}
              className="p-1.5 text-gray-500 hover:text-rose-600 hover:bg-rose-50 rounded-md transition-colors"
            >
              <Trash2 className="w-3.5 h-3.5" />
            </button>
          </div>
        )
      ),
    },
  ];

  const [isExporting, setIsExporting] = useState(false);

  const handleExportExcel = async () => {
    setIsExporting(true);
    try {
      const blob = await departmentApi.exportDepartments();
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `danh_sach_phong_ban_${new Date().toISOString().split('T')[0]}.xlsx`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      toast.success('Đã xuất danh sách phòng ban ra file Excel');
    } catch (err) {
      console.error('Export department error:', err);
      toast.error('Không thể xuất danh sách phòng ban');
    } finally {
      setIsExporting(false);
    }
  };

  return (
    <div className="space-y-4">
      <PageHeader
        title={t('department.title')}
        subtitle={t('department.subtitle')}
        action={
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={handleExportExcel}
              disabled={isExporting}
              className="inline-flex items-center gap-1.5 px-3 py-2 text-xs font-medium text-gray-700 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 shadow-xs transition-colors disabled:opacity-50"
            >
              <Download className="w-3.5 h-3.5 text-gray-500" />
              <span>{isExporting ? 'Đang xuất...' : 'Xuất Excel'}</span>
            </button>

            {canManage && (
              <button
                type="button"
                onClick={handleOpenCreate}
                className="px-3.5 py-2 bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold rounded-lg shadow-xs flex items-center gap-1.5 transition-colors"
              >
                <Plus className="w-4 h-4" />
                <span>{t('department.addNew')}</span>
              </button>
            )}
          </div>
        }
      />

      <AdvancedFilterBar
        searchTerm={keyword}
        onSearchChange={(val) => {
          setKeyword(val);
          setCurrentPage(0);
        }}
        placeholder="Tìm kiếm theo mã, tên phòng ban..."
        activeFilterCount={[keyword, active].filter((v) => v !== undefined && v !== '').length}
        onResetFilters={() => {
          setKeyword('');
          setActive(undefined);
          setCurrentPage(0);
        }}
      >
        {/* Active Status Filter */}
        <select
          value={active === undefined ? '' : active ? 'true' : 'false'}
          onChange={(e) => {
            setActive(e.target.value === '' ? undefined : e.target.value === 'true');
            setCurrentPage(0);
          }}
          className="text-xs bg-gray-50 border border-gray-200 rounded-lg px-2.5 py-2 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
        >
          <option value="">⚡ Tất cả trạng thái</option>
          <option value="true">Đang hoạt động</option>
          <option value="false">Ngưng hoạt động</option>
        </select>
      </AdvancedFilterBar>

      <DataTable
        columns={columns}
        data={departments}
        totalElements={totalElements}
        totalPages={totalPages}
        currentPage={currentPage}
        pageSize={pageSize}
        onPageChange={setCurrentPage}
        isLoading={isLoading}
      />

      {/* Modal Create / Edit */}
      {isOpenModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-xs animate-in fade-in">
          <div className="bg-white rounded-xl max-w-lg w-full p-6 shadow-xl border border-gray-200/80">
            <div className="flex items-center justify-between border-b border-gray-100 pb-3 mb-4">
              <h3 className="text-sm font-bold text-gray-900">
                {editingDept ? 'Chỉnh sửa phòng ban' : 'Thêm phòng ban mới'}
              </h3>
              <button
                type="button"
                onClick={() => setIsOpenModal(false)}
                className="text-gray-400 hover:text-gray-600"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            <form onSubmit={handleSave} className="space-y-4" noValidate>
              {!editingDept && (
                <FormField label={t('department.code')} error={fieldErrors.code} required helperText="Chữ hoa, số và gạch nối (VD: IT_DEV)">
                  <input
                    type="text"
                    value={formData.code}
                    onChange={(e) => {
                      setFormData({ ...formData, code: e.target.value });
                      if (fieldErrors.code) setFieldErrors((prev) => ({ ...prev, code: '' }));
                    }}
                    placeholder="VD: IT_DEV"
                    className={`w-full px-3 py-2 text-xs bg-white border rounded-lg focus:outline-none focus:ring-2 transition-all ${
                      fieldErrors.code
                        ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                        : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                    }`}
                  />
                </FormField>
              )}

              <FormField label={t('department.name')} error={fieldErrors.name} required>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => {
                    setFormData({ ...formData, name: e.target.value });
                    if (fieldErrors.name) setFieldErrors((prev) => ({ ...prev, name: '' }));
                  }}
                  placeholder="VD: Phòng Công Nghệ Thông Tin"
                  className={`w-full px-3 py-2 text-xs bg-white border rounded-lg focus:outline-none focus:ring-2 transition-all ${
                    fieldErrors.name
                      ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                      : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                  }`}
                />
              </FormField>

              <FormField label={t('department.manager')}>
                <select
                  value={formData.managerId ?? ''}
                  onChange={(e) =>
                    setFormData({ ...formData, managerId: e.target.value ? Number(e.target.value) : undefined })
                  }
                  className="w-full px-3 py-2 text-xs bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                >
                  <option value="">-- {t('form.selectPlaceholder')} --</option>
                  {employees.map((emp) => (
                    <option key={emp.id} value={emp.id}>
                      {emp.firstName} {emp.lastName} ({emp.employeeCode})
                    </option>
                  ))}
                </select>
              </FormField>

              <FormField label={t('department.description')}>
                <textarea
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  rows={3}
                  placeholder="Mô tả chức năng nhiệm vụ..."
                  className="w-full px-3 py-2 text-xs bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                />
              </FormField>

              <div className="flex items-center justify-end gap-2.5 pt-2 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => setIsOpenModal(false)}
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

      {/* Delete Dialog */}
      <ConfirmDialog
        isOpen={!!deletingDept}
        onClose={() => setDeletingDept(null)}
        onConfirm={handleDeleteConfirm}
        title={t('department.deleteConfirm')}
        description={`Bạn có chắc muốn xóa phòng ban ${deletingDept?.name}?`}
        isDestructive
        isLoading={isDeleting}
      />
    </div>
  );
};
