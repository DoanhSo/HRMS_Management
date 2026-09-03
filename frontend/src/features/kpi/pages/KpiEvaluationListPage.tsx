import { useState, useEffect } from 'react';
import {
  Award,
  Search,
  Plus,
  CheckCircle,
  XCircle,
  Eye,
  Edit,
  TrendingUp,
  Filter,
  DollarSign,
  UserCheck,
} from 'lucide-react';
import { kpiApi } from '@/api/kpi.api';
import { employeeApi } from '@/api/employee.api';
import { departmentApi } from '@/api/department.api';
import {
  KpiEvaluationResponse,
  KpiCriteriaResponse,
  KpiRating,
  KpiEvaluationStatus,
  EmployeeResponse,
  DepartmentResponse,
  KpiEvaluationCreateRequest,
} from '@/types';
import { PageHeader } from '@/components/shared/PageHeader';
import { ConfirmDialog } from '@/components/shared/ConfirmDialog';
import { useAuthStore } from '@/stores/authStore';

export const KpiEvaluationListPage = () => {
  const { hasAnyRole } = useAuthStore();
  const isManagerOrAdmin = hasAnyRole('ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER');

  const currentDate = new Date();
  const [year, setYear] = useState<number>(currentDate.getFullYear());
  const [month, setMonth] = useState<number>(currentDate.getMonth() + 1);
  const [departmentId, setDepartmentId] = useState<number | undefined>(undefined);
  const [status, setStatus] = useState<KpiEvaluationStatus | undefined>(undefined);
  const [keyword, setKeyword] = useState<string>('');

  const [evaluations, setEvaluations] = useState<KpiEvaluationResponse[]>([]);
  const [departments, setDepartments] = useState<DepartmentResponse[]>([]);
  const [employees, setEmployees] = useState<EmployeeResponse[]>([]);
  const [activeCriteria, setActiveCriteria] = useState<KpiCriteriaResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  // Evaluation Modal
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [selectedEmployeeId, setSelectedEmployeeId] = useState<number | ''>('');
  const [criterionScores, setCriterionScores] = useState<{ [key: number]: number }>({});
  const [criterionComments, setCriterionComments] = useState<{ [key: number]: string }>({});
  const [feedback, setFeedback] = useState<string>('');
  const [submitting, setSubmitting] = useState<boolean>(false);

  // Detail Modal
  const [viewEvaluation, setViewEvaluation] = useState<KpiEvaluationResponse | null>(null);

  // Action Dialog
  const [confirmDialog, setConfirmDialog] = useState<{
    isOpen: boolean;
    type: 'approve' | 'reject';
    evalId: number | null;
    employeeName: string;
  }>({
    isOpen: false,
    type: 'approve',
    evalId: null,
    employeeName: '',
  });

  const loadData = async () => {
    try {
      setLoading(true);
      const data = await kpiApi.getEvaluations({
        year,
        month,
        departmentId,
        status,
        keyword: keyword || undefined,
        size: 50,
      });
      setEvaluations(data.content || []);
    } catch (error) {
      console.error('Error fetching evaluations:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [year, month, departmentId, status]);

  useEffect(() => {
    const fetchMetadata = async () => {
      try {
        const [depts, crit, emps] = await Promise.all([
          departmentApi.getAllActive(),
          kpiApi.getActiveCriteria(),
          employeeApi.search({ size: 100 }),
        ]);
        setDepartments(depts || []);
        setActiveCriteria(crit || []);
        setEmployees(emps.content || []);
      } catch (err) {
        console.error('Error fetching metadata:', err);
      }
    };
    fetchMetadata();
  }, []);

  const handleOpenCreateModal = (empId?: number) => {
    setSelectedEmployeeId(empId || '');
    const initialScores: { [key: number]: number } = {};
    const initialComments: { [key: number]: string } = {};
    activeCriteria.forEach((c) => {
      initialScores[c.id] = 80;
      initialComments[c.id] = '';
    });
    setCriterionScores(initialScores);
    setCriterionComments(initialComments);
    setFeedback('');
    setIsModalOpen(true);
  };

  const handleScoreChange = (criteriaId: number, val: number) => {
    setCriterionScores((prev) => ({
      ...prev,
      [criteriaId]: Math.max(0, Math.min(100, val)),
    }));
  };

  // Live score calculation
  const calculatedTotalScore = activeCriteria.reduce((acc, c) => {
    const score = criterionScores[c.id] || 0;
    const weight = c.weight || 20;
    return acc + (score * weight) / 100;
  }, 0);

  const getEstimatedRating = (score: number): { rating: KpiRating; coeff: number; color: string } => {
    if (score >= 90) return { rating: 'A', coeff: 1.5, color: 'text-emerald-600 bg-emerald-50 border-emerald-200' };
    if (score >= 75) return { rating: 'B', coeff: 1.2, color: 'text-blue-600 bg-blue-50 border-blue-200' };
    if (score >= 50) return { rating: 'C', coeff: 1.0, color: 'text-amber-600 bg-amber-50 border-amber-200' };
    return { rating: 'D', coeff: 0.5, color: 'text-rose-600 bg-rose-50 border-rose-200' };
  };

  const currentEst = getEstimatedRating(calculatedTotalScore);

  const handleSubmitEvaluation = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedEmployeeId) {
      alert('Vui lòng chọn nhân viên cần đánh giá');
      return;
    }

    try {
      setSubmitting(true);
      const payload: KpiEvaluationCreateRequest = {
        employeeId: Number(selectedEmployeeId),
        periodMonth: month,
        periodYear: year,
        feedback,
        details: activeCriteria.map((c) => ({
          kpiCriteriaId: c.id,
          score: criterionScores[c.id] || 0,
          comments: criterionComments[c.id] || '',
        })),
      };

      await kpiApi.createOrUpdateEvaluation(payload);
      setIsModalOpen(false);
      loadData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Có lỗi xảy ra khi lưu đánh giá KPI');
    } finally {
      setSubmitting(false);
    }
  };

  const handleApprove = async () => {
    if (!confirmDialog.evalId) return;
    try {
      if (confirmDialog.type === 'approve') {
        await kpiApi.approveEvaluation(confirmDialog.evalId);
      } else {
        await kpiApi.rejectEvaluation(confirmDialog.evalId);
      }
      setConfirmDialog({ isOpen: false, type: 'approve', evalId: null, employeeName: '' });
      loadData();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Lỗi khi cập nhật trạng thái');
    }
  };

  const formatVND = (amount?: number) => {
    if (!amount) return '0 ₫';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  };

  // Stats calculation
  const totalBonus = evaluations.reduce((sum, e) => sum + (e.bonusAmount || 0), 0);
  const approvedCount = evaluations.filter((e) => e.status === 'APPROVED').length;
  const ratingACount = evaluations.filter((e) => e.rating === 'A').length;

  return (
    <div className="space-y-6">
      <PageHeader
        title="Đánh Giá Hiệu Suất & KPI"
        subtitle={`Quản lý điểm đánh giá KPI Tháng ${month}/${year}, tự động xếp loại và tính thưởng vào bảng lương.`}
        action={
          isManagerOrAdmin ? (
            <button
              onClick={() => handleOpenCreateModal()}
              className="inline-flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white font-medium rounded-lg shadow-sm transition-colors"
            >
              <Plus className="w-4 h-4" />
              Chấm Điểm KPI
            </button>
          ) : undefined
        }
      />

      {/* Stats Overview */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-indigo-50 flex items-center justify-center text-indigo-600">
            <Award className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">Tổng Phiếu Đánh Giá</p>
            <h3 className="text-2xl font-bold text-slate-800">{evaluations.length}</h3>
          </div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-emerald-50 flex items-center justify-center text-emerald-600">
            <UserCheck className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">Đã Duyệt</p>
            <h3 className="text-2xl font-bold text-emerald-600">{approvedCount} / {evaluations.length}</h3>
          </div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-amber-50 flex items-center justify-center text-amber-600">
            <TrendingUp className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">Loại A (Xuất sắc)</p>
            <h3 className="text-2xl font-bold text-amber-600">{ratingACount} nhân sự</h3>
          </div>
        </div>

        <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm flex items-center gap-4">
          <div className="w-12 h-12 rounded-xl bg-purple-50 flex items-center justify-center text-purple-600">
            <DollarSign className="w-6 h-6" />
          </div>
          <div>
            <p className="text-xs font-semibold uppercase tracking-wider text-slate-500">Tổng Tiền Thưởng KPI</p>
            <h3 className="text-xl font-bold text-purple-700">{formatVND(totalBonus)}</h3>
          </div>
        </div>
      </div>

      {/* Filter Bar */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-wrap items-center gap-3">
        <div className="flex items-center gap-2">
          <label className="text-xs font-semibold text-slate-600">Tháng:</label>
          <select
            value={month}
            onChange={(e) => setMonth(Number(e.target.value))}
            className="px-3 py-1.5 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 bg-white"
          >
            {Array.from({ length: 12 }, (_, i) => i + 1).map((m) => (
              <option key={m} value={m}>Tháng {m}</option>
            ))}
          </select>
        </div>

        <div className="flex items-center gap-2">
          <label className="text-xs font-semibold text-slate-600">Năm:</label>
          <select
            value={year}
            onChange={(e) => setYear(Number(e.target.value))}
            className="px-3 py-1.5 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 bg-white"
          >
            {[2024, 2025, 2026, 2027].map((y) => (
              <option key={y} value={y}>Năm {y}</option>
            ))}
          </select>
        </div>

        <div className="flex items-center gap-2">
          <label className="text-xs font-semibold text-slate-600">Phòng ban:</label>
          <select
            value={departmentId || ''}
            onChange={(e) => setDepartmentId(e.target.value ? Number(e.target.value) : undefined)}
            className="px-3 py-1.5 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 bg-white"
          >
            <option value="">Tất cả phòng ban</option>
            {departments.map((d) => (
              <option key={d.id} value={d.id}>{d.name}</option>
            ))}
          </select>
        </div>

        <div className="flex items-center gap-2">
          <label className="text-xs font-semibold text-slate-600">Trạng thái:</label>
          <select
            value={status || ''}
            onChange={(e) => setStatus((e.target.value as KpiEvaluationStatus) || undefined)}
            className="px-3 py-1.5 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 bg-white"
          >
            <option value="">Tất cả trạng thái</option>
            <option value="DRAFT">Bản nháp</option>
            <option value="SUBMITTED">Chờ duyệt</option>
            <option value="APPROVED">Đã duyệt (Chốt thưởng)</option>
            <option value="REJECTED">Từ chối</option>
          </select>
        </div>

        <div className="relative flex-1 min-w-[200px]">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
          <input
            type="text"
            placeholder="Tìm theo tên, mã NV..."
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && loadData()}
            className="w-full pl-9 pr-3 py-1.5 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
          />
        </div>

        <button
          onClick={loadData}
          className="px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 text-sm font-medium rounded-lg transition-colors inline-flex items-center gap-1.5"
        >
          <Filter className="w-4 h-4" />
          Lọc
        </button>
      </div>

      {/* Evaluations Table */}
      <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-200 text-xs font-bold text-slate-600 uppercase tracking-wider">
                <th className="py-3.5 px-4">Nhân Viên</th>
                <th className="py-3.5 px-4">Phòng Ban</th>
                <th className="py-3.5 px-4 text-center">Tổng Điểm</th>
                <th className="py-3.5 px-4 text-center">Xếp Loại</th>
                <th className="py-3.5 px-4 text-center">Hệ Số KPI</th>
                <th className="py-3.5 px-4 text-right">Tiền Thưởng KPI</th>
                <th className="py-3.5 px-4 text-center">Trạng Thái</th>
                <th className="py-3.5 px-4 text-right">Hành Động</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-sm">
              {loading ? (
                <tr>
                  <td colSpan={8} className="py-12 text-center text-slate-500">
                    Đang tải dữ liệu đánh giá KPI...
                  </td>
                </tr>
              ) : evaluations.length === 0 ? (
                <tr>
                  <td colSpan={8} className="py-12 text-center text-slate-500">
                    Chưa có phiếu đánh giá KPI nào trong Tháng {month}/{year}.
                  </td>
                </tr>
              ) : (
                evaluations.map((item) => {
                  const ratingBadge = {
                    A: 'bg-emerald-100 text-emerald-800 border-emerald-300',
                    B: 'bg-blue-100 text-blue-800 border-blue-300',
                    C: 'bg-amber-100 text-amber-800 border-amber-300',
                    D: 'bg-rose-100 text-rose-800 border-rose-300',
                  }[item.rating];

                  const statusBadge = {
                    DRAFT: 'bg-slate-100 text-slate-700',
                    SUBMITTED: 'bg-amber-100 text-amber-800',
                    APPROVED: 'bg-emerald-100 text-emerald-800',
                    REJECTED: 'bg-rose-100 text-rose-800',
                  }[item.status];

                  const statusText = {
                    DRAFT: 'Bản nháp',
                    SUBMITTED: 'Chờ duyệt',
                    APPROVED: 'Đã duyệt',
                    REJECTED: 'Từ chối',
                  }[item.status];

                  return (
                    <tr key={item.id} className="hover:bg-slate-50/80 transition-colors">
                      <td className="py-3 px-4">
                        <div className="font-semibold text-slate-800">{item.employeeName}</div>
                        <div className="text-xs text-slate-500 font-mono">{item.employeeCode}</div>
                      </td>
                      <td className="py-3 px-4 text-slate-600">{item.departmentName || '-'}</td>
                      <td className="py-3 px-4 text-center">
                        <span className="font-bold text-slate-800 text-base">{item.totalScore.toFixed(1)}</span>
                        <span className="text-xs text-slate-400">/100</span>
                      </td>
                      <td className="py-3 px-4 text-center">
                        <span className={`inline-flex items-center justify-center w-7 h-7 rounded-full text-xs font-bold border ${ratingBadge}`}>
                          {item.rating}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-center font-semibold text-slate-700">
                        {item.kpiCoefficient.toFixed(2)}x
                      </td>
                      <td className="py-3 px-4 text-right font-bold text-emerald-600">
                        {formatVND(item.bonusAmount)}
                      </td>
                      <td className="py-3 px-4 text-center">
                        <span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${statusBadge}`}>
                          {statusText}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-right space-x-1.5 whitespace-nowrap">
                        <button
                          onClick={() => setViewEvaluation(item)}
                          className="p-1.5 text-slate-600 hover:text-indigo-600 hover:bg-slate-100 rounded-lg transition-colors"
                          title="Xem chi tiết"
                        >
                          <Eye className="w-4 h-4" />
                        </button>

                        {isManagerOrAdmin && item.status !== 'APPROVED' && (
                          <>
                            <button
                              onClick={() => {
                                setSelectedEmployeeId(item.employeeId);
                                const scores: { [key: number]: number } = {};
                                const comments: { [key: number]: string } = {};
                                item.details.forEach((d) => {
                                  scores[d.kpiCriteriaId] = d.score;
                                  comments[d.kpiCriteriaId] = d.comments || '';
                                });
                                setCriterionScores(scores);
                                setCriterionComments(comments);
                                setFeedback(item.feedback || '');
                                setIsModalOpen(true);
                              }}
                              className="p-1.5 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                              title="Sửa điểm"
                            >
                              <Edit className="w-4 h-4" />
                            </button>

                            <button
                              onClick={() => setConfirmDialog({
                                isOpen: true,
                                type: 'approve',
                                evalId: item.id,
                                employeeName: item.employeeName,
                              })}
                              className="p-1.5 text-emerald-600 hover:bg-emerald-50 rounded-lg transition-colors"
                              title="Duyệt chốt thưởng"
                            >
                              <CheckCircle className="w-4 h-4" />
                            </button>

                            <button
                              onClick={() => setConfirmDialog({
                                isOpen: true,
                                type: 'reject',
                                evalId: item.id,
                                employeeName: item.employeeName,
                              })}
                              className="p-1.5 text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"
                              title="Từ chối"
                            >
                              <XCircle className="w-4 h-4" />
                            </button>
                          </>
                        )}
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Scoring / Evaluation Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs animate-fadeIn">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-2xl overflow-hidden border border-slate-100 max-h-[90vh] flex flex-col">
            <div className="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-indigo-50/50">
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 rounded-xl bg-indigo-600 flex items-center justify-center text-white">
                  <Award className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-base font-bold text-slate-800">Chấm Điểm Đánh Giá KPI</h3>
                  <p className="text-xs text-slate-500">Kỳ đánh giá: Tháng {month}/{year}</p>
                </div>
              </div>
              <button
                onClick={() => setIsModalOpen(false)}
                className="text-slate-400 hover:text-slate-600 text-lg font-bold"
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleSubmitEvaluation} className="p-6 overflow-y-auto flex-1 space-y-5">
              {/* Employee Selection */}
              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                  Nhân Viên Được Đánh Giá <span className="text-rose-500">*</span>
                </label>
                <select
                  value={selectedEmployeeId}
                  onChange={(e) => setSelectedEmployeeId(Number(e.target.value))}
                  required
                  className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 bg-white"
                >
                  <option value="">-- Chọn nhân viên --</option>
                  {employees.map((emp) => (
                    <option key={emp.id} value={emp.id}>
                      {emp.fullName} ({emp.employeeCode})
                    </option>
                  ))}
                </select>
              </div>

              {/* Dynamic Criteria List */}
              <div className="space-y-4">
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider">
                  Tiêu Chí Đánh Giá (Thang điểm 0 - 100)
                </label>

                {activeCriteria.map((c) => (
                  <div key={c.id} className="p-3.5 bg-slate-50 rounded-xl border border-slate-200/80 space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-sm font-semibold text-slate-800">{c.name}</span>
                      <span className="px-2 py-0.5 bg-indigo-100 text-indigo-700 text-xs font-bold rounded-md">
                        Trọng số: {c.weight}%
                      </span>
                    </div>
                    {c.targetDescription && (
                      <p className="text-xs text-slate-500">{c.targetDescription}</p>
                    )}

                    <div className="flex items-center gap-4 pt-1">
                      <input
                        type="range"
                        min="0"
                        max="100"
                        value={criterionScores[c.id] || 0}
                        onChange={(e) => handleScoreChange(c.id, Number(e.target.value))}
                        className="flex-1 accent-indigo-600 cursor-pointer"
                      />
                      <input
                        type="number"
                        min="0"
                        max="100"
                        value={criterionScores[c.id] || 0}
                        onChange={(e) => handleScoreChange(c.id, Number(e.target.value))}
                        className="w-16 px-2 py-1 text-sm font-bold text-center border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 bg-white"
                      />
                      <span className="text-xs text-slate-500 font-medium">điểm</span>
                    </div>

                    <input
                      type="text"
                      placeholder="Ghi chú / nhận xét tiêu chí này (nếu có)..."
                      value={criterionComments[c.id] || ''}
                      onChange={(e) => setCriterionComments({ ...criterionComments, [c.id]: e.target.value })}
                      className="w-full px-3 py-1 text-xs border border-slate-200 rounded-lg bg-white focus:ring-2 focus:ring-indigo-500"
                    />
                  </div>
                ))}
              </div>

              {/* Live Rating & Coefficient Summary */}
              <div className={`p-4 rounded-xl border ${currentEst.color} flex items-center justify-between`}>
                <div>
                  <p className="text-xs font-bold uppercase tracking-wider">Dự kiến xếp loại</p>
                  <div className="flex items-baseline gap-2 mt-0.5">
                    <span className="text-2xl font-black">Loại {currentEst.rating}</span>
                    <span className="text-sm font-semibold">({calculatedTotalScore.toFixed(1)}/100 điểm)</span>
                  </div>
                </div>
                <div className="text-right">
                  <p className="text-xs font-bold uppercase tracking-wider">Hệ số Thưởng KPI</p>
                  <span className="text-xl font-bold">{currentEst.coeff.toFixed(2)}x</span>
                </div>
              </div>

              {/* Feedback */}
              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1.5">
                  Nhận Xét Chung Của Quản Lý
                </label>
                <textarea
                  rows={2}
                  placeholder="Góp ý, định hướng công việc cho nhân sự..."
                  value={feedback}
                  onChange={(e) => setFeedback(e.target.value)}
                  className="w-full px-3 py-2 text-sm border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500"
                />
              </div>

              <div className="pt-2 flex items-center justify-end gap-3 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
                >
                  Hủy Bỏ
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-semibold rounded-lg shadow-sm transition-colors disabled:opacity-50"
                >
                  {submitting ? 'Đang lưu...' : 'Lưu Đánh Giá'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* View Detail Modal */}
      {viewEvaluation && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-xl overflow-hidden border border-slate-100 p-6 space-y-4">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100">
              <div>
                <h3 className="text-lg font-bold text-slate-800">{viewEvaluation.employeeName}</h3>
                <p className="text-xs text-slate-500">Mã NV: {viewEvaluation.employeeCode} | Kỳ {viewEvaluation.periodMonth}/{viewEvaluation.periodYear}</p>
              </div>
              <span className="text-2xl font-black text-indigo-600">Loại {viewEvaluation.rating}</span>
            </div>

            <div className="space-y-3">
              <h4 className="text-xs font-bold text-slate-700 uppercase tracking-wider">Chi Tiết Điểm Từng Tiêu Chí</h4>
              {viewEvaluation.details.map((d) => (
                <div key={d.id} className="p-3 bg-slate-50 rounded-lg flex items-center justify-between">
                  <div>
                    <p className="text-sm font-semibold text-slate-800">{d.kpiCriteriaName}</p>
                    {d.comments && <p className="text-xs text-slate-500 italic">{d.comments}</p>}
                  </div>
                  <div className="text-right">
                    <span className="text-sm font-bold text-indigo-600">{d.score} đ</span>
                    <span className="text-xs text-slate-400 block">({d.weight}%)</span>
                  </div>
                </div>
              ))}
            </div>

            <div className="p-3.5 bg-indigo-50/70 rounded-xl space-y-1.5 text-xs text-indigo-900">
              <div className="flex justify-between font-semibold">
                <span>Tổng Điểm:</span>
                <span>{viewEvaluation.totalScore.toFixed(1)} / 100</span>
              </div>
              <div className="flex justify-between font-semibold">
                <span>Hệ Số KPI:</span>
                <span>{viewEvaluation.kpiCoefficient.toFixed(2)}x</span>
              </div>
              <div className="flex justify-between font-bold text-emerald-700 text-sm pt-1 border-t border-indigo-200/50">
                <span>Tiền Thưởng KPI:</span>
                <span>{formatVND(viewEvaluation.bonusAmount)}</span>
              </div>
            </div>

            {viewEvaluation.feedback && (
              <div className="text-xs text-slate-600 bg-slate-50 p-3 rounded-lg">
                <span className="font-bold block text-slate-700 mb-1">Nhận xét:</span>
                {viewEvaluation.feedback}
              </div>
            )}

            <div className="pt-2 flex justify-end">
              <button
                onClick={() => setViewEvaluation(null)}
                className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 text-sm font-medium rounded-lg"
              >
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Confirmation Dialog */}
      <ConfirmDialog
        isOpen={confirmDialog.isOpen}
        title={confirmDialog.type === 'approve' ? 'Phê Duyệt Đánh Giá KPI' : 'Từ Chối Đánh Giá KPI'}
        description={
          confirmDialog.type === 'approve'
            ? `Bạn có chắc chắn muốn phê duyệt đánh giá KPI cho nhân viên ${confirmDialog.employeeName}? Tiền thưởng KPI sẽ được tự động chốt và tính vào bảng lương.`
            : `Bạn có chắc chắn muốn từ chối đánh giá KPI cho nhân viên ${confirmDialog.employeeName}?`
        }
        confirmText={confirmDialog.type === 'approve' ? 'Duyệt Chốt Thưởng' : 'Từ Chối'}
        isDestructive={confirmDialog.type === 'reject'}
        onConfirm={handleApprove}
        onClose={() => setConfirmDialog({ isOpen: false, type: 'approve', evalId: null, employeeName: '' })}
      />
    </div>
  );
};
