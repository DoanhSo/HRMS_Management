import React, { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Users, UserCheck, Building2, Wallet, RefreshCw } from 'lucide-react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  Legend,
  PieChart,
  Pie,
  Cell,
  LineChart,
  Line,
  CartesianGrid,
} from 'recharts';
import { dashboardApi } from '@/api/dashboard.api';
import { StatCard } from '@/components/shared/StatCard';
import { PageHeader } from '@/components/shared/PageHeader';
import { formatCurrency } from '@/lib/utils';
import {
  AttendanceOverviewResponse,
  DashboardSummaryResponse,
  DepartmentStatsResponse,
  PayrollSummaryResponse,
} from '@/types';

const COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6', '#ec4899', '#06b6d4'];

export const DashboardPage: React.FC = () => {
  const { t } = useTranslation();
  const [isLoading, setIsLoading] = useState(true);
  const [summary, setSummary] = useState<DashboardSummaryResponse | null>(null);
  const [attendance, setAttendance] = useState<AttendanceOverviewResponse | null>(null);
  const [deptStats, setDeptStats] = useState<DepartmentStatsResponse[]>([]);
  const [payrollHistory, setPayrollHistory] = useState<PayrollSummaryResponse[]>([]);

  const loadData = async () => {
    setIsLoading(true);
    try {
      const [sumRes, attRes, deptRes, payRes] = await Promise.all([
        dashboardApi.getSummary(),
        dashboardApi.getAttendanceOverview(),
        dashboardApi.getDepartmentStats(),
        dashboardApi.getPayrollSummary(),
      ]);

      setSummary(sumRes);
      setAttendance(attRes);
      setDeptStats(deptRes);
      setPayrollHistory(payRes);
    } catch (err) {
      console.error('Failed to load dashboard data:', err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const attendanceData = attendance
    ? [
        { name: t('dashboard.present'), count: attendance.presentCount, fill: '#10b981' },
        { name: t('dashboard.late'), count: attendance.lateCount, fill: '#f59e0b' },
        { name: t('dashboard.earlyLeave'), count: attendance.earlyLeaveCount, fill: '#8b5cf6' },
        { name: t('status.ON_LEAVE'), count: attendance.onLeaveCount, fill: '#6366f1' },
        { name: t('dashboard.absent'), count: attendance.absentCount, fill: '#ef4444' },
      ]
    : [];

  const pieData = deptStats.map((d) => ({
    name: d.departmentName,
    value: d.employeeCount,
  }));

  const payrollLineData = [...payrollHistory].reverse().map((p) => ({
    name: `T${p.month}/${p.year}`,
    gross: p.totalGrossSalary,
    net: p.totalNetSalary,
  }));

  return (
    <div className="space-y-6">
      <PageHeader
        title={t('nav.dashboard')}
        subtitle={t('app.tagline')}
        action={
          <button
            type="button"
            onClick={loadData}
            disabled={isLoading}
            className="p-2 text-gray-500 hover:text-gray-900 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors shadow-2xs"
          >
            <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
          </button>
        }
      />

      {/* 1. Stat Cards Row */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title={t('dashboard.totalEmployees')}
          value={summary?.totalEmployees ?? '-'}
          icon={Users}
          subtitle={`${summary?.probationEmployees ?? 0} ${t('dashboard.probationEmployees').toLowerCase()}`}
          iconBg="bg-blue-50"
          iconColor="text-blue-600"
        />
        <StatCard
          title={t('dashboard.activeEmployees')}
          value={summary?.activeEmployees ?? '-'}
          icon={UserCheck}
          iconBg="bg-emerald-50"
          iconColor="text-emerald-600"
        />
        <StatCard
          title={t('dashboard.departments')}
          value={summary?.totalDepartments ?? '-'}
          icon={Building2}
          subtitle={`${summary?.totalPositions ?? 0} ${t('dashboard.positions').toLowerCase()}`}
          iconBg="bg-indigo-50"
          iconColor="text-indigo-600"
        />
        <StatCard
          title={t('dashboard.monthlyPayroll')}
          value={formatCurrency(summary?.latestMonthlyPayrollCost)}
          icon={Wallet}
          iconBg="bg-amber-50"
          iconColor="text-amber-600"
        />
      </div>

      {/* 2. Charts Row */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Attendance Bar Chart Card */}
        <div className="bg-white p-5 rounded-xl border border-gray-200/80 shadow-[0_1px_3px_rgba(0,0,0,0.05)]">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-sm font-bold text-gray-900">{t('dashboard.attendanceOverview')}</h3>
              <p className="text-xs text-gray-500">
                {t('dashboard.attendanceRate')}:{' '}
                <span className="font-semibold text-emerald-600">
                  {attendance ? `${attendance.attendanceRatePercentage.toFixed(1)}%` : '-'}
                </span>
              </p>
            </div>
          </div>
          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={attendanceData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f3f4f6" />
                <XAxis dataKey="name" stroke="#9ca3af" fontSize={11} tickLine={false} />
                <YAxis stroke="#9ca3af" fontSize={11} tickLine={false} allowDecimals={false} />
                <Tooltip
                  formatter={(val: any) => [`${val} nhân sự`, 'Số lượng']}
                  contentStyle={{
                    backgroundColor: '#fff',
                    borderRadius: '8px',
                    border: '1px solid #e5e7eb',
                    fontSize: '12px',
                  }}
                />
                <Bar dataKey="count" radius={[4, 4, 0, 0]}>
                  {attendanceData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.fill} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Department Headcount Pie Chart Card */}
        <div className="bg-white p-5 rounded-xl border border-gray-200/80 shadow-[0_1px_3px_rgba(0,0,0,0.05)]">
          <div className="mb-4">
            <h3 className="text-sm font-bold text-gray-900">{t('dashboard.departmentHeadcount')}</h3>
            <p className="text-xs text-gray-500">Tỷ trọng nhân sự phân bổ theo các phòng ban</p>
          </div>
          <div className="h-64 w-full flex items-center justify-center">
            {pieData.length === 0 ? (
              <p className="text-xs text-gray-400">Chưa có dữ liệu</p>
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={pieData}
                    cx="50%"
                    cy="50%"
                    innerRadius={55}
                    outerRadius={80}
                    paddingAngle={4}
                    dataKey="value"
                  >
                    {pieData.map((_, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip
                    formatter={(val: any) => [`${val} nhân sự`, 'Nhân sự']}
                    contentStyle={{
                      backgroundColor: '#fff',
                      borderRadius: '8px',
                      border: '1px solid #e5e7eb',
                      fontSize: '12px',
                    }}
                  />
                  <Legend
                    layout="horizontal"
                    verticalAlign="bottom"
                    align="center"
                    iconType="circle"
                    iconSize={8}
                    wrapperStyle={{ fontSize: '11px', paddingTop: '10px' }}
                  />
                </PieChart>
              </ResponsiveContainer>
            )}
          </div>
        </div>
      </div>

      {/* 3. Payroll Trend Line Chart */}
      <div className="bg-white p-5 rounded-xl border border-gray-200/80 shadow-[0_1px_3px_rgba(0,0,0,0.05)]">
        <div className="mb-4">
          <h3 className="text-sm font-bold text-gray-900">{t('dashboard.payrollCostTrend')}</h3>
          <p className="text-xs text-gray-500">Chi phí lương thực lĩnh (Net) và tổng chi phí (Gross) qua các kỳ</p>
        </div>
        <div className="h-64 w-full">
          {payrollLineData.length === 0 ? (
            <div className="h-full flex items-center justify-center text-xs text-gray-400">
              Chưa có dữ liệu lịch sử bảng lương
            </div>
          ) : (
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={payrollLineData} margin={{ top: 10, right: 20, left: 20, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f3f4f6" />
                <XAxis dataKey="name" stroke="#9ca3af" fontSize={11} tickLine={false} />
                <YAxis
                  stroke="#9ca3af"
                  fontSize={11}
                  tickLine={false}
                  tickFormatter={(v) => `${(v / 1000000).toFixed(0)}M`}
                />
                <Tooltip
                  formatter={(val: any) => [formatCurrency(val), '']}
                  contentStyle={{
                    backgroundColor: '#fff',
                    borderRadius: '8px',
                    border: '1px solid #e5e7eb',
                    fontSize: '12px',
                  }}
                />
                <Legend
                  wrapperStyle={{ fontSize: '11px', paddingTop: '8px' }}
                  formatter={(value) => (value === 'gross' ? 'Tổng chi phí (Gross)' : 'Thực lĩnh (Net)')}
                />
                <Line type="monotone" dataKey="gross" stroke="#3b82f6" strokeWidth={2} dot={{ r: 4 }} activeDot={{ r: 6 }} />
                <Line type="monotone" dataKey="net" stroke="#10b981" strokeWidth={2} dot={{ r: 4 }} activeDot={{ r: 6 }} />
              </LineChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>
    </div>
  );
};
