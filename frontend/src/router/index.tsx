import { createBrowserRouter, Navigate } from 'react-router-dom';
import { LoginPage } from '@/features/auth/pages/LoginPage';
import { AppLayout } from '@/components/layout/AppLayout';
import { ProtectedRoute } from '@/components/layout/ProtectedRoute';
import { RoleGuard } from '@/components/layout/RoleGuard';

// Pages
import { DashboardPage } from '@/features/dashboard/pages/DashboardPage';
import { EmployeeListPage } from '@/features/employees/pages/EmployeeListPage';
import { EmployeeFormPage } from '@/features/employees/pages/EmployeeFormPage';
import { EmployeeDetailPage } from '@/features/employees/pages/EmployeeDetailPage';
import { DepartmentListPage } from '@/features/departments/pages/DepartmentListPage';
import { PositionListPage } from '@/features/positions/pages/PositionListPage';
import { AttendancePage } from '@/features/attendance/pages/AttendancePage';
import { AttendanceAdminPage } from '@/features/attendance/pages/AttendanceAdminPage';
import { LeaveManagementPage } from '@/features/leave/pages/LeaveManagementPage';
import { PayrollPeriodsPage } from '@/features/payroll/pages/PayrollPeriodsPage';
import { PayslipListPage } from '@/features/payroll/pages/PayslipListPage';
import { MyPayslipsPage } from '@/features/payroll/pages/MyPayslipsPage';
import { ReportsPage } from '@/features/reports/pages/ReportsPage';
import { AuditLogPage } from '@/features/audit/pages/AuditLogPage';
import { UserListPage } from '@/features/users/pages/UserListPage';
import { ChangePasswordPage } from '@/features/settings/pages/ChangePasswordPage';
import { KpiEvaluationListPage } from '@/features/kpi/pages/KpiEvaluationListPage';
import { KpiCriteriaListPage } from '@/features/kpi/pages/KpiCriteriaListPage';
import { SalaryScaleListPage } from '@/features/salary-scale/pages/SalaryScaleListPage';

const MANAGER_OR_ABOVE = ['ADMIN', 'HR', 'MANAGER', 'ROLE_ADMIN', 'ROLE_HR', 'ROLE_MANAGER'];
const HR_OR_ADMIN = ['ADMIN', 'HR', 'ROLE_ADMIN', 'ROLE_HR'];

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    element: <ProtectedRoute />,
    children: [
      {
        element: <AppLayout />,
        children: [
          {
            path: '/',
            element: <Navigate to="/dashboard" replace />,
          },
          // 1. Dashboard (Manager+)
          {
            element: <RoleGuard allowedRoles={MANAGER_OR_ABOVE} />,
            children: [
              {
                path: '/dashboard',
                element: <DashboardPage />,
              },
            ],
          },
          // 2. Employees
          {
            element: <RoleGuard allowedRoles={MANAGER_OR_ABOVE} />,
            children: [
              {
                path: '/employees',
                element: <EmployeeListPage />,
              },
              {
                path: '/employees/:id',
                element: <EmployeeDetailPage />,
              },
            ],
          },
          {
            element: <RoleGuard allowedRoles={HR_OR_ADMIN} />,
            children: [
              {
                path: '/employees/new',
                element: <EmployeeFormPage />,
              },
              {
                path: '/employees/:id/edit',
                element: <EmployeeFormPage />,
              },
            ],
          },
          // 3. Departments & Positions (All)
          {
            path: '/departments',
            element: <DepartmentListPage />,
          },
          {
            path: '/positions',
            element: <PositionListPage />,
          },
          // 4. Attendance
          {
            path: '/attendance',
            element: <AttendancePage />,
          },
          {
            element: <RoleGuard allowedRoles={MANAGER_OR_ABOVE} />,
            children: [
              {
                path: '/attendance/admin',
                element: <AttendanceAdminPage />,
              },
            ],
          },
          // 5. Leave
          {
            path: '/leave',
            element: <LeaveManagementPage />,
          },
          // 6. Payroll
          {
            path: '/payroll',
            element: <PayrollPeriodsPage />,
          },
          {
            path: '/payroll/payslips',
            element: <PayslipListPage />,
          },
          {
            path: '/payroll/my-records',
            element: <MyPayslipsPage />,
          },
          // 7. Salary Scales & Coefficients
          {
            element: <RoleGuard allowedRoles={HR_OR_ADMIN} />,
            children: [
              {
                path: '/salary-scales',
                element: <SalaryScaleListPage />,
              },
            ],
          },
          // 8. KPI Performance Appraisal
          {
            path: '/kpi',
            element: <KpiEvaluationListPage />,
          },
          {
            element: <RoleGuard allowedRoles={HR_OR_ADMIN} />,
            children: [
              {
                path: '/kpi/criteria',
                element: <KpiCriteriaListPage />,
              },
            ],
          },
          // 9. Reports (Manager+)
          {
            element: <RoleGuard allowedRoles={MANAGER_OR_ABOVE} />,
            children: [
              {
                path: '/reports',
                element: <ReportsPage />,
              },
            ],
          },
          // 8. Audit Logs (HR/Admin)
          {
            element: <RoleGuard allowedRoles={HR_OR_ADMIN} />,
            children: [
              {
                path: '/audit-logs',
                element: <AuditLogPage />,
              },
            ],
          },
          // 9. User Management (Admin only)
          {
            element: <RoleGuard allowedRoles={['ADMIN', 'ROLE_ADMIN']} />,
            children: [
              {
                path: '/users',
                element: <UserListPage />,
              },
            ],
          },
          // 10. Settings
          {
            path: '/settings/change-password',
            element: <ChangePasswordPage />,
          },
        ],
      },
    ],
  },
  {
    path: '*',
    element: <Navigate to="/dashboard" replace />,
  },
]);
