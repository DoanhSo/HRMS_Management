// ==========================================
// 1. API Global Types
// ==========================================
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  success: boolean;
  data: T;
  timestamp: string;
  path: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface PageableParams {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
}

// ==========================================
// 2. Auth Types & Enums
// ==========================================
export type RoleName = 'ROLE_ADMIN' | 'ROLE_HR' | 'ROLE_MANAGER' | 'ROLE_EMPLOYEE' | 'ADMIN' | 'HR' | 'MANAGER' | 'EMPLOYEE';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  roles: string[];
  permissions?: string[];
  enabled: boolean;
  accountNonLocked: boolean;
  createdAt?: string;
  updatedAt?: string;
}

// ==========================================
// 3. Employee Types & Enums
// ==========================================
export type Gender = 'MALE' | 'FEMALE' | 'OTHER';
export type EmploymentStatus = 'PROBATION' | 'ACTIVE' | 'ON_LEAVE' | 'TERMINATED';

export interface EmployeeResponse {
  id: number;
  employeeCode: string;
  firstName: string;
  lastName: string;
  fullName: string;
  dateOfBirth?: string | null;
  gender?: Gender | null;
  phone?: string | null;
  address?: string | null;
  hireDate: string;
  terminationDate?: string | null;
  employmentStatus: EmploymentStatus;
  departmentId?: number | null;
  positionId?: number | null;
  managerId?: number | null;
  managerName?: string | null;
  userId?: number | null;
  profilePictureUrl?: string | null;
  createdAt: string;
  updatedAt?: string | null;
}

export interface EmployeeCreateRequest {
  firstName: string;
  lastName: string;
  dateOfBirth?: string;
  gender?: Gender;
  phone?: string;
  address?: string;
  hireDate: string;
  employmentStatus?: EmploymentStatus;
  departmentId?: number;
  positionId?: number;
  managerId?: number;
  userId?: number;
  profilePictureUrl?: string;
}

export interface EmployeeUpdateRequest {
  firstName: string;
  lastName: string;
  dateOfBirth?: string;
  gender?: Gender;
  phone?: string;
  address?: string;
  terminationDate?: string;
  employmentStatus?: EmploymentStatus;
  departmentId?: number;
  positionId?: number;
  managerId?: number;
  profilePictureUrl?: string;
}

export interface EmployeeSearchParams extends PageableParams {
  keyword?: string;
  departmentId?: number;
  positionId?: number;
  status?: EmploymentStatus;
  gender?: Gender;
  hireDateFrom?: string;
  hireDateTo?: string;
}

// ==========================================
// 4. Department Types
// ==========================================
export interface DepartmentResponse {
  id: number;
  name: string;
  code: string;
  description?: string | null;
  managerId?: number | null;
  managerName?: string | null;
  parentDepartmentId?: number | null;
  parentDepartmentName?: string | null;
  employeeCount?: number;
  active: boolean;
  createdAt: string;
  updatedAt?: string | null;
}

export interface DepartmentCreateRequest {
  name: string;
  code: string;
  description?: string;
  managerId?: number;
  parentDepartmentId?: number;
}

export interface DepartmentUpdateRequest {
  name: string;
  description?: string;
  managerId?: number;
  parentDepartmentId?: number;
  active?: boolean;
}

// ==========================================
// 5. Position Types
// ==========================================
export interface PositionResponse {
  id: number;
  title: string;
  code: string;
  description?: string | null;
  departmentId?: number | null;
  departmentName?: string | null;
  basicSalary: number;
  minSalary?: number | null;
  maxSalary?: number | null;
  employeeCount?: number;
  active: boolean;
  createdAt: string;
  updatedAt?: string | null;
}

export interface PositionCreateRequest {
  title: string;
  code: string;
  description?: string;
  departmentId?: number;
  basicSalary: number;
  minSalary?: number;
  maxSalary?: number;
}

export interface PositionUpdateRequest {
  title: string;
  description?: string;
  departmentId?: number;
  basicSalary?: number;
  minSalary?: number;
  maxSalary?: number;
  active?: boolean;
}

// ==========================================
// 6. Attendance Types & Enums
// ==========================================
export type AttendanceStatus =
  | 'PRESENT'
  | 'LATE'
  | 'EARLY_LEAVE'
  | 'LATE_AND_EARLY_LEAVE'
  | 'ABSENT'
  | 'ON_LEAVE';

export interface AttendanceResponse {
  id: number;
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  workDate: string;
  checkIn?: string | null;
  checkOut?: string | null;
  status: AttendanceStatus;
  totalWorkHours?: number;
  notes?: string | null;
  createdAt: string;
  updatedAt?: string | null;
}

