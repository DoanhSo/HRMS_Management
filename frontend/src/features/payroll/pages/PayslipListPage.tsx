import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { ArrowLeft, Download, FileText } from 'lucide-react';
import { toast } from 'sonner';
import { payrollApi } from '@/api/payroll.api';
import { reportApi } from '@/api/report.api';
import { departmentApi } from '@/api/department.api';
import { PageHeader } from '@/components/shared/PageHeader';
import { AdvancedFilterBar } from '@/components/shared/AdvancedFilterBar';
import { DataTable, ColumnDef } from '@/components/shared/DataTable';
import { StatusBadge } from '@/components/shared/StatusBadge';
import { formatCurrency } from '@/lib/utils';
import { DepartmentResponse, PayrollPeriodResponse, PayslipResponse } from '@/types';

export const PayslipListPage: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const initialPeriodId = searchParams.get('periodId');

  const [payslips, setPayslips] = useState<PayslipResponse[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(15);
  const [isLoading, setIsLoading] = useState(true);

  // Filters
  const [keyword, setKeyword] = useState('');
  const [periodId, setPeriodId] = useState<number | undefined>(
    initialPeriodId ? Number(initialPeriodId) : undefined
  );
  const [departmentId, setDepartmentId] = useState<number | undefined>();
  const [periods, setPeriods] = useState<PayrollPeriodResponse[]>([]);
  const [departments, setDepartments] = useState<DepartmentResponse[]>([]);

  // Downloading state
  const [downloadingId, setDownloadingId] = useState<number | null>(null);

  useEffect(() => {
    payrollApi.getPeriods({ page: 0, size: 50 }).then((res) => setPeriods(res.content)).catch(console.warn);
    departmentApi.getAllActive().then(setDepartments).catch(console.warn);
  }, []);

  const loadPayslips = useCallback(async () => {
    setIsLoading(true);
    try {
      const res = await payrollApi.searchPayslips({
        page: currentPage,
        size: pageSize,
        periodId: periodId,
        departmentId: departmentId,
        keyword: keyword || undefined,
      });
      setPayslips(res.content);
      setTotalElements(res.totalElements);
      setTotalPages(res.totalPages);
    } catch (err) {
      console.error('Failed to load payslips:', err);
    } finally {
      setIsLoading(false);
    }
  }, [currentPage, pageSize, periodId, departmentId, keyword]);

  useEffect(() => {
    loadPayslips();
  }, [loadPayslips]);

  const handleDownloadPdf = async (id: number, employeeCode: string) => {
    setDownloadingId(id);
    try {
      const blob = await reportApi.downloadPayslipPdf(id);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `phieu_luong_${employeeCode}.pdf`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      window.URL.revokeObjectURL(url);
      toast.success('Đã tải phiếu lương PDF');
    } catch (err: any) {
      toast.error('Không thể tải file PDF');
    } finally {
      setDownloadingId(null);
    }
  };

  const columns: ColumnDef<PayslipResponse>[] = [
    {
      key: 'employeeCode',
      header: 'Mã NV',
      className: 'font-semibold',
      render: (row) => (
        <span className="font-mono text-xs text-blue-600 bg-blue-50 px-2 py-0.5 rounded">
          {row.employeeCode}
        </span>
      ),
    },
    {
      key: 'employeeName',
      header: 'Họ và tên',
      render: (row) => <span className="font-medium text-gray-900">{row.employeeName}</span>,
    },
    {
      key: 'actualWorkDays',
      header: 'Ngày công',
      render: (row) => `${row.actualWorkDays} công`,
    },
    {
      key: 'basicSalary',
      header: 'Lương cơ bản',
      render: (row) => formatCurrency(row.basicSalary),
    },
    {
      key: 'allowances',
      header: 'Phụ cấp',
      render: (row) => formatCurrency(row.allowances),
    },
    {
      key: 'deductions',
      header: 'Khấu trừ / BH',
      render: (row) => (
        <span className="text-rose-600 font-medium">-{formatCurrency(row.deductions)}</span>
      ),
    },
    {
      key: 'tax',
      header: 'Thuế TNCN',
      render: (row) => (
        <span className="text-amber-600 font-medium">-{formatCurrency(row.tax)}</span>
      ),
    },
    {
      key: 'netSalary',
      header: 'Thực nhận (Net)',
      render: (row) => (
        <span className="font-bold text-emerald-600">{formatCurrency(row.netSalary)}</span>
      ),
    },
    {
      key: 'status',
      header: 'Trạng thái',
      render: (row) => <StatusBadge status={row.status} />,
    },
    {
      key: 'actions',
      header: '',
      headerClassName: 'text-right',
      className: 'text-right',
      render: (row) => (
        <div className="flex items-center justify-end" onClick={(e) => e.stopPropagation()}>
          <button
            type="button"
            title="Tải PDF"
            disabled={downloadingId === row.id}
            onClick={() => handleDownloadPdf(row.id, row.employeeCode)}
            className="p-1.5 text-gray-500 hover:text-blue-600 hover:bg-blue-50 rounded-md transition-colors disabled:opacity-40"
          >
            <Download className="w-3.5 h-3.5" />
          </button>
        </div>
      ),
    },
  ];

  const [isExporting, setIsExporting] = useState(false);

  const handleExportPeriodExcel = async () => {
    if (!periodId) {
      toast.warning('Vui lòng chọn kỳ lương cần xuất');
      return;
    }
    setIsExporting(true);
    try {
      const blob = await payrollApi.exportPayrollPeriodExcel(periodId);
      const selectedPeriod = periods.find((p) => p.id === periodId);
      const filename = `bang_luong_${selectedPeriod?.year || ''}_thang_${selectedPeriod?.month || ''}.xlsx`;
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
      toast.success('Đã xuất bảng lương ra file Excel thành công');
    } catch (err) {
      console.error('Export payroll error:', err);
      toast.error('Không thể xuất bảng lương');
    } finally {
      setIsExporting(false);
    }
  };

  return (
    <div className="space-y-4 max-w-6xl">
      <PageHeader
        title={t('payroll.allPayslips')}
        subtitle="Danh sách chi tiết phiếu lương của từng nhân sự trong kỳ"
        action={
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => navigate('/payroll')}
              className="px-3 py-2 text-xs font-medium text-gray-600 hover:text-gray-900 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors flex items-center gap-1.5 shadow-2xs"
            >
              <ArrowLeft className="w-3.5 h-3.5" />
              <span>Danh sách kỳ lương</span>
            </button>

            {periodId && (
              <button
                type="button"
                onClick={handleExportPeriodExcel}
                disabled={isExporting}
                className="inline-flex items-center gap-1.5 px-3.5 py-2 text-xs font-medium text-emerald-700 bg-emerald-50 border border-emerald-200 rounded-lg hover:bg-emerald-100 shadow-xs transition-colors disabled:opacity-50"
              >
                <Download className="w-3.5 h-3.5 text-emerald-600" />
                <span>{isExporting ? 'Đang xuất...' : 'Xuất Bảng Lương Excel'}</span>
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
        placeholder="Tìm kiếm theo mã, họ tên nhân viên..."
        activeFilterCount={[keyword, periodId, departmentId].filter((v) => v !== undefined && v !== '').length}
        onResetFilters={() => {
          setKeyword('');
          setPeriodId(undefined);
          setDepartmentId(undefined);
          setCurrentPage(0);
        }}
      >
        {/* Payroll Period Filter */}
        <select
          value={periodId ?? ''}
          onChange={(e) => {
            setPeriodId(e.target.value ? Number(e.target.value) : undefined);
            setCurrentPage(0);
          }}
          className="text-xs bg-gray-50 border border-gray-200 rounded-lg px-2.5 py-2 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
        >
          <option value="">📅 Tất cả các kỳ lương</option>
          {periods.map((p) => (
            <option key={p.id} value={p.id}>
              {p.name}
            </option>
          ))}
        </select>

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
      </AdvancedFilterBar>

      <DataTable
        columns={columns}
        data={payslips}
        totalElements={totalElements}
        totalPages={totalPages}
        currentPage={currentPage}
        pageSize={pageSize}
        onPageChange={setCurrentPage}
        isLoading={isLoading}
      />
    </div>
  );
};
