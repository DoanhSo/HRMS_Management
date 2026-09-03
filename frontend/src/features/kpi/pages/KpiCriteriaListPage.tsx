import { useState, useEffect } from 'react';
import { Target, Plus, Edit, Trash2, CheckCircle2, XCircle } from 'lucide-react';
import { kpiApi } from '@/api/kpi.api';
import { departmentApi } from '@/api/department.api';
import { KpiCriteriaResponse, KpiCriteriaCreateRequest, DepartmentResponse } from '@/types';
import { PageHeader } from '@/components/shared/PageHeader';
import { ConfirmDialog } from '@/components/shared/ConfirmDialog';
import { useAuthStore } from '@/stores/authStore';

export const KpiCriteriaListPage = () => {
  const { hasAnyRole } = useAuthStore();
  const isHrOrAdmin = hasAnyRole('ROLE_ADMIN', 'ROLE_HR');

  const [criteriaList, setCriteriaList] = useState<KpiCriteriaResponse[]>([]);
  const [departments, setDepartments] = useState<DepartmentResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  // Create / Edit Modal
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [formCode, setFormCode] = useState<string>('');
  const [formName, setFormName] = useState<string>('');
  const [formDeptId, setFormDeptId] = useState<number | ''>('');
  const [formWeight, setFormWeight] = useState<number>(20);
  const [formDesc, setFormDesc] = useState<string>('');
  const [formActive, setFormActive] = useState<boolean>(true);
  const [submitting, setSubmitting] = useState<boolean>(false);

  // Delete Dialog
  const [deleteDialog, setDeleteDialog] = useState<{ isOpen: boolean; id: number | null; name: string }>({
    isOpen: false,
    id: null,
    name: '',
  });

  const loadData = async () => {
    try {
      setLoading(true);
      const [crit, depts] = await Promise.all([
        kpiApi.getCriteria({ size: 100 }),
        departmentApi.getAllActive(),
      ]);
      setCriteriaList(crit.content || []);
      setDepartments(depts || []);
    } catch (err) {
      console.error('Error fetching criteria:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleOpenModal = (item?: KpiCriteriaResponse) => {
    if (item) {
      setEditingId(item.id);
      setFormCode(item.code);
      setFormName(item.name);
      setFormDeptId(item.departmentId || '');
      setFormWeight(item.weight);
      setFormDesc(item.targetDescription || '');
      setFormActive(item.active);
    } else {
      setEditingId(null);
      setFormCode('');
      setFormName('');
      setFormDeptId('');
      setFormWeight(20);
      setFormDesc('');
      setFormActive(true);
    }
    setIsModalOpen(true);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setSubmitting(true);
      if (editingId) {
        await kpiApi.updateCriteria(editingId, {
          name: formName,
          departmentId: formDeptId ? Number(formDeptId) : null,
          weight: formWeight,
          targetDescription: formDesc,
          active: formActive,
        });
      } else {
        const payload: KpiCriteriaCreateRequest = {
          code: formCode.trim().toUpperCase(),
          name: formName.trim(),
          departmentId: formDeptId ? Number(formDeptId) : null,
          weight: formWeight,
          targetDescription: formDesc,
          active: formActive,
        };
        await kpiApi.createCriteria(payload);
      }
      setIsModalOpen(false);
      loadData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Có lỗi xảy ra khi lưu tiêu chí KPI');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteDialog.id) return;
    try {
      await kpiApi.deleteCriteria(deleteDialog.id);
      setDeleteDialog({ isOpen: false, id: null, name: '' });
      loadData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Không thể xóa tiêu chí này');
    }
  };

  const totalWeight = criteriaList
    .filter((c) => c.active)
    .reduce((sum, c) => sum + c.weight, 0);

  return (
    <div className="space-y-6">
      <PageHeader
        title="Danh Mục Tiêu Chí KPI"
        subtitle="Thiết lập các tiêu chí đánh giá hiệu suất nhân sự và phân bổ trọng số (Tổng trọng số chuẩn: 100%)."
        action={
          isHrOrAdmin ? (
            <button
              onClick={() => handleOpenModal()}
              className="inline-flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white font-medium rounded-lg shadow-sm transition-colors"
            >
              <Plus className="w-4 h-4" />
              Thêm Tiêu Chí
            </button>
          ) : undefined
        }
      />

      {/* Weight Summary Banner */}
      <div className={`p-4 rounded-xl border flex items-center justify-between ${
        totalWeight === 100
          ? 'bg-emerald-50 border-emerald-200 text-emerald-800'
          : 'bg-amber-50 border-amber-200 text-amber-800'
      }`}>
        <div className="flex items-center gap-3">
          <Target className="w-5 h-5" />
          <div>
            <p className="text-xs font-bold uppercase tracking-wider">Tổng Trọng Số Các Tiêu Chí Đang Hoạt Động</p>
            <p className="text-sm font-semibold mt-0.5">
              {totalWeight}% {totalWeight === 100 ? '(Chuẩn 100% - Đã cân bằng)' : '(Khuyến nghị nên cân đối đủ 100%)'}
            </p>
          </div>
        </div>
        <span className="text-2xl font-black">{totalWeight}%</span>
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-200 text-xs font-bold text-slate-600 uppercase tracking-wider">
                <th className="py-3.5 px-4">Mã Tiêu Chí</th>
                <th className="py-3.5 px-4">Tên Tiêu Chí</th>
                <th className="py-3.5 px-4">Phòng Ban Áp Dụng</th>
                <th className="py-3.5 px-4 text-center">Trọng Số (%)</th>
                <th className="py-3.5 px-4">Mô Tả Mục Tiêu</th>
                <th className="py-3.5 px-4 text-center">Trạng Thái</th>
                {isHrOrAdmin && <th className="py-3.5 px-4 text-right">Thao Tác</th>}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-sm">
              {loading ? (
                <tr>
                  <td colSpan={7} className="py-12 text-center text-slate-500">
                    Đang tải danh mục tiêu chí...
                  </td>
                </tr>
              ) : criteriaList.length === 0 ? (
                <tr>
                  <td colSpan={7} className="py-12 text-center text-slate-500">
                    Chưa có tiêu chí KPI nào.
                  </td>
                </tr>
              ) : (
                criteriaList.map((item) => (
                  <tr key={item.id} className="hover:bg-slate-50/80 transition-colors">
                    <td className="py-3 px-4 font-mono font-semibold text-indigo-600 text-xs">
                      {item.code}
                    </td>
                    <td className="py-3 px-4 font-semibold text-slate-800">
                      {item.name}
                    </td>
                    <td className="py-3 px-4 text-slate-600">
                      {item.departmentName || <span className="text-slate-400 italic">Toàn công ty</span>}
                    </td>
                    <td className="py-3 px-4 text-center">
                      <span className="inline-block px-2.5 py-0.5 bg-indigo-50 text-indigo-700 font-bold text-xs rounded-md">
                        {item.weight}%
                      </span>
                    </td>
                    <td className="py-3 px-4 text-xs text-slate-500 max-w-xs truncate">
                      {item.targetDescription || '-'}
                    </td>
                    <td className="py-3 px-4 text-center">
                      {item.active ? (
                        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700">
                          <CheckCircle2 className="w-3 h-3" /> Hoạt động
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-slate-100 text-slate-600">
                          <XCircle className="w-3 h-3" /> Tạm dừng
                        </span>
                      )}
                    </td>
                    {isHrOrAdmin && (
                      <td className="py-3 px-4 text-right space-x-1.5 whitespace-nowrap">
                        <button
                          onClick={() => handleOpenModal(item)}
                          className="p-1.5 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                          title="Chỉnh sửa"
                        >
                          <Edit className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => setDeleteDialog({ isOpen: true, id: item.id, name: item.name })}
                          className="p-1.5 text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"
                          title="Xóa"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </td>
                    )}
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Create / Edit Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs animate-fadeIn">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg overflow-hidden border border-slate-100">
            <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-indigo-50/50">
              <h3 className="text-base font-bold text-slate-800">
                {editingId ? 'Cập Nhật Tiêu Chí KPI' : 'Thêm Tiêu Chí KPI Mới'}
              </h3>
              <button onClick={() => setIsModalOpen(false)} className="text-slate-400 hover:text-slate-600 font-bold">
                ✕
              </button>
            </div>

            <form onSubmit={handleSave} className="p-6 space-y-4">
              {!editingId && (
                <div>
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                    Mã Tiêu Chí <span className="text-rose-500">*</span>
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="VD: KPI_WORK_QUALITY"
                    value={formCode}
                    onChange={(e) => setFormCode(e.target.value)}
                    className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg font-mono uppercase focus:ring-2 focus:ring-indigo-500"
                  />
                </div>
              )}

              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                  Tên Tiêu Chí <span className="text-rose-500">*</span>
                </label>
                <input
                  type="text"
                  required
                  placeholder="VD: Chất lượng công việc & Độ chính xác"
                  value={formName}
                  onChange={(e) => setFormName(e.target.value)}
                  className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                    Phòng Ban Áp Dụng
                  </label>
                  <select
                    value={formDeptId}
                    onChange={(e) => setFormDeptId(e.target.value ? Number(e.target.value) : '')}
                    className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 bg-white"
                  >
                    <option value="">Toàn công ty</option>
                    {departments.map((d) => (
                      <option key={d.id} value={d.id}>{d.name}</option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                    Trọng Số (%) <span className="text-rose-500">*</span>
                  </label>
                  <input
                    type="number"
                    min={1}
                    max={100}
                    required
                    value={formWeight}
                    onChange={(e) => setFormWeight(Number(e.target.value))}
                    className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg font-bold focus:ring-2 focus:ring-indigo-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                  Mô Tả Mục Tiêu / Định Mức
                </label>
                <textarea
                  rows={3}
                  placeholder="Mô tả tiêu chuẩn hoàn thành công việc..."
                  value={formDesc}
                  onChange={(e) => setFormDesc(e.target.value)}
                  className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
                />
              </div>

              <div className="flex items-center gap-2 pt-1">
                <input
                  type="checkbox"
                  id="formActive"
                  checked={formActive}
                  onChange={(e) => setFormActive(e.target.checked)}
                  className="w-4 h-4 text-indigo-600 rounded-sm focus:ring-indigo-500"
                />
                <label htmlFor="formActive" className="text-sm font-semibold text-slate-700 cursor-pointer">
                  Kích hoạt tiêu chí này
                </label>
              </div>

              <div className="pt-4 flex items-center justify-end gap-3 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100 rounded-lg"
                >
                  Hủy Bỏ
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-semibold rounded-lg shadow-sm disabled:opacity-50"
                >
                  {submitting ? 'Đang lưu...' : 'Lưu Tiêu Chí'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete Dialog */}
      <ConfirmDialog
        isOpen={deleteDialog.isOpen}
        title="Xóa Tiêu Chí KPI"
        description={`Bạn có chắc chắn muốn xóa tiêu chí "${deleteDialog.name}" không?`}
        confirmText="Xóa Tiêu Chí"
        isDestructive={true}
        onConfirm={handleDelete}
        onClose={() => setDeleteDialog({ isOpen: false, id: null, name: '' })}
      />
    </div>
  );
};
