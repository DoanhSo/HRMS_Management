import { z } from 'zod';

// ==========================================
// 1. Auth Validations
// ==========================================
export const loginSchema = z.object({
  username: z
    .string({ required_error: 'Vui lòng nhập tên đăng nhập' })
    .min(3, 'Tên đăng nhập phải có ít nhất 3 ký tự')
    .max(50, 'Tên đăng nhập không được vượt quá 50 ký tự')
    .trim(),
  password: z
    .string({ required_error: 'Vui lòng nhập mật khẩu' })
    .min(6, 'Mật khẩu phải có ít nhất 6 ký tự')
    .max(100, 'Mật khẩu không được vượt quá 100 ký tự'),
});

export type LoginFormData = z.infer<typeof loginSchema>;

export const changePasswordSchema = z
  .object({
    oldPassword: z
      .string({ required_error: 'Vui lòng nhập mật khẩu hiện tại' })
      .min(1, 'Vui lòng nhập mật khẩu hiện tại'),
    newPassword: z
      .string({ required_error: 'Vui lòng nhập mật khẩu mới' })
      .min(6, 'Mật khẩu mới phải có ít nhất 6 ký tự')
      .max(100, 'Mật khẩu mới không được vượt quá 100 ký tự'),
    confirmPassword: z
      .string({ required_error: 'Vui lòng xác nhận mật khẩu mới' })
      .min(1, 'Vui lòng xác nhận mật khẩu mới'),
  })
  .refine((data) => data.newPassword === data.confirmPassword, {
    message: 'Mật khẩu xác nhận không khớp với mật khẩu mới',
    path: ['confirmPassword'],
  });

export type ChangePasswordFormData = z.infer<typeof changePasswordSchema>;

// ==========================================
// 2. Employee Validations
// ==========================================
const phoneRegex = /(84|0[3|5|7|8|9])+([0-9]{8})\b/;

export const employeeSchema = z.object({
  firstName: z
    .string({ required_error: 'Họ và tên đệm không được để trống' })
    .min(1, 'Họ và tên đệm không được để trống')
    .max(50, 'Họ và tên đệm không quá 50 ký tự')
    .trim(),
  lastName: z
    .string({ required_error: 'Tên không được để trống' })
    .min(1, 'Tên không được để trống')
    .max(50, 'Tên không quá 50 ký tự')
    .trim(),
  email: z
    .string({ required_error: 'Email không được để trống' })
    .min(1, 'Email không được để trống')
    .email('Email không đúng định dạng (VD: example@company.com)')
    .trim(),
  phone: z
    .string()
    .optional()
    .refine((val) => !val || phoneRegex.test(val), {
      message: 'Số điện thoại không hợp lệ (VD: 0912345678)',
    }),
  gender: z.enum(['MALE', 'FEMALE', 'OTHER'], {
    required_error: 'Vui lòng chọn giới tính',
  }),
  dateOfBirth: z
    .string()
    .optional()
    .refine(
      (val) => {
        if (!val) return true;
        const birthDate = new Date(val);
        const today = new Date();
        const age = today.getFullYear() - birthDate.getFullYear();
        return age >= 18;
      },
      { message: 'Nhân viên phải từ 18 tuổi trở lên' }
    ),
  hireDate: z
    .string({ required_error: 'Ngày vào làm không được để trống' })
    .min(1, 'Ngày vào làm không được để trống'),
  employmentStatus: z.enum(['PROBATION', 'ACTIVE', 'ON_LEAVE', 'TERMINATED'], {
    required_error: 'Vui lòng chọn trạng thái làm việc',
  }),
  departmentId: z
    .number({ required_error: 'Vui lòng chọn phòng ban' })
    .min(1, 'Vui lòng chọn phòng ban'),
  positionId: z
    .number({ required_error: 'Vui lòng chọn chức vụ' })
    .min(1, 'Vui lòng chọn chức vụ'),
  managerId: z.number().optional().nullable(),
  address: z.string().max(500, 'Địa chỉ không vượt quá 500 ký tự').optional(),
});

