import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { ArrowLeft, Edit2, Phone, MapPin, Calendar, Building2, Briefcase, User } from 'lucide-react';
import { toast } from 'sonner';
import { employeeApi } from '@/api/employee.api';
import { departmentApi } from '@/api/department.api';
import { positionApi } from '@/api/position.api';
import { useAuthStore } from '@/stores/authStore';
import { PageHeader } from '@/components/shared/PageHeader';
import { StatusBadge } from '@/components/shared/StatusBadge';
import { formatDate } from '@/lib/utils';
import { DepartmentResponse, EmployeeResponse, PositionResponse } from '@/types';

export const EmployeeDetailPage: React.FC = () => {
  const { t } = useTranslation();
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const hasAnyRole = useAuthStore((state) => state.hasAnyRole);
  const canManage = hasAnyRole('ADMIN', 'HR', 'ROLE_ADMIN', 'ROLE_HR');

  const [employee, setEmployee] = useState<EmployeeResponse | null>(null);
  const [department, setDepartment] = useState<DepartmentResponse | null>(null);
  const [position, setPosition] = useState<PositionResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    setIsLoading(true);

    employeeApi
      .getById(Number(id))
      .then(async (emp) => {
        setEmployee(emp);
        if (emp.departmentId) {
          departmentApi.getById(emp.departmentId).then(setDepartment).catch(console.warn);
        }
        if (emp.positionId) {
          positionApi.getById(emp.positionId).then(setPosition).catch(console.warn);
        }
      })
      .catch((err) => {
        console.error('Failed to load employee:', err);
        toast.error('Không tìm thấy thông tin nhân viên');
        navigate('/employees');
      })
      .finally(() => setIsLoading(false));
  }, [id, navigate]);

  if (isLoading || !employee) {
    return (
      <div className="p-12 text-center text-xs text-gray-500">
        <span className="inline-block w-5 h-5 border-2 border-blue-600 border-t-transparent rounded-full animate-spin mr-2" />
        Đang tải thông tin nhân viên...
      </div>
    );
  }

  return (
    <div className="space-y-6 max-w-4xl">
      <PageHeader
        title={`${employee.firstName} ${employee.lastName}`}
        subtitle={`${employee.employeeCode} • ${position?.title || 'Nhân viên'}`}
        action={
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => navigate('/employees')}
              className="px-3 py-1.5 text-xs font-medium text-gray-600 hover:text-gray-900 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors flex items-center gap-1.5 shadow-2xs"
            >
              <ArrowLeft className="w-3.5 h-3.5" />
              <span>{t('actions.back')}</span>
            </button>
            {canManage && (
              <button
                type="button"
                onClick={() => navigate(`/employees/${employee.id}/edit`)}
                className="px-3.5 py-1.5 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors flex items-center gap-1.5 shadow-xs"
              >
                <Edit2 className="w-3.5 h-3.5" />
                <span>{t('actions.edit')}</span>
              </button>
            )}
          </div>
        }
      />

      {/* Header Profile Card */}
      <div className="bg-white p-6 rounded-xl border border-gray-200/80 shadow-[0_1px_3px_rgba(0,0,0,0.05)] flex flex-col sm:flex-row items-center sm:items-start gap-5">
        <div className="w-16 h-16 rounded-full bg-blue-600 text-white font-bold text-xl flex items-center justify-center shrink-0 shadow-sm">
          {employee.firstName ? employee.firstName[0] : 'N'}
        </div>
        <div className="flex-1 text-center sm:text-left space-y-1">
          <div className="flex flex-col sm:flex-row sm:items-center gap-2">
            <h2 className="text-lg font-bold text-gray-900">{employee.firstName} {employee.lastName}</h2>
            <StatusBadge status={employee.employmentStatus} />
          </div>
          <p className="text-xs text-gray-500 font-mono">{employee.employeeCode}</p>
          <div className="flex flex-wrap items-center justify-center sm:justify-start gap-4 pt-2 text-xs text-gray-600">
            {employee.phone && (
              <div className="flex items-center gap-1.5">
                <Phone className="w-3.5 h-3.5 text-gray-400" />
                <span>{employee.phone}</span>
              </div>
            )}
            {employee.address && (
              <div className="flex items-center gap-1.5">
                <MapPin className="w-3.5 h-3.5 text-gray-400" />
                <span>{employee.address}</span>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Info Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Personal Details */}
        <div className="bg-white p-5 rounded-xl border border-gray-200/80 shadow-[0_1px_3px_rgba(0,0,0,0.05)] space-y-3">
          <h3 className="text-xs font-bold text-gray-900 uppercase tracking-wider border-b border-gray-100 pb-2">
            {t('employee.personalInfo')}
          </h3>
          <div className="space-y-2.5 text-xs">
            <div className="flex justify-between py-1 border-b border-gray-50">
              <span className="text-gray-500">{t('employee.fields.gender', 'Giới tính')}</span>
              <span className="font-medium text-gray-900">{employee.gender ? t(`gender.${employee.gender}`) : '-'}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-gray-50">
              <span className="text-gray-500">{t('employee.dateOfBirth')}</span>
              <span className="font-medium text-gray-900">{formatDate(employee.dateOfBirth)}</span>
            </div>
            <div className="flex justify-between py-1 border-b border-gray-50">
              <span className="text-gray-500">{t('employee.phone')}</span>
              <span className="font-medium text-gray-900">{employee.phone || '-'}</span>
            </div>
            <div className="flex justify-between py-1">
              <span className="text-gray-500">{t('employee.address')}</span>
              <span className="font-medium text-gray-900 text-right max-w-xs">{employee.address || '-'}</span>
            </div>
          </div>
        </div>

        {/* Employment Details */}
        <div className="bg-white p-5 rounded-xl border border-gray-200/80 shadow-[0_1px_3px_rgba(0,0,0,0.05)] space-y-3">
          <h3 className="text-xs font-bold text-gray-900 uppercase tracking-wider border-b border-gray-100 pb-2">
            {t('employee.employmentInfo')}
          </h3>
          <div className="space-y-2.5 text-xs">
            <div className="flex justify-between py-1 border-b border-gray-50">
              <span className="text-gray-500">{t('employee.department')}</span>
              <span className="font-medium text-gray-900 flex items-center gap-1">
                <Building2 className="w-3.5 h-3.5 text-gray-400" />
                {department?.name || '-'}
              </span>
            </div>
            <div className="flex justify-between py-1 border-b border-gray-50">
              <span className="text-gray-500">{t('employee.position')}</span>
              <span className="font-medium text-gray-900 flex items-center gap-1">
                <Briefcase className="w-3.5 h-3.5 text-gray-400" />
                {position?.title || '-'}
              </span>
            </div>
            <div className="flex justify-between py-1 border-b border-gray-50">
              <span className="text-gray-500">{t('employee.hireDate')}</span>
              <span className="font-medium text-gray-900 flex items-center gap-1">
                <Calendar className="w-3.5 h-3.5 text-gray-400" />
                {formatDate(employee.hireDate)}
              </span>
            </div>
            <div className="flex justify-between py-1 border-b border-gray-50">
              <span className="text-gray-500">{t('employee.manager')}</span>
              <span className="font-medium text-gray-900 flex items-center gap-1">
                <User className="w-3.5 h-3.5 text-gray-400" />
                {employee.managerName || '-'}
              </span>
            </div>
            {employee.terminationDate && (
              <div className="flex justify-between py-1 text-rose-600 font-medium">
                <span>{t('employee.terminationDate')}</span>
                <span>{formatDate(employee.terminationDate)}</span>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
