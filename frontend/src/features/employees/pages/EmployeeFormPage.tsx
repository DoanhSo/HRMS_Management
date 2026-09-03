import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { ArrowLeft, Save } from 'lucide-react';
import { toast } from 'sonner';
import { employeeApi } from '@/api/employee.api';
import { departmentApi } from '@/api/department.api';
import { positionApi } from '@/api/position.api';
import { PageHeader } from '@/components/shared/PageHeader';
import { FormField } from '@/components/shared/FormField';
import { DepartmentResponse, EmploymentStatus, Gender, PositionResponse } from '@/types';

export const EmployeeFormPage: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEdit = !!id;

  const [isLoading, setIsLoading] = useState(false);
  const [isFetching, setIsFetching] = useState(isEdit);

  // Form Fields
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [dateOfBirth, setDateOfBirth] = useState('');
  const [gender, setGender] = useState<Gender>('MALE');
  const [phone, setPhone] = useState('');
  const [address, setAddress] = useState('');
  const [hireDate, setHireDate] = useState(new Date().toISOString().split('T')[0]);
  const [terminationDate, setTerminationDate] = useState('');
  const [employmentStatus, setEmploymentStatus] = useState<EmploymentStatus>('PROBATION');
  const [departmentId, setDepartmentId] = useState<number | undefined>();
  const [positionId, setPositionId] = useState<number | undefined>();
  const [managerId, setManagerId] = useState<number | undefined>();

  // Validation Errors
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  // Options
  const [departments, setDepartments] = useState<DepartmentResponse[]>([]);
  const [positions, setPositions] = useState<PositionResponse[]>([]);
  const [managers, setManagers] = useState<any[]>([]);

  useEffect(() => {
    departmentApi.getAllActive().then(setDepartments).catch(console.warn);
    positionApi.getAllActive().then(setPositions).catch(console.warn);
    employeeApi.search({ page: 0, size: 100 }).then((res) => setManagers(res.content)).catch(console.warn);

    if (isEdit && id) {
      setIsFetching(true);
      employeeApi
        .getById(Number(id))
        .then((emp) => {
          setFirstName(emp.firstName || '');
          setLastName(emp.lastName || '');
          setDateOfBirth(emp.dateOfBirth || '');
          setGender(emp.gender || 'MALE');
          setPhone(emp.phone || '');
          setAddress(emp.address || '');
          setHireDate(emp.hireDate || '');
          setTerminationDate(emp.terminationDate || '');
          setEmploymentStatus(emp.employmentStatus || 'PROBATION');
          setDepartmentId(emp.departmentId || undefined);
          setPositionId(emp.positionId || undefined);
          setManagerId(emp.managerId || undefined);
        })
        .catch((err) => {
          toast.error('Không tìm thấy thông tin nhân viên');
          navigate('/employees');
        })
        .finally(() => setIsFetching(false));
    }
  }, [id, isEdit, navigate]);

  const validateForm = (): boolean => {
    const errors: Record<string, string> = {};

    if (!firstName.trim()) {
      errors.firstName = 'Họ và tên đệm không được để trống';
    } else if (firstName.trim().length < 2) {
      errors.firstName = 'Họ và tên đệm phải có ít nhất 2 ký tự';
    }

    if (!lastName.trim()) {
      errors.lastName = 'Tên không được để trống';
    } else if (lastName.trim().length < 2) {
      errors.lastName = 'Tên phải có ít nhất 2 ký tự';
    }

    if (phone && !/(84|0[3|5|7|8|9])+([0-9]{8})\b/.test(phone)) {
      errors.phone = 'Số điện thoại không đúng định dạng (VD: 0912345678)';
    }

    if (dateOfBirth) {
      const birth = new Date(dateOfBirth);
      const today = new Date();
      const age = today.getFullYear() - birth.getFullYear();
      if (age < 18) {
        errors.dateOfBirth = 'Nhân viên phải từ 18 tuổi trở lên';
      }
    }

    if (!hireDate) {
      errors.hireDate = 'Vui lòng chọn ngày vào làm';
    }

    if (terminationDate && hireDate && new Date(terminationDate) < new Date(hireDate)) {
      errors.terminationDate = 'Ngày nghỉ việc phải sau ngày vào làm';
    }

    if (!departmentId) {
      errors.departmentId = 'Vui lòng chọn phòng ban';
    }

    if (!positionId) {
      errors.positionId = 'Vui lòng chọn chức vụ';
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateForm()) {
      toast.error('Vui lòng kiểm tra lại các trường thông tin bị lỗi');
      return;
    }

    setIsLoading(true);

    try {
      if (isEdit && id) {
        await employeeApi.update(Number(id), {
          firstName,
          lastName,
          dateOfBirth: dateOfBirth || undefined,
          gender,
          phone: phone || undefined,
          address: address || undefined,
          terminationDate: terminationDate || undefined,
          employmentStatus,
          departmentId,
          positionId,
          managerId,
        });
        toast.success(t('employee.messages.updateSuccess', 'Cập nhật nhân viên thành công'));
      } else {
        await employeeApi.create({
          firstName,
          lastName,
          dateOfBirth: dateOfBirth || undefined,
          gender,
          phone: phone || undefined,
          address: address || undefined,
          hireDate,
          employmentStatus,
          departmentId,
          positionId,
          managerId,
        });
        toast.success(t('employee.messages.createSuccess', 'Thêm nhân viên thành công'));
      }

      navigate('/employees');
    } catch (error: any) {
      console.error('Save employee error:', error);
      const errorMsg = error.response?.data?.message || 'Có lỗi xảy ra khi lưu nhân viên';
      toast.error(errorMsg);
    } finally {
      setIsLoading(false);
    }
  };

  if (isFetching) {
    return (
      <div className="p-12 text-center text-xs text-gray-500">
        <span className="inline-block w-5 h-5 border-2 border-blue-600 border-t-transparent rounded-full animate-spin mr-2" />
        Đang tải dữ liệu...
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-4xl">
      <PageHeader
        title={isEdit ? t('employee.editEmployee') : t('employee.addNew')}
        action={
          <button
            type="button"
            onClick={() => navigate('/employees')}
            className="px-3 py-1.5 text-xs font-medium text-gray-600 hover:text-gray-900 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors flex items-center gap-1.5 shadow-2xs"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>{t('actions.back')}</span>
          </button>
        }
      />

      <form onSubmit={handleSubmit} className="space-y-6" noValidate>
        {/* Section 1: Personal Info */}
        <div className="bg-white p-6 rounded-xl border border-gray-200/80 shadow-[0_1px_3px_rgba(0,0,0,0.05)] space-y-4">
          <h3 className="text-sm font-bold text-gray-900 border-b border-gray-100 pb-3">
            {t('employee.personalInfo')}
          </h3>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <FormField label={t('employee.firstName')} error={fieldErrors.firstName} required>
              <input
                type="text"
                value={firstName}
                onChange={(e) => {
                  setFirstName(e.target.value);
                  if (fieldErrors.firstName) setFieldErrors((prev) => ({ ...prev, firstName: '' }));
                }}
                placeholder="Nguyễn Văn"
                className={`w-full px-3 py-2 text-xs bg-white border rounded-lg text-gray-900 focus:outline-none focus:ring-2 transition-all ${
                  fieldErrors.firstName
                    ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                    : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                }`}
              />
            </FormField>

            <FormField label={t('employee.lastName')} error={fieldErrors.lastName} required>
              <input
                type="text"
                value={lastName}
                onChange={(e) => {
                  setLastName(e.target.value);
                  if (fieldErrors.lastName) setFieldErrors((prev) => ({ ...prev, lastName: '' }));
                }}
                placeholder="An"
                className={`w-full px-3 py-2 text-xs bg-white border rounded-lg text-gray-900 focus:outline-none focus:ring-2 transition-all ${
                  fieldErrors.lastName
                    ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                    : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                }`}
              />
            </FormField>

            <FormField label={t('employee.dateOfBirth')} error={fieldErrors.dateOfBirth}>
              <input
                type="date"
                value={dateOfBirth}
                onChange={(e) => {
                  setDateOfBirth(e.target.value);
                  if (fieldErrors.dateOfBirth) setFieldErrors((prev) => ({ ...prev, dateOfBirth: '' }));
                }}
                className="w-full px-3 py-2 text-xs bg-white border border-gray-200 rounded-lg text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
              />
            </FormField>

            <FormField label={t('employee.fields.gender', 'Giới tính')} required>
              <select
                value={gender}
                onChange={(e) => setGender(e.target.value as Gender)}
                className="w-full px-3 py-2 text-xs bg-white border border-gray-200 rounded-lg text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
              >
                <option value="MALE">{t('gender.MALE')}</option>
                <option value="FEMALE">{t('gender.FEMALE')}</option>
                <option value="OTHER">{t('gender.OTHER')}</option>
              </select>
            </FormField>

            <FormField label={t('employee.phone')} error={fieldErrors.phone} helperText="Định dạng 10 số (VD: 0912345678)">
              <input
                type="tel"
                value={phone}
                onChange={(e) => {
                  setPhone(e.target.value);
                  if (fieldErrors.phone) setFieldErrors((prev) => ({ ...prev, phone: '' }));
                }}
                placeholder="0912345678"
                className={`w-full px-3 py-2 text-xs bg-white border rounded-lg text-gray-900 focus:outline-none focus:ring-2 transition-all ${
                  fieldErrors.phone
                    ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                    : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                }`}
              />
            </FormField>

            <FormField label={t('employee.address')}>
              <input
                type="text"
                value={address}
                onChange={(e) => setAddress(e.target.value)}
                placeholder="Số nhà, đường, quận/huyện, TP"
                className="w-full px-3 py-2 text-xs bg-white border border-gray-200 rounded-lg text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
              />
            </FormField>
          </div>
        </div>

        {/* Section 2: Employment Info */}
        <div className="bg-white p-6 rounded-xl border border-gray-200/80 shadow-[0_1px_3px_rgba(0,0,0,0.05)] space-y-4">
          <h3 className="text-sm font-bold text-gray-900 border-b border-gray-100 pb-3">
            {t('employee.employmentInfo')}
          </h3>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <FormField label={t('employee.hireDate')} error={fieldErrors.hireDate} required>
              <input
                type="date"
                value={hireDate}
                onChange={(e) => {
                  setHireDate(e.target.value);
                  if (fieldErrors.hireDate) setFieldErrors((prev) => ({ ...prev, hireDate: '' }));
                }}
                className={`w-full px-3 py-2 text-xs bg-white border rounded-lg text-gray-900 focus:outline-none focus:ring-2 transition-all ${
                  fieldErrors.hireDate
                    ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                    : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                }`}
              />
            </FormField>

            {isEdit && (
              <FormField label={t('employee.terminationDate')} error={fieldErrors.terminationDate}>
                <input
                  type="date"
                  value={terminationDate}
                  onChange={(e) => {
                    setTerminationDate(e.target.value);
                    if (fieldErrors.terminationDate) setFieldErrors((prev) => ({ ...prev, terminationDate: '' }));
                  }}
                  className="w-full px-3 py-2 text-xs bg-white border border-gray-200 rounded-lg text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
                />
              </FormField>
            )}

            <FormField label={t('employee.status')} required>
              <select
                value={employmentStatus}
                onChange={(e) => setEmploymentStatus(e.target.value as EmploymentStatus)}
                className="w-full px-3 py-2 text-xs bg-white border border-gray-200 rounded-lg text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
              >
                <option value="PROBATION">{t('status.PROBATION')}</option>
                <option value="ACTIVE">{t('status.ACTIVE')}</option>
                <option value="ON_LEAVE">{t('status.ON_LEAVE')}</option>
                {isEdit && <option value="TERMINATED">{t('status.TERMINATED')}</option>}
              </select>
            </FormField>

            <FormField label={t('employee.department')} error={fieldErrors.departmentId} required>
              <select
                value={departmentId ?? ''}
                onChange={(e) => {
                  setDepartmentId(e.target.value ? Number(e.target.value) : undefined);
                  if (fieldErrors.departmentId) setFieldErrors((prev) => ({ ...prev, departmentId: '' }));
                }}
                className={`w-full px-3 py-2 text-xs bg-white border rounded-lg text-gray-900 focus:outline-none focus:ring-2 transition-all ${
                  fieldErrors.departmentId
                    ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                    : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                }`}
              >
                <option value="">-- {t('form.selectPlaceholder')} --</option>
                {departments.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.name}
                  </option>
                ))}
              </select>
            </FormField>

            <FormField label={t('employee.position')} error={fieldErrors.positionId} required>
              <select
                value={positionId ?? ''}
                onChange={(e) => {
                  setPositionId(e.target.value ? Number(e.target.value) : undefined);
                  if (fieldErrors.positionId) setFieldErrors((prev) => ({ ...prev, positionId: '' }));
                }}
                className={`w-full px-3 py-2 text-xs bg-white border rounded-lg text-gray-900 focus:outline-none focus:ring-2 transition-all ${
                  fieldErrors.positionId
                    ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                    : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                }`}
              >
                <option value="">-- {t('form.selectPlaceholder')} --</option>
                {positions.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.title}
                  </option>
                ))}
              </select>
            </FormField>

            <FormField label={t('employee.manager')}>
              <select
                value={managerId ?? ''}
                onChange={(e) => setManagerId(e.target.value ? Number(e.target.value) : undefined)}
                className="w-full px-3 py-2 text-xs bg-white border border-gray-200 rounded-lg text-gray-900 focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500"
              >
                <option value="">-- {t('form.selectPlaceholder')} --</option>
                {managers
                  .filter((m) => !isEdit || m.id !== Number(id))
                  .map((m) => (
                    <option key={m.id} value={m.id}>
                      {m.firstName} {m.lastName} ({m.employeeCode})
                    </option>
                  ))}
              </select>
            </FormField>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center justify-end gap-3 pt-2">
          <button
            type="button"
            onClick={() => navigate('/employees')}
            disabled={isLoading}
            className="px-4 py-2 text-xs font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer"
          >
            {t('actions.cancel')}
          </button>
          <button
            type="submit"
            disabled={isLoading}
            className="px-4 py-2 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors flex items-center gap-2 shadow-xs cursor-pointer disabled:opacity-50"
          >
            {isLoading ? (
              <span className="w-3.5 h-3.5 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              <Save className="w-3.5 h-3.5" />
            )}
            <span>{t('actions.save')}</span>
          </button>
        </div>
      </form>
    </div>
  );
};