export type EmployeeFormData = z.infer<typeof employeeSchema>;

// ==========================================
// 3. Department Validations
// ==========================================
export const departmentSchema = z.object({
  name: z
    .string({ required_error: 'Tên phòng ban không được để trống' })
    .min(2, 'Tên phòng ban phải có ít nhất 2 ký tự')
    .max(100, 'Tên phòng ban không quá 100 ký tự')
    .trim(),
  code: z
    .string({ required_error: 'Mã phòng ban không được để trống' })
    .min(2, 'Mã phòng ban phải có ít nhất 2 ký tự')
    .max(20, 'Mã phòng ban không quá 20 ký tự')
    .regex(/^[A-Z0-9_-]+$/, 'Mã phòng ban chỉ gồm chữ hoa, số và dấu gạch nối (VD: IT_DEV)')
    .trim(),
  description: z.string().max(500, 'Mô tả không quá 500 ký tự').optional(),
  managerId: z.number().optional().nullable(),
  parentDepartmentId: z.number().optional().nullable(),
  active: z.boolean().default(true),
});

export type DepartmentFormData = z.infer<typeof departmentSchema>;

// ==========================================
// 4. Position Validations
// ==========================================
export const positionSchema = z
  .object({
    title: z
      .string({ required_error: 'Tên chức vụ không được để trống' })
      .min(2, 'Tên chức vụ phải có ít nhất 2 ký tự')
      .max(100, 'Tên chức vụ không quá 100 ký tự')
      .trim(),
    code: z
      .string({ required_error: 'Mã chức vụ không được để trống' })
      .min(2, 'Mã chức vụ phải có ít nhất 2 ký tự')
      .max(20, 'Mã chức vụ không quá 20 ký tự')
      .regex(/^[A-Z0-9_-]+$/, 'Mã chức vụ chỉ gồm chữ hoa, số và dấu gạch nối (VD: DEV_SR)')
      .trim(),
    departmentId: z
      .number({ required_error: 'Vui lòng chọn phòng ban' })
      .min(1, 'Vui lòng chọn phòng ban'),
    basicSalary: z
      .number({ required_error: 'Lương cơ bản không được để trống' })
      .min(1, 'Lương cơ bản phải lớn hơn 0 ₫'),
    minSalary: z
      .number()
      .min(0, 'Lương tối thiểu không được âm')
      .optional()
      .nullable(),
    maxSalary: z
      .number()
      .min(0, 'Lương tối đa không được âm')
      .optional()
      .nullable(),
    description: z.string().max(500, 'Mô tả không quá 500 ký tự').optional(),
    active: z.boolean().default(true),
  })
  .refine(
    (data) => {
      if (data.minSalary != null && data.maxSalary != null) {
        return data.maxSalary >= data.minSalary;
      }
      return true;
    },
    {
      message: 'Mức lương tối đa phải lớn hơn hoặc bằng mức lương tối thiểu',
      path: ['maxSalary'],
    }
  );

export type PositionFormData = z.infer<typeof positionSchema>;

// ==========================================
// 5. Leave Request Validations
// ==========================================
export const leaveRequestSchema = z
  .object({
    leaveTypeId: z
      .number({ required_error: 'Vui lòng chọn loại nghỉ phép' })
      .min(1, 'Vui lòng chọn loại nghỉ phép'),
    startDate: z
      .string({ required_error: 'Vui lòng chọn ngày bắt đầu' })
      .min(1, 'Vui lòng chọn ngày bắt đầu'),
    endDate: z
      .string({ required_error: 'Vui lòng chọn ngày kết thúc' })
      .min(1, 'Vui lòng chọn ngày kết thúc'),
    totalDays: z
      .number({ required_error: 'Vui lòng nhập số ngày nghỉ' })
      .min(0.5, 'Số ngày nghỉ tối thiểu là 0.5 ngày')
      .max(365, 'Số ngày nghỉ không được vượt quá 365 ngày'),
    reason: z
      .string({ required_error: 'Vui lòng nhập lý do nghỉ phép' })
      .min(5, 'Lý do nghỉ phép phải có ít nhất 5 ký tự')
      .max(500, 'Lý do không quá 500 ký tự')
      .trim(),
  })
  .refine(
    (data) => {
      const start = new Date(data.startDate);
      const end = new Date(data.endDate);
      return end >= start;
    },
    {
      message: 'Ngày kết thúc phải bằng hoặc sau ngày bắt đầu',
      path: ['endDate'],
    }
  );

