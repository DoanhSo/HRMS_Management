import React, { useState, useEffect, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { Download, Wallet } from 'lucide-react';
import { toast } from 'sonner';
import { payrollApi } from '@/api/payroll.api';
import { reportApi } from '@/api/report.api';
import { PageHeader } from '@/components/shared/PageHeader';
import { DataTable, ColumnDef } from '@/components/shared/DataTable';
import { StatusBadge } from '@/components/shared/StatusBadge';
import { formatCurrency } from '@/lib/utils';
import { PayslipResponse } from '@/types';

export const MyPayslipsPage: React.FC = () => {
  const { t } = useTranslation();

  const [payslips, setPayslips] = useState<PayslipResponse[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(15);
  const [isLoading, setIsLoading] = useState(true);
  const [downloadingId, setDownloadingId] = useState<number | null>(null);

  const loadMyPayslips = useCallback(async () => {
    setIsLoading(true);
    try {
      const res = await payrollApi.getMyPayslips({ page: currentPage, size: pageSize });
      setPayslips(res.content);
      setTotalElements(res.totalElements);
      setTotalPages(res.totalPages);
    } catch (err) {
      console.error('Failed to load personal payslips:', err);
    } finally {
      setIsLoading(false);
    }
  }, [currentPage, pageSize]);

  useEffect(() => {
    loadMyPayslips();
  }, [loadMyPayslips]);

  const handleDownloadPdf = async (id: number, periodName: string) => {
    setDownloadingId(id);
    try {
      const blob = await reportApi.downloadPayslipPdf(id);
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `phieu_luong_${periodName.replace(/\s+/g, '_')}.pdf`;
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
      key: 'payrollPeriodName',
      header: t('payroll.periodName'),
      render: (row) => <span className="font-semibold text-gray-900">{row.payrollPeriodName}</span>,
    },
    {
      key: 'actualWorkDays',
      header: t('payroll.actualWorkDays'),
      render: (row) => <span className="text-xs text-gray-700">{row.actualWorkDays} ngày</span>,
    },
    {
      key: 'basicSalary',
      header: t('payroll.basicSalary'),
      render: (row) => formatCurrency(row.basicSalary),
    },
    {
      key: 'grossSalary',
      header: t('payroll.grossSalary'),
      render: (row) => formatCurrency(row.grossSalary),
    },
    {
      key: 'tax',
      header: t('payroll.tax'),
      render: (row) => <span className="text-rose-600 font-medium">{formatCurrency(row.tax)}</span>,
    },
    {
      key: 'netSalary',
      header: t('payroll.netSalary'),
      render: (row) => (
        <span className="font-bold text-emerald-700 text-sm">{formatCurrency(row.netSalary)}</span>
      ),
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
      render: (row) => (
        <div className="flex items-center justify-end" onClick={(e) => e.stopPropagation()}>
          <button
            type="button"
            title="Tải PDF"
            disabled={downloadingId === row.id}
            onClick={() => handleDownloadPdf(row.id, row.payrollPeriodName)}
            className="px-2.5 py-1 text-xs text-blue-600 hover:bg-blue-50 border border-blue-200 rounded-md transition-colors flex items-center gap-1 disabled:opacity-40"
          >
            <Download className="w-3.5 h-3.5" />
            <span>Tải PDF</span>
          </button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-6 max-w-5xl">
      <PageHeader
        title={t('payroll.myPayslips')}
        subtitle="Lịch sử chi trả và chi tiết phiếu lương cá nhân của bạn"
      />

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
