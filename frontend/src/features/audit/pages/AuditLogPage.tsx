import React, { useState, useEffect, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { auditApi, AuditSearchParams } from '@/api/audit.api';
import { PageHeader } from '@/components/shared/PageHeader';
import { AdvancedFilterBar } from '@/components/shared/AdvancedFilterBar';
import { DataTable, ColumnDef } from '@/components/shared/DataTable';
import { formatDateTime } from '@/lib/utils';
import { AuditLog } from '@/types';

export const AuditLogPage: React.FC = () => {
  const { t } = useTranslation();

  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [isLoading, setIsLoading] = useState(true);

  // Filters
  const [username, setUsername] = useState('');
  const [action, setAction] = useState('');
  const [entityName, setEntityName] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  const loadAuditLogs = useCallback(async () => {
    setIsLoading(true);
    try {
      const params: AuditSearchParams = {
        page: currentPage,
        size: pageSize,
        username: username || undefined,
        action: action || undefined,
        entityName: entityName || undefined,
        startDate: startDate ? `${startDate}T00:00:00` : undefined,
        endDate: endDate ? `${endDate}T23:59:59` : undefined,
        sortBy: 'createdAt',
        sortDir: 'desc',
      };
      const res = await auditApi.search(params);
      setLogs(res.content);
      setTotalElements(res.totalElements);
      setTotalPages(res.totalPages);
    } catch (err) {
      console.error('Failed to load audit logs:', err);
    } finally {
      setIsLoading(false);
    }
  }, [currentPage, pageSize, username, action, entityName, startDate, endDate]);

  useEffect(() => {
    loadAuditLogs();
  }, [loadAuditLogs]);

  const columns: ColumnDef<AuditLog>[] = [
    {
      key: 'createdAt',
      header: t('audit.timestamp'),
      render: (row) => <span className="font-mono text-xs text-gray-600">{formatDateTime(row.createdAt)}</span>,
    },
    {
      key: 'username',
      header: t('audit.user'),
      render: (row) => (
        <span className="font-semibold text-gray-900">{row.username || <span className="text-gray-400">Hệ thống</span>}</span>
      ),
    },
    {
      key: 'action',
      header: t('audit.action'),
      render: (row) => (
        <span className="inline-flex items-center px-2 py-0.5 rounded text-[11px] font-mono font-medium bg-blue-50 text-blue-700 border border-blue-200/60">
          {row.action}
        </span>
      ),
    },
    {
      key: 'entity',
      header: t('audit.entity'),
      render: (row) => (
        <span className="text-xs text-gray-700">
          {row.entityName} {row.entityId ? `(#${row.entityId})` : ''}
        </span>
      ),
    },
    {
      key: 'ipAddress',
      header: t('audit.ip'),
      render: (row) => <span className="font-mono text-[11px] text-gray-400">{row.ipAddress || '-'}</span>,
    },
    {
      key: 'details',
      header: t('audit.details'),
      render: (row) => (
        <span className="text-[11px] text-gray-500 line-clamp-1 max-w-sm" title={row.details || ''}>
          {row.details || '-'}
        </span>
      ),
    },
  ];

  return (
    <div className="space-y-4 max-w-6xl">
      <PageHeader title={t('audit.title')} subtitle={t('audit.subtitle')} />

      <AdvancedFilterBar
        searchTerm={username}
        onSearchChange={(val) => {
          setUsername(val);
          setCurrentPage(0);
        }}
        placeholder="Lọc theo tên tài khoản username..."
        activeFilterCount={[username, action, entityName, startDate, endDate].filter((v) => v !== '').length}
        onResetFilters={() => {
          setUsername('');
          setAction('');
          setEntityName('');
          setStartDate('');
          setEndDate('');
          setCurrentPage(0);
        }}
      >
        {/* Action Select */}
        <select
          value={action}
          onChange={(e) => {
            setAction(e.target.value);
            setCurrentPage(0);
          }}
          className="text-xs bg-gray-50 border border-gray-200 rounded-lg px-2.5 py-2 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
        >
          <option value="">⚡ Tất cả hành động</option>
          <option value="LOGIN">LOGIN</option>
          <option value="CREATE">CREATE</option>
          <option value="UPDATE">UPDATE</option>
          <option value="DELETE">DELETE</option>
          <option value="APPROVE">APPROVE</option>
          <option value="CALCULATE">CALCULATE</option>
        </select>

        {/* Entity Select */}
        <select
          value={entityName}
          onChange={(e) => {
            setEntityName(e.target.value);
            setCurrentPage(0);
          }}
          className="text-xs bg-gray-50 border border-gray-200 rounded-lg px-2.5 py-2 text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500/20"
        >
          <option value="">📁 Tất cả đối tượng</option>
          <option value="AUTH">AUTH</option>
          <option value="EMPLOYEE">EMPLOYEE</option>
          <option value="DEPARTMENT">DEPARTMENT</option>
          <option value="POSITION">POSITION</option>
          <option value="ATTENDANCE">ATTENDANCE</option>
          <option value="LEAVE">LEAVE</option>
          <option value="PAYROLL">PAYROLL</option>
        </select>

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
        data={logs}
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