export type LeaveRequestFormData = z.infer<typeof leaveRequestSchema>;

// ==========================================
// 6. Manual Attendance Validations
// ==========================================
export const manualAttendanceSchema = z
  .object({
    employeeId: z
      .number({ required_error: 'Vui lòng chọn nhân viên' })
      .min(1, 'Vui lòng chọn nhân viên'),
    workDate: z
      .string({ required_error: 'Vui lòng chọn ngày làm việc' })
      .min(1, 'Vui lòng chọn ngày làm việc')
      .refine(
        (val) => {
          const selected = new Date(val);
          const today = new Date();
          today.setHours(23, 59, 59, 999);
          return selected <= today;
        },
        { message: 'Không thể chấm công cho ngày trong tương lai' }
      ),
    checkIn: z.string().optional().nullable(),
    checkOut: z.string().optional().nullable(),
    status: z.enum(
      [
        'PRESENT',
        'LATE',
        'EARLY_LEAVE',
        'LATE_AND_EARLY_LEAVE',
        'ABSENT',
        'ON_LEAVE',
      ],
      { required_error: 'Vui lòng chọn trạng thái chấm công' }
    ),
    notes: z.string().max(255, 'Ghi chú không quá 255 ký tự').optional(),
  })
  .refine(
    (data) => {
      if (data.checkIn && data.checkOut) {
        return data.checkOut > data.checkIn;
      }
      return true;
    },
    {
      message: 'Giờ check-out phải sau giờ check-in',
      path: ['checkOut'],
    }
  );

export type ManualAttendanceFormData = z.infer<typeof manualAttendanceSchema>;

// ==========================================
// 7. Payroll Period Validations
// ==========================================
export const payrollPeriodSchema = z
  .object({
    name: z
      .string({ required_error: 'Tên kỳ lương không được để trống' })
      .min(3, 'Tên kỳ lương phải có ít nhất 3 ký tự')
      .max(100, 'Tên kỳ lương không quá 100 ký tự')
      .trim(),
    year: z
      .number({ required_error: 'Vui lòng nhập năm' })
      .min(2020, 'Năm không hợp lệ (từ 2020 trở lên)')
      .max(2099, 'Năm không hợp lệ'),
    month: z
      .number({ required_error: 'Vui lòng nhập tháng' })
      .min(1, 'Tháng phải từ 1 đến 12')
      .max(12, 'Tháng phải từ 1 đến 12'),
    startDate: z
      .string({ required_error: 'Vui lòng chọn ngày bắt đầu' })
      .min(1, 'Vui lòng chọn ngày bắt đầu'),
    endDate: z
      .string({ required_error: 'Vui lòng chọn ngày kết thúc' })
      .min(1, 'Vui lòng chọn ngày kết thúc'),
    workingDays: z
      .number({ required_error: 'Vui lòng nhập số ngày công chuẩn' })
      .min(1, 'Số ngày công chuẩn phải từ 1 đến 31')
      .max(31, 'Số ngày công chuẩn tối đa 31 ngày'),
  })
  .refine(
    (data) => {
      const start = new Date(data.startDate);
      const end = new Date(data.endDate);
      return end >= start;
    },
    {
      message: 'Ngày kết thúc phải bằng hoặc sau ngày bắt đầu',
      path: ['endDate'],
    }
  );

export type PayrollPeriodFormData = z.infer<typeof payrollPeriodSchema>;