export interface CheckInRequest {
  notes?: string;
}

export interface CheckOutRequest {
  notes?: string;
}

export interface AttendanceManualRequest {
  employeeId: number;
  workDate: string;
  checkIn?: string;
  checkOut?: string;
  status?: AttendanceStatus;
  notes?: string;
}

export interface AttendanceSearchParams extends PageableParams {
  keyword?: string;
  departmentId?: number;
  startDate?: string;
  endDate?: string;
  status?: AttendanceStatus;
}

// ==========================================
// 7. Leave Types & Enums
// ==========================================
export type LeaveRequestStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';

export interface LeaveTypeResponse {
  id: number;
  name: string;
  code: string;
  description?: string | null;
  paid: boolean;
  defaultDaysPerYear: number;
  active: boolean;
  createdAt: string;
}

export interface LeaveBalanceResponse {
  id: number;
  employeeId: number;
  employeeName: string;
  leaveTypeId: number;
  leaveTypeName: string;
  leaveTypeCode: string;
  year: number;
  totalDays: number;
  usedDays: number;
  remainingDays: number;
}

export interface LeaveRequestResponse {
  id: number;
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  leaveTypeId: number;
  leaveTypeName: string;
  startDate: string;
  endDate: string;
  totalDays: number;
  reason?: string | null;
  status: LeaveRequestStatus;
  approverId?: number | null;
  approverName?: string | null;
  rejectionReason?: string | null;
  createdAt: string;
}

export interface LeaveTypeCreateRequest {
  name: string;
  code: string;
  description?: string;
  paid: boolean;
  defaultDaysPerYear: number;
}

export interface LeaveRequestCreateRequest {
  leaveTypeId: number;
  startDate: string;
  endDate: string;
  reason?: string;
}

export interface LeaveApprovalRequest {
  rejectionReason?: string;
}

export interface LeaveSearchParams extends PageableParams {
  keyword?: string;
  departmentId?: number;
  leaveTypeId?: number;
  status?: LeaveRequestStatus;
  startDate?: string;
  endDate?: string;
}

// ==========================================
// 8. Payroll Types & Enums
// ==========================================
export type PayrollPeriodStatus = 'DRAFT' | 'CALCULATED' | 'APPROVED' | 'PAID';
export type PayslipStatus = 'CALCULATED' | 'APPROVED' | 'PAID';

export interface PayrollPeriodResponse {
  id: number;
  name: string;
  year: number;
  month: number;
  startDate: string;
  endDate: string;
  workingDays: number;
  status: PayrollPeriodStatus;
  createdAt: string;
}

export interface PayrollPeriodCreateRequest {
  name?: string;
  year: number;
  month: number;
  startDate: string;
  endDate: string;
  workingDays?: number;
}

export interface PayslipResponse {
  id: number;
  payrollPeriodId: number;
  payrollPeriodName: string;
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  basicSalary: number;
  actualWorkDays: number;
  grossSalary: number;
  allowances: number;
  deductions: number;
  tax: number;
  netSalary: number;
  status: PayslipStatus;
  notes?: string | null;
  createdAt: string;
}

// ==========================================
// 9. Dashboard Types
// ==========================================
export interface DashboardSummaryResponse {
  totalEmployees: number;
  activeEmployees: number;
  probationEmployees: number;
  totalDepartments: number;
  totalPositions: number;
  latestMonthlyPayrollCost?: number | null;
}

export interface AttendanceOverviewResponse {
  date: string;
  totalActiveEmployees: number;
  presentCount: number;
  lateCount: number;
  earlyLeaveCount: number;
  onLeaveCount: number;
  absentCount: number;
  attendanceRatePercentage: number;
}

export interface DepartmentStatsResponse {
  departmentId: number;
  departmentName: string;
  departmentCode: string;
  managerName?: string | null;
  employeeCount: number;
}

export interface PayrollSummaryResponse {
  periodId: number;
  periodName: string;
  year: number;
  month: number;
  totalEmployeesPaid: number;
  totalGrossSalary: number;
  totalTaxDeducted: number;
  totalNetSalary: number;
}

// ==========================================
// 10. Notification Types & Enums
// ==========================================
export type NotificationType =
  | 'LEAVE_SUBMITTED'
  | 'LEAVE_APPROVED'
  | 'LEAVE_REJECTED'
  | 'LEAVE_CANCELLED'
  | 'PAYSLIP_READY'
  | 'ATTENDANCE_ABSENT'
  | 'SYSTEM';

export interface NotificationResponse {
  id: number;
  userId: number;
  type: NotificationType;
  title: string;
  message: string;
  link?: string | null;
  isRead: boolean;
  createdAt: string;
}

