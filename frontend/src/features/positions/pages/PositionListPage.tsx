import React, { useState, useEffect, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { Plus, Edit2, Trash2, X, Save, Download } from 'lucide-react';
import { toast } from 'sonner';
import { positionApi } from '@/api/position.api';
import { departmentApi } from '@/api/department.api';
import { useAuthStore } from '@/stores/authStore';
import { PageHeader } from '@/components/shared/PageHeader';
import { AdvancedFilterBar } from '@/components/shared/AdvancedFilterBar';
import { DataTable, ColumnDef } from '@/components/shared/DataTable';
import { StatusBadge } from '@/components/shared/StatusBadge';
import { ConfirmDialog } from '@/components/shared/ConfirmDialog';
import { FormField } from '@/components/shared/FormField';
import { formatCurrency } from '@/lib/utils';
import { DepartmentResponse, PositionResponse } from '@/types';

export const PositionListPage: React.FC = () => {
  const { t } = useTranslation();
  const hasAnyRole = useAuthStore((state) => state.hasAnyRole);
  const canManage = hasAnyRole('ADMIN', 'HR', 'ROLE_ADMIN', 'ROLE_HR');

  const [positions, setPositions] = useState<PositionResponse[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(15);
  const [isLoading, setIsLoading] = useState(true);

  // Filter
  const [keyword, setKeyword] = useState('');
  const [departmentId, setDepartmentId] = useState<number | undefined>();
  const [active, setActive] = useState<boolean | undefined>();
  const [departments, setDepartments] = useState<DepartmentResponse[]>([]);

  // Dialog Form
  const [isOpenModal, setIsOpenModal] = useState(false);
  const [editingPos, setEditingPos] = useState<PositionResponse | null>(null);
  const [formData, setFormData] = useState({
    code: '',
    title: '',
    departmentId: undefined as number | undefined,
    basicSalary: 0,
    minSalary: undefined as number | undefined,
    maxSalary: undefined as number | undefined,
    description: '',
  });
  const [isSaving, setIsSaving] = useState(false);

  // Delete
  const [deletingPos, setDeletingPos] = useState<PositionResponse | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  useEffect(() => {
    departmentApi.getAllActive().then(setDepartments).catch(console.warn);
  }, []);

  const loadPositions = useCallback(async () => {
    setIsLoading(true);
    try {
      const res = await positionApi.search({
        page: currentPage,
        size: pageSize,
        keyword: keyword || undefined,
        departmentId: departmentId,
        active: active,
      });
      setPositions(res.content);
      setTotalElements(res.totalElements);
      setTotalPages(res.totalPages);
    } catch (err) {
      console.error('Failed to load positions:', err);
      toast.error('Không thể tải danh sách chức vụ');
    } finally {
      setIsLoading(false);
    }
  }, [currentPage, pageSize, keyword, departmentId, active]);

  useEffect(() => {
    loadPositions();
  }, [loadPositions]);

  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const handleOpenCreate = () => {
    setEditingPos(null);
    setFieldErrors({});
    setFormData({
      code: '',
      title: '',
      departmentId: undefined,
      basicSalary: 10000000,
      minSalary: 8000000,
      maxSalary: 15000000,
      description: '',
    });
    setIsOpenModal(true);
  };

  const handleOpenEdit = (pos: PositionResponse) => {
    setEditingPos(pos);
    setFieldErrors({});
    setFormData({
      code: pos.code,
      title: pos.title,
      departmentId: pos.departmentId || undefined,
      basicSalary: pos.basicSalary,
      minSalary: pos.minSalary || undefined,
      maxSalary: pos.maxSalary || undefined,
      description: pos.description || '',
    });
    setIsOpenModal(true);
  };

  const validatePos = (): boolean => {
    const errs: Record<string, string> = {};
    if (!editingPos) {
      if (!formData.code.trim()) {
        errs.code = 'Mã chức vụ không được để trống';
      } else if (formData.code.trim().length < 2) {
        errs.code = 'Mã chức vụ phải có ít nhất 2 ký tự';
      } else if (!/^[A-Z0-9_-]+$/.test(formData.code.trim())) {
        errs.code = 'Mã chức vụ chỉ gồm chữ hoa, số và dấu gạch nối (VD: DEV_SR)';
      }
    }

    if (!formData.title.trim()) {
      errs.title = 'Tên chức vụ không được để trống';
    } else if (formData.title.trim().length < 2) {
      errs.title = 'Tên chức vụ phải có ít nhất 2 ký tự';
    }

    if (!formData.departmentId) {
      errs.departmentId = 'Vui lòng chọn phòng ban';
    }

    if (formData.basicSalary == null || formData.basicSalary <= 0) {
      errs.basicSalary = 'Lương cơ bản phải lớn hơn 0 ₫';
    }

    if (formData.minSalary != null && formData.maxSalary != null) {
      if (formData.maxSalary < formData.minSalary) {
        errs.maxSalary = 'Lương tối đa phải lớn hơn hoặc bằng lương tối thiểu';
      }
    }

    setFieldErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validatePos()) {
      toast.error('Vui lòng kiểm tra lại các trường thông tin');
      return;
    }

    setIsSaving(true);
    try {
      if (editingPos) {
        await positionApi.update(editingPos.id, {
          title: formData.title.trim(),
          departmentId: formData.departmentId,
          basicSalary: formData.basicSalary,
          minSalary: formData.minSalary,
          maxSalary: formData.maxSalary,
          description: formData.description?.trim() || undefined,
        });
        toast.success('Cập nhật chức vụ thành công');
      } else {
        await positionApi.create({
          code: formData.code.trim().toUpperCase(),
          title: formData.title.trim(),
          departmentId: formData.departmentId!,
          basicSalary: formData.basicSalary,
          minSalary: formData.minSalary,
          maxSalary: formData.maxSalary,
          description: formData.description?.trim() || undefined,
        });
        toast.success('Thêm chức vụ thành công');
      }
      setIsOpenModal(false);
      loadPositions();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Lỗi khi lưu chức vụ');
    } finally {
      setIsSaving(false);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deletingPos) return;
    setIsDeleting(true);
    try {
      await positionApi.delete(deletingPos.id);
      toast.success('Xóa chức vụ thành công');
      setDeletingPos(null);
      loadPositions();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Không thể xóa chức vụ này do có nhân sự đang đảm nhiệm');
    } finally {
      setIsDeleting(false);
    }
  };

  const columns: ColumnDef<PositionResponse>[] = [
    {
      key: 'code',
      header: t('position.code'),
      render: (row) => <span className="font-semibold text-gray-900 font-mono">{row.code}</span>,
    },
    {
      key: 'title',
      header: t('position.titleField'),
      render: (row) => (
        <div>
          <p className="font-medium text-gray-900">{row.title}</p>
          {row.description && <p className="text-[10px] text-gray-400 truncate max-w-xs">{row.description}</p>}
        </div>
      ),
    },
    {
      key: 'departmentName',
      header: t('position.department'),
      render: (row) => row.departmentName || <span className="text-gray-400">Chung</span>,
    },
    {
      key: 'basicSalary',
      header: t('position.basicSalary'),
      render: (row) => <span className="font-semibold text-gray-900">{formatCurrency(row.basicSalary)}</span>,
    },
    {
      key: 'range',
      header: 'Khung Lương',
      render: (row) => (
        <span className="text-[11px] text-gray-500 font-mono">
          {formatCurrency(row.minSalary)} - {formatCurrency(row.maxSalary)}
        </span>
      ),
    },
    {
      key: 'employeeCount',
      header: t('position.employeeCount'),
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
              onClick={() => setDeletingPos(row)}
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
      const blob = await positionApi.exportPositions();
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `danh_sach_chuc_vu_${new Date().toISOString().split('T')[0]}.xlsx`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      toast.success('Đã xuất danh sách chức vụ ra file Excel');
    } catch (err) {
      console.error('Export position error:', err);
      toast.error('Không thể xuất danh sách chức vụ');
    } finally {
      setIsExporting(false);
    }
  };

  return (
    <div className="space-y-4">
      <PageHeader
        title={t('position.title')}
        subtitle={t('position.subtitle')}
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
                <span>{t('position.addNew')}</span>
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
        placeholder="Tìm kiếm theo mã, chức danh..."
        activeFilterCount={[keyword, departmentId, active].filter((v) => v !== undefined && v !== '').length}
        onResetFilters={() => {
          setKeyword('');
          setDepartmentId(undefined);
          setActive(undefined);
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
        data={positions}
        totalElements={totalElements}
        totalPages={totalPages}
        currentPage={currentPage}
        pageSize={pageSize}
        onPageChange={setCurrentPage}
        isLoading={isLoading}
      />

      {/* Create / Edit Modal */}
      {isOpenModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-xs animate-in fade-in">
          <div className="bg-white rounded-xl max-w-lg w-full p-6 shadow-xl border border-gray-200/80 max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between border-b border-gray-100 pb-3 mb-4">
              <h3 className="text-sm font-bold text-gray-900">
                {editingPos ? 'Chỉnh sửa chức vụ' : 'Thêm chức vụ mới'}
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
              {!editingPos && (
                <FormField label={t('position.code')} error={fieldErrors.code} required helperText="Chữ hoa, số và gạch nối (VD: DEV_SR)">
                  <input
                    type="text"
                    value={formData.code}
                    onChange={(e) => {
                      setFormData({ ...formData, code: e.target.value });
                      if (fieldErrors.code) setFieldErrors((prev) => ({ ...prev, code: '' }));
                    }}
                    placeholder="VD: DEV_SR"
                    className={`w-full px-3 py-2 text-xs bg-white border rounded-lg focus:outline-none focus:ring-2 transition-all ${
                      fieldErrors.code
                        ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                        : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                    }`}
                  />
                </FormField>
              )}

              <FormField label={t('position.titleField')} error={fieldErrors.title} required>
                <input
                  type="text"
                  value={formData.title}
                  onChange={(e) => {
                    setFormData({ ...formData, title: e.target.value });
                    if (fieldErrors.title) setFieldErrors((prev) => ({ ...prev, title: '' }));
                  }}
                  placeholder="VD: Senior Frontend Developer"
                  className={`w-full px-3 py-2 text-xs bg-white border rounded-lg focus:outline-none focus:ring-2 transition-all ${
                    fieldErrors.title
                      ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                      : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                  }`}
                />
              </FormField>

              <FormField label={t('position.department')} error={fieldErrors.departmentId} required>
                <select
                  value={formData.departmentId ?? ''}
                  onChange={(e) => {
                    setFormData({ ...formData, departmentId: e.target.value ? Number(e.target.value) : undefined });
                    if (fieldErrors.departmentId) setFieldErrors((prev) => ({ ...prev, departmentId: '' }));
                  }}
                  className={`w-full px-3 py-2 text-xs bg-white border rounded-lg focus:outline-none focus:ring-2 transition-all ${
                    fieldErrors.departmentId
                      ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                      : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                  }`}
                >
                  <option value="">-- {t('form.selectPlaceholder')} --</option>
                  {departments.map((d) => (
                    <option key={d.id} value={d.id}>
                      {d.name}
                    </option>
                  ))}
                </select>
              </FormField>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <FormField label={`${t('position.basicSalary')} (VNĐ)`} error={fieldErrors.basicSalary} required>
                  <input
                    type="number"
                    value={formData.basicSalary}
                    onChange={(e) => {
                      setFormData({ ...formData, basicSalary: Number(e.target.value) });
                      if (fieldErrors.basicSalary) setFieldErrors((prev) => ({ ...prev, basicSalary: '' }));
                    }}
                    min={0}
                    step={500000}
                    className={`w-full px-3 py-2 text-xs bg-white border rounded-lg focus:outline-none focus:ring-2 transition-all ${
                      fieldErrors.basicSalary
                        ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                        : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                    }`}
                  />
                </FormField>
                <FormField label={t('position.minSalary')}>
                  <input
                    type="number"
                    value={formData.minSalary ?? ''}
                    onChange={(e) =>
                      setFormData({ ...formData, minSalary: e.target.value ? Number(e.target.value) : undefined })
                    }
                    min={0}
                    step={500000}
                    className="w-full px-3 py-2 text-xs bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500/20"
                  />
                </FormField>
                <FormField label={t('position.maxSalary')} error={fieldErrors.maxSalary}>
                  <input
                    type="number"
                    value={formData.maxSalary ?? ''}
                    onChange={(e) => {
                      setFormData({ ...formData, maxSalary: e.target.value ? Number(e.target.value) : undefined });
                      if (fieldErrors.maxSalary) setFieldErrors((prev) => ({ ...prev, maxSalary: '' }));
                    }}
                    min={0}
                    step={500000}
                    className={`w-full px-3 py-2 text-xs bg-white border rounded-lg focus:outline-none focus:ring-2 transition-all ${
                      fieldErrors.maxSalary
                        ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                        : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                    }`}
                  />
                </FormField>
              </div>

              <FormField label={t('department.description')}>
                <textarea
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  rows={2}
                  placeholder="Mô tả quyền hạn và trách nhiệm..."
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

      {/* Delete Confirm */}
      <ConfirmDialog
        isOpen={!!deletingPos}
        onClose={() => setDeletingPos(null)}
        onConfirm={handleDeleteConfirm}
        title={t('position.deleteConfirm')}
        description={`Bạn có chắc muốn xóa chức vụ ${deletingPos?.title}?`}
        isDestructive
        isLoading={isDeleting}
      />
    </div>
  );
};
