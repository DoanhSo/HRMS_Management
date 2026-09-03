import { useState, useEffect } from 'react';
import { Plus, Edit, Trash2, CheckCircle2, XCircle, Search } from 'lucide-react';
import { salaryScaleApi } from '@/api/salary-scale.api';
import { positionApi } from '@/api/position.api';
import { SalaryScaleResponse, SalaryScaleCreateRequest, PositionResponse } from '@/types';
import { PageHeader } from '@/components/shared/PageHeader';
import { ConfirmDialog } from '@/components/shared/ConfirmDialog';
import { useAuthStore } from '@/stores/authStore';

export const SalaryScaleListPage = () => {
  const { hasAnyRole } = useAuthStore();
  const isHrOrAdmin = hasAnyRole('ROLE_ADMIN', 'ROLE_HR');

  const [scales, setScales] = useState<SalaryScaleResponse[]>([]);
  const [positions, setPositions] = useState<PositionResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [keyword, setKeyword] = useState<string>('');
  const [positionId, setPositionId] = useState<number | undefined>(undefined);

  // Create / Edit Modal
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [formCode, setFormCode] = useState<string>('');
  const [formTitle, setFormTitle] = useState<string>('');
  const [formPositionId, setFormPositionId] = useState<number | ''>('');
  const [formCoeff, setFormCoeff] = useState<number>(1.0);
  const [formBaseSalary, setFormBaseSalary] = useState<number>(15000000);
  const [formBonus, setFormBonus] = useState<number>(2000000);
  const [formActive, setFormActive] = useState<boolean>(true);
  const [submitting, setSubmitting] = useState<boolean>(false);

  // Delete Dialog
  const [deleteDialog, setDeleteDialog] = useState<{ isOpen: boolean; id: number | null; title: string }>({
    isOpen: false,
    id: null,
    title: '',
  });

  const loadData = async () => {
    try {
      setLoading(true);
      const data = await salaryScaleApi.getSalaryScales({
        keyword: keyword || undefined,
        positionId,
        size: 50,
      });
      setScales(data.content || []);
    } catch (err) {
      console.error('Error fetching salary scales:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [positionId]);

  useEffect(() => {
    const fetchPositions = async () => {
      try {
        const pos = await positionApi.getAllActive();
        setPositions(pos || []);
      } catch (err) {
        console.error('Error fetching positions:', err);
      }
    };
    fetchPositions();
  }, []);

  const handleOpenModal = (item?: SalaryScaleResponse) => {
    if (item) {
      setEditingId(item.id);
      setFormCode(item.code);
      setFormTitle(item.title);
      setFormPositionId(item.positionId || '');
      setFormCoeff(item.coefficient);
      setFormBaseSalary(item.baseSalary);
      setFormBonus(item.standardBonus || 0);
      setFormActive(item.active);
    } else {
      setEditingId(null);
      setFormCode('');
      setFormTitle('');
      setFormPositionId('');
      setFormCoeff(1.0);
      setFormBaseSalary(15000000);
      setFormBonus(2000000);
      setFormActive(true);
    }
    setIsModalOpen(true);
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setSubmitting(true);
      if (editingId) {
        await salaryScaleApi.updateSalaryScale(editingId, {
          title: formTitle.trim(),
          positionId: formPositionId ? Number(formPositionId) : null,
          coefficient: formCoeff,
          baseSalary: formBaseSalary,
          standardBonus: formBonus,
          active: formActive,
        });
      } else {
        const payload: SalaryScaleCreateRequest = {
          code: formCode.trim().toUpperCase(),
          title: formTitle.trim(),
          positionId: formPositionId ? Number(formPositionId) : null,
          coefficient: formCoeff,
          baseSalary: formBaseSalary,
          standardBonus: formBonus,
          active: formActive,
        };
        await salaryScaleApi.createSalaryScale(payload);
      }
      setIsModalOpen(false);
      loadData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Có lỗi xảy ra khi lưu thang bảng lương');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!deleteDialog.id) return;
    try {
      await salaryScaleApi.deleteSalaryScale(deleteDialog.id);
      setDeleteDialog({ isOpen: false, id: null, title: '' });
      loadData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Không thể xóa bậc lương này');
    }
  };

  const formatVND = (amount?: number) => {
    if (!amount) return '0 ₫';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Thang Bảng Lương & Hệ Số Lương"
        subtitle="Quản lý các ngạch, bậc lương, hệ số lương cơ bản và mức thưởng định mức áp dụng cho nhân sự theo vị trí chức vụ."
        action={
          isHrOrAdmin ? (
            <button
              onClick={() => handleOpenModal()}
              className="inline-flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white font-medium rounded-lg shadow-sm transition-colors"
            >
              <Plus className="w-4 h-4" />
              Thêm Bậc Lương
            </button>
          ) : undefined
        }
      />

      {/* Filter */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-wrap items-center gap-3">
        <div className="flex items-center gap-2">
          <label className="text-xs font-semibold text-slate-600">Chức vụ:</label>
          <select
            value={positionId || ''}
            onChange={(e) => setPositionId(e.target.value ? Number(e.target.value) : undefined)}
            className="px-3 py-1.5 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 bg-white"
          >
            <option value="">Tất cả chức vụ</option>
            {positions.map((p) => (
              <option key={p.id} value={p.id}>{p.title}</option>
            ))}
          </select>
        </div>

        <div className="relative flex-1 min-w-[200px]">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
          <input
            type="text"
            placeholder="Tìm theo tên bậc lương, mã..."
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && loadData()}
            className="w-full pl-9 pr-3 py-1.5 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
          />
        </div>

        <button
          onClick={loadData}
          className="px-4 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 text-sm font-medium rounded-lg transition-colors"
        >
          Tìm kiếm
        </button>
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-200 text-xs font-bold text-slate-600 uppercase tracking-wider">
                <th className="py-3.5 px-4">Mã Bậc Lương</th>
                <th className="py-3.5 px-4">Tên Ngạch / Bậc Lương</th>
                <th className="py-3.5 px-4">Chức Vụ Áp Dụng</th>
                <th className="py-3.5 px-4 text-center">Hệ Số Lương</th>
                <th className="py-3.5 px-4 text-right">Lương Cơ Bản Gốc</th>
                <th className="py-3.5 px-4 text-right">Lương Sau Nhân Hệ Số</th>
                <th className="py-3.5 px-4 text-right">Thưởng Định Mức</th>
                <th className="py-3.5 px-4 text-center">Trạng Thái</th>
                {isHrOrAdmin && <th className="py-3.5 px-4 text-right">Thao Tác</th>}
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-sm">
              {loading ? (
                <tr>
                  <td colSpan={9} className="py-12 text-center text-slate-500">
                    Đang tải thang bảng lương...
                  </td>
                </tr>
              ) : scales.length === 0 ? (
                <tr>
                  <td colSpan={9} className="py-12 text-center text-slate-500">
                    Chưa có bậc lương nào trong hệ thống.
                  </td>
                </tr>
              ) : (
                scales.map((item) => (
                  <tr key={item.id} className="hover:bg-slate-50/80 transition-colors">
                    <td className="py-3 px-4 font-mono font-semibold text-indigo-600 text-xs">
                      {item.code}
                    </td>
                    <td className="py-3 px-4 font-semibold text-slate-800">
                      {item.title}
                    </td>
                    <td className="py-3 px-4 text-slate-600">
                      {item.positionTitle || <span className="text-slate-400 italic">Áp dụng chung</span>}
                    </td>
                    <td className="py-3 px-4 text-center">
                      <span className="inline-block px-2.5 py-0.5 bg-blue-50 text-blue-700 font-bold text-xs rounded-md">
                        {item.coefficient.toFixed(2)}x
                      </span>
                    </td>
                    <td className="py-3 px-4 text-right font-medium text-slate-700">
                      {formatVND(item.baseSalary)}
                    </td>
                    <td className="py-3 px-4 text-right font-bold text-emerald-600">
                      {formatVND(item.calculatedSalary)}
                    </td>
                    <td className="py-3 px-4 text-right text-slate-600">
                      {formatVND(item.standardBonus)}
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
                          onClick={() => setDeleteDialog({ isOpen: true, id: item.id, title: item.title })}
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
                {editingId ? 'Cập Nhật Bậc Lương & Hệ Số' : 'Thêm Bậc Lương & Hệ Số Mới'}
              </h3>
              <button onClick={() => setIsModalOpen(false)} className="text-slate-400 hover:text-slate-600 font-bold">
                ✕
              </button>
            </div>

            <form onSubmit={handleSave} className="p-6 space-y-4">
              {!editingId && (
                <div>
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                    Mã Bậc Lương <span className="text-rose-500">*</span>
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="VD: SCALE_DEV_L2"
                    value={formCode}
                    onChange={(e) => setFormCode(e.target.value)}
                    className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg font-mono uppercase focus:ring-2 focus:ring-indigo-500"
                  />
                </div>
              )}

              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                  Tên Ngạch / Bậc Lương <span className="text-rose-500">*</span>
                </label>
                <input
                  type="text"
                  required
                  placeholder="VD: Kỹ sư Phần mềm - Bậc 2"
                  value={formTitle}
                  onChange={(e) => setFormTitle(e.target.value)}
                  className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                    Chức Vụ Áp Dụng
                  </label>
                  <select
                    value={formPositionId}
                    onChange={(e) => setFormPositionId(e.target.value ? Number(e.target.value) : '')}
                    className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 bg-white"
                  >
                    <option value="">Áp dụng chung</option>
                    {positions.map((p) => (
                      <option key={p.id} value={p.id}>{p.title}</option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                    Hệ Số Lương <span className="text-rose-500">*</span>
                  </label>
                  <input
                    type="number"
                    step="0.05"
                    min="0.1"
                    required
                    value={formCoeff}
                    onChange={(e) => setFormCoeff(Number(e.target.value))}
                    className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg font-bold text-blue-600 focus:ring-2 focus:ring-indigo-500"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                    Lương Cơ Bản Gốc (VNĐ) <span className="text-rose-500">*</span>
                  </label>
                  <input
                    type="number"
                    min="0"
                    step="100000"
                    required
                    value={formBaseSalary}
                    onChange={(e) => setFormBaseSalary(Number(e.target.value))}
                    className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg font-medium focus:ring-2 focus:ring-indigo-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                    Thưởng Định Mức (VNĐ)
                  </label>
                  <input
                    type="number"
                    min="0"
                    step="100000"
                    value={formBonus}
                    onChange={(e) => setFormBonus(Number(e.target.value))}
                    className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg font-medium focus:ring-2 focus:ring-indigo-500"
                  />
                </div>
              </div>

              {/* Calculated preview */}
              <div className="p-3 bg-emerald-50 rounded-xl border border-emerald-200 flex items-center justify-between text-emerald-900">
                <span className="text-xs font-bold uppercase tracking-wider">Lương sau nhân hệ số:</span>
                <span className="text-base font-bold text-emerald-700">
                  {formatVND(formBaseSalary * formCoeff)}
                </span>
              </div>

              <div className="flex items-center gap-2 pt-1">
                <input
                  type="checkbox"
                  id="scaleActive"
                  checked={formActive}
                  onChange={(e) => setFormActive(e.target.checked)}
                  className="w-4 h-4 text-indigo-600 rounded-sm focus:ring-indigo-500"
                />
                <label htmlFor="scaleActive" className="text-sm font-semibold text-slate-700 cursor-pointer">
                  Kích hoạt bậc lương này
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
                  {submitting ? 'Đang lưu...' : 'Lưu Bậc Lương'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete Dialog */}
      <ConfirmDialog
        isOpen={deleteDialog.isOpen}
        title="Xóa Bậc Lương"
        description={`Bạn có chắc chắn muốn xóa bậc lương "${deleteDialog.title}" không?`}
        confirmText="Xóa Bậc Lương"
        isDestructive={true}
        onConfirm={handleDelete}
        onClose={() => setDeleteDialog({ isOpen: false, id: null, title: '' })}
      />
    </div>
  );
};
