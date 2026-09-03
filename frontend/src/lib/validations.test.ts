import { describe, it, expect } from 'vitest';
import {
  loginSchema,
  changePasswordSchema,
  employeeSchema,
  departmentSchema,
  positionSchema,
  leaveRequestSchema,
  payrollPeriodSchema,
  manualAttendanceSchema,
} from './validations';

describe('Validations Module', () => {
  describe('loginSchema', () => {
    it('should validate correct login credentials', () => {
      const result = loginSchema.safeParse({
        username: 'admin',
        password: 'Password123',
      });
      expect(result.success).toBe(true);
    });

    it('should reject short username and password', () => {
      const result = loginSchema.safeParse({
        username: 'ad',
        password: '123',
      });
      expect(result.success).toBe(false);
      if (!result.success) {
        expect(result.error.flatten().fieldErrors.username).toBeDefined();
        expect(result.error.flatten().fieldErrors.password).toBeDefined();
      }
    });
  });

  describe('changePasswordSchema', () => {
    it('should validate when passwords match', () => {
      const result = changePasswordSchema.safeParse({
        oldPassword: 'OldPassword123',
        newPassword: 'NewPassword123',
        confirmPassword: 'NewPassword123',
      });
      expect(result.success).toBe(true);
    });

    it('should reject when confirmPassword does not match', () => {
      const result = changePasswordSchema.safeParse({
        oldPassword: 'OldPassword123',
        newPassword: 'NewPassword123',
        confirmPassword: 'DifferentPassword123',
      });
      expect(result.success).toBe(false);
    });
  });

  describe('employeeSchema', () => {
    it('should validate valid employee payload', () => {
      const result = employeeSchema.safeParse({
        firstName: 'Nguyễn',
        lastName: 'Văn A',
        email: 'vana@company.com',
        phone: '0912345678',
        gender: 'MALE',
        dateOfBirth: '1995-05-15',
        hireDate: '2025-01-01',
        employmentStatus: 'ACTIVE',
        departmentId: 1,
        positionId: 2,
      });
      expect(result.success).toBe(true);
    });

    it('should reject invalid email and phone format', () => {
      const result = employeeSchema.safeParse({
        firstName: 'Nguyễn',
        lastName: 'Văn A',
        email: 'invalid-email',
        phone: '12345',
        gender: 'MALE',
        hireDate: '2025-01-01',
        employmentStatus: 'ACTIVE',
        departmentId: 1,
        positionId: 2,
      });
      expect(result.success).toBe(false);
      if (!result.success) {
        const errors = result.error.flatten().fieldErrors;
        expect(errors.email).toBeDefined();
        expect(errors.phone).toBeDefined();
      }
    });
  });

  describe('departmentSchema', () => {
    it('should validate uppercase code and valid name', () => {
      const result = departmentSchema.safeParse({
        code: 'IT_DEV',
        name: 'Phòng Phát Triển Phần Mềm',
      });
      expect(result.success).toBe(true);
    });

    it('should reject invalid code pattern', () => {
      const result = departmentSchema.safeParse({
        code: 'it dev with space',
        name: 'A',
      });
      expect(result.success).toBe(false);
    });
  });

  describe('positionSchema', () => {
    it('should validate valid position', () => {
      const result = positionSchema.safeParse({
        code: 'DEV_SR',
        title: 'Senior Developer',
        departmentId: 1,
        basicSalary: 25000000,
        minSalary: 20000000,
        maxSalary: 35000000,
      });
      expect(result.success).toBe(true);
    });

    it('should reject when maxSalary < minSalary', () => {
      const result = positionSchema.safeParse({
        code: 'DEV_SR',
        title: 'Senior Developer',
        departmentId: 1,
        basicSalary: 25000000,
        minSalary: 30000000,
        maxSalary: 20000000,
      });
      expect(result.success).toBe(false);
    });
  });

  describe('leaveRequestSchema', () => {
    it('should validate valid leave dates', () => {
      const result = leaveRequestSchema.safeParse({
        leaveTypeId: 1,
        startDate: '2026-08-25',
        endDate: '2026-08-27',
        totalDays: 3,
        reason: 'Nghỉ phép thường niên',
      });
      expect(result.success).toBe(true);
    });

    it('should reject when endDate < startDate', () => {
      const result = leaveRequestSchema.safeParse({
        leaveTypeId: 1,
        startDate: '2026-08-27',
        endDate: '2026-08-25',
        totalDays: 3,
        reason: 'Nghỉ phép thường niên',
      });
      expect(result.success).toBe(false);
    });
  });

  describe('manualAttendanceSchema', () => {
    it('should validate valid attendance record', () => {
      const result = manualAttendanceSchema.safeParse({
        employeeId: 1,
        workDate: '2026-08-19',
        checkIn: '08:30',
        checkOut: '17:30',
        status: 'PRESENT',
      });
      expect(result.success).toBe(true);
    });

    it('should reject when checkOut is earlier than checkIn', () => {
      const result = manualAttendanceSchema.safeParse({
        employeeId: 1,
        workDate: '2026-08-19',
        checkIn: '17:30',
        checkOut: '08:30',
        status: 'PRESENT',
      });
      expect(result.success).toBe(false);
    });
  });

  describe('payrollPeriodSchema', () => {
    it('should validate valid period', () => {
      const result = payrollPeriodSchema.safeParse({
        name: 'Kỳ Lương Tháng 08/2026',
        month: 8,
        year: 2026,
        startDate: '2026-08-01',
        endDate: '2026-08-31',
        workingDays: 22,
      });
      expect(result.success).toBe(true);
    });

    it('should reject invalid month and working days', () => {
      const result = payrollPeriodSchema.safeParse({
        name: 'Kỳ Lương',
        month: 13,
        year: 2026,
        startDate: '2026-08-01',
        endDate: '2026-08-31',
        workingDays: 45,
      });
      expect(result.success).toBe(false);
    });
  });
});