// ==========================================
// 11. Audit Log Types
// ==========================================
export interface AuditLog {
  id: number;
  userId?: number | null;
  username?: string | null;
  action: string;
  entityName: string;
  entityId?: number | null;
  details?: string | null;
  ipAddress?: string | null;
  createdAt: string;
}

// ==========================================
// 12. User Management Types
// ==========================================
export interface UserManagementResponse {
  id: number;
  username: string;
  email: string;
  enabled: boolean;
  accountNonLocked: boolean;
  roles: string[];
  permissions?: string[];
  employeeId?: number | null;
  employeeCode?: string | null;
  employeeName?: string | null;
  createdAt: string;
  updatedAt?: string | null;
}

export interface UserCreateRequest {
  username: string;
  email: string;
  password?: string;
  roles: string[];
  employeeId?: number | null;
}

export interface UserUpdateRequest {
  email: string;
  roles: string[];
  enabled?: boolean;
  accountNonLocked?: boolean;
  employeeId?: number | null;
}

export interface AdminResetPasswordRequest {
  newPassword?: string;
}

export interface RoleResponse {
  id: number;
  name: string;
  description?: string;
}

export interface UserSearchParams extends PageableParams {
  keyword?: string;
  role?: string;
  enabled?: boolean;
}

// ==========================================
// 13. Excel Import & Export Types
// ==========================================
export interface ImportErrorDetail {
  rowNumber: number;
  identifier?: string;
  fieldName?: string;
  errorMessage: string;
}

export interface ImportResultResponse {
  totalRows: number;
  successCount: number;
  failedCount: number;
  errors: ImportErrorDetail[];
}

// ==========================================
// 14. KPI Performance Appraisal Types
// ==========================================
export type KpiRating = 'A' | 'B' | 'C' | 'D';
export type KpiEvaluationStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED';

export interface KpiCriteriaResponse {
  id: number;
  code: string;
  name: string;
  departmentId?: number | null;
  departmentName?: string | null;
  weight: number;
  targetDescription?: string | null;
  active: boolean;
  createdAt: string;
  updatedAt?: string | null;
}

export interface KpiCriteriaCreateRequest {
  code: string;
  name: string;
  departmentId?: number | null;
  weight: number;
  targetDescription?: string | null;
  active?: boolean;
}

export interface KpiCriteriaUpdateRequest {
  name: string;
  departmentId?: number | null;
  weight: number;
  targetDescription?: string | null;
  active: boolean;
}

export interface KpiEvaluationDetailRequest {
  kpiCriteriaId: number;
  score: number;
  comments?: string;
}

export interface KpiEvaluationDetailResponse {
  id: number;
  kpiCriteriaId: number;
  kpiCriteriaCode: string;
  kpiCriteriaName: string;
  score: number;
  weight: number;
  comments?: string | null;
}

export interface KpiEvaluationCreateRequest {
  employeeId: number;
  periodMonth: number;
  periodYear: number;
  feedback?: string;
  details: KpiEvaluationDetailRequest[];
}

export interface KpiEvaluationResponse {
  id: number;
  employeeId: number;
  employeeCode: string;
  employeeName: string;
  departmentName?: string | null;
  periodMonth: number;
  periodYear: number;
  evaluatorId?: number | null;
  evaluatorName?: string | null;
  totalScore: number;
  rating: KpiRating;
  kpiCoefficient: number;
  bonusAmount: number;
  status: KpiEvaluationStatus;
  feedback?: string | null;
  details: KpiEvaluationDetailResponse[];
  createdAt: string;
  updatedAt?: string | null;
}

export interface KpiSearchParams extends PageableParams {
  year?: number;
  month?: number;
  status?: KpiEvaluationStatus;
  departmentId?: number;
  keyword?: string;
}

// ==========================================
// 15. Salary Scale & Coefficient Types
// ==========================================
export interface SalaryScaleResponse {
  id: number;
  code: string;
  title: string;
  positionId?: number | null;
  positionTitle?: string | null;
  coefficient: number;
  baseSalary: number;
  standardBonus: number;
  calculatedSalary: number;
  active: boolean;
  createdAt: string;
  updatedAt?: string | null;
}

export interface SalaryScaleCreateRequest {
  code: string;
  title: string;
  positionId?: number | null;
  coefficient: number;
  baseSalary: number;
  standardBonus?: number;
  active?: boolean;
}

export interface SalaryScaleUpdateRequest {
  title: string;
  positionId?: number | null;
  coefficient: number;
  baseSalary: number;
  standardBonus?: number;
  active: boolean;
}

export interface SalaryScaleSearchParams extends PageableParams {
  keyword?: string;
  positionId?: number;
  active?: boolean;
}

