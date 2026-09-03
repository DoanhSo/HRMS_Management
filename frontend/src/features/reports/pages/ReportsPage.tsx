import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { FileSpreadsheet, Download, FileText } from 'lucide-react';
import { toast } from 'sonner';
import { reportApi } from '@/api/report.api';
import { departmentApi } from '@/api/department.api';
import { payrollApi } from '@/api/payroll.api';
import { PageHeader } from '@/components/shared/PageHeader';
import { DepartmentResponse } from '@/types';

export const ReportsPage: React.FC = () => {
  const { t } = useTranslation();

  // Attendance Report state
  const [startDate, setStartDate] = useState(
    new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().split('T')[0]
  );
  const [endDate, setEndDate] = useState(new Date().toISOString().split('T')[0]);
  const [departmentId, setDepartmentId] = useState<number | undefined>();
  const [departments, setDepartments] = useState<DepartmentResponse[]>([]);
  const [isDownloadingExcel, setIsDownloadingExcel] = useState(false);

  // Payslip Report state
  const [payslips, setPayslips] = useState<any[]>([]);
  const [payslipDeptId, setPayslipDeptId] = useState<number | undefined>();
  const [selectedPayslipId, setSelectedPayslipId] = useState<number | undefined>();
  const [isDownloadingPdf, setIsDownloadingPdf] = useState(false);

  useEffect(() => {
    departmentApi.getAllActive().then(setDepartments).catch(console.warn);
    payrollApi.searchPayslips({ page: 0, size: 50 }).then((res) => setPayslips(res.content)).catch(console.warn);
  }, []);

  const handleDownloadAttendanceExcel = async () => {
    setIsDownloadingExcel(true);
    try {
      const blob = await reportApi.downloadAttendanceExcel(startDate, endDate, departmentId);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `bao_cao_cham_cong_${startDate}_${endDate}.xlsx`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
      toast.success('Đã tải xuống file Excel báo cáo chấm công');
    } catch (err) {
      console.error('Download excel error:', err);
      toast.error('Không thể xuất báo cáo Excel');
    } finally {
      setIsDownloadingExcel(false);
    }
  };

  const handleDownloadPayslipPdf = async () => {
    if (!selectedPayslipId) {
      toast.error('Vui lòng chọn phiếu lương cần tải');
      return;
    }
    setIsDownloadingPdf(true);
    try {
      const blob = await reportApi.downloadPayslipPdf(selectedPayslipId);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `phieu_luong_${selectedPayslipId}.pdf`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
      toast.success('Đã tải xuống phiếu lương PDF');
    } catch (err) {
      console.error('Download pdf error:', err);
      toast.error('Không thể xuất phiếu lương PDF');
    } finally {
      setIsDownloadingPdf(false);
    }
  };

  return (
    <div className="space-y-6 max-w-5xl">
      <PageHeader title={t('reports.title')} subtitle={t('reports.subtitle')} />

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Attendance Excel Card */}
        <div className="bg-white p-6 rounded-xl border border-gray-200/80 shadow-[0_1px_3px_rgba(0,0,0,0.05)] flex flex-col justify-between space-y-4">
          <div className="space-y-3">
            <div className="flex items-center gap-3">
              <div className="p-3 bg-emerald-50 text-emerald-600 rounded-lg">
                <FileSpreadsheet className="w-6 h-6" />
              </div>
              <div>
                <h3 className="text-sm font-bold text-gray-900">{t('reports.attendanceReport')}</h3>
                <p className="text-xs text-gray-400">Xuất định dạng Microsoft Excel (.xlsx)</p>
              </div>
            </div>

            <p className="text-xs text-gray-500 leading-relaxed">{t('reports.attendanceReportDesc')}</p>

            <div className="space-y-3 pt-2">
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1">
                  <label className="text-xs font-semibold text-gray-700">Từ ngày</label>
                  <input
                    type="date"
                    value={startDate}
                    onChange={(e) => setStartDate(e.target.value)}
                    className="w-full px-3 py-2 text-xs bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/20"
                  />
                </div>
                <div className="space-y-1">
                  <label className="text-xs font-semibold text-gray-700">Đến ngày</label>
                  <input
                    type="date"
                    value={endDate}
                    onChange={(e) => setEndDate(e.target.value)}
                    className="w-full px-3 py-2 text-xs bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/20"
                  />
                </div>
              </div>

              <div className="space-y-1">
                <label className="text-xs font-semibold text-gray-700">{t('employee.department')}</label>
                <select
                  value={departmentId ?? ''}
                  onChange={(e) => setDepartmentId(e.target.value ? Number(e.target.value) : undefined)}
                  className="w-full px-3 py-2 text-xs bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-emerald-500/20"
                >
                  <option value="">-- Toàn công ty --</option>
                  {departments.map((d) => (
                    <option key={d.id} value={d.id}>
                      {d.name}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          </div>

          <button
            type="button"
            disabled={isDownloadingExcel}
            onClick={handleDownloadAttendanceExcel}
            className="w-full py-2.5 px-4 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold rounded-lg transition-colors flex items-center justify-center gap-2 shadow-xs disabled:opacity-50"
          >
            {isDownloadingExcel ? (
              <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              <Download className="w-4 h-4" />
            )}
            <span>{t('actions.downloadExcel')}</span>
          </button>
        </div>

        {/* Payslip PDF Card */}
        <div className="bg-white p-6 rounded-xl border border-gray-200/80 shadow-[0_1px_3px_rgba(0,0,0,0.05)] flex flex-col justify-between space-y-4">
          <div className="space-y-3">
            <div className="flex items-center gap-3">
              <div className="p-3 bg-rose-50 text-rose-600 rounded-lg">
                <FileText className="w-6 h-6" />
              </div>
              <div>
                <h3 className="text-sm font-bold text-gray-900">{t('reports.payslipReport')}</h3>
                <p className="text-xs text-gray-400">Xuất định dạng PDF tiêu chuẩn</p>
              </div>
            </div>

            <p className="text-xs text-gray-500 leading-relaxed">{t('reports.payslipReportDesc')}</p>

              <div className="space-y-3">
                <div className="space-y-1">
                  <label className="text-xs font-semibold text-gray-700">Lọc theo phòng ban</label>
                  <select
                    value={payslipDeptId ?? ''}
                    onChange={(e) => {
                      const dept = e.target.value ? Number(e.target.value) : undefined;
                      setPayslipDeptId(dept);
                      setSelectedPayslipId(undefined);
                      payrollApi.searchPayslips({ page: 0, size: 50, departmentId: dept })
                        .then((res) => setPayslips(res.content))
                        .catch(console.warn);
                    }}
                    className="w-full px-3 py-2 text-xs bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-rose-500/20"
                  >
                    <option value="">🏢 Tất cả phòng ban</option>
                    {departments.map((d) => (
                      <option key={d.id} value={d.id}>
                        {d.name} ({d.code})
                      </option>
                    ))}
                  </select>
                </div>

                <div className="space-y-1">
                  <label className="text-xs font-semibold text-gray-700">Chọn phiếu lương nhân viên</label>
                  <select
                    value={selectedPayslipId ?? ''}
                    onChange={(e) => setSelectedPayslipId(e.target.value ? Number(e.target.value) : undefined)}
                    className="w-full px-3 py-2 text-xs bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-rose-500/20"
                  >
                    <option value="">-- {t('form.selectPlaceholder')} --</option>
                    {payslips.map((p) => (
                      <option key={p.id} value={p.id}>
                        {p.employeeName} ({p.employeeCode}) - {p.payrollPeriodName}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
          </div>

          <button
            type="button"
            disabled={isDownloadingPdf || !selectedPayslipId}
            onClick={handleDownloadPayslipPdf}
            className="w-full py-2.5 px-4 bg-rose-600 hover:bg-rose-700 text-white text-xs font-semibold rounded-lg transition-colors flex items-center justify-center gap-2 shadow-xs disabled:opacity-40"
          >
            {isDownloadingPdf ? (
              <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              <Download className="w-4 h-4" />
            )}
            <span>{t('actions.downloadPdf')}</span>
          </button>
        </div>
      </div>
    </div>
  );
};
