import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Lock, Save } from 'lucide-react';
import { toast } from 'sonner';
import { authApi } from '@/api/auth.api';
import { useAuthStore } from '@/stores/authStore';
import { PageHeader } from '@/components/shared/PageHeader';
import { FormField } from '@/components/shared/FormField';
import { changePasswordSchema, ChangePasswordFormData } from '@/lib/validations';

export const ChangePasswordPage: React.FC = () => {
  const { t } = useTranslation();
  const logout = useAuthStore((state) => state.logout);
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
  } = useForm<ChangePasswordFormData>({
    resolver: zodResolver(changePasswordSchema),
    defaultValues: {
      oldPassword: '',
      newPassword: '',
      confirmPassword: '',
    },
  });

  const onSubmit = async (data: ChangePasswordFormData) => {
    setServerError(null);
    try {
      await authApi.changePassword({
        oldPassword: data.oldPassword,
        newPassword: data.newPassword,
      });
      toast.success(t('auth.changePasswordSuccess'));
      reset();
      setTimeout(() => {
        logout();
      }, 1500);
    } catch (err: any) {
      const msg =
        err.response?.data?.message ||
        'Không thể đổi mật khẩu. Vui lòng kiểm tra lại mật khẩu hiện tại.';
      setServerError(msg);
      toast.error(msg);
    }
  };

  return (
    <div className="space-y-6 max-w-xl">
      <PageHeader
        title={t('nav.changePassword')}
        subtitle="Cập nhật mật khẩu để bảo vệ an toàn cho tài khoản của bạn"
      />

      <div className="bg-white rounded-xl border border-gray-200/80 p-6 shadow-[0_1px_3px_rgba(0,0,0,0.05)]">
        {serverError && (
          <div className="mb-4 p-3 rounded-lg bg-rose-50 border border-rose-200/80 text-rose-700 text-xs font-medium">
            {serverError}
          </div>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
          <FormField
            label={t('auth.oldPassword')}
            error={errors.oldPassword?.message}
            required
          >
            <div className="relative">
              <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
              <input
                type="password"
                {...register('oldPassword')}
                placeholder="Nhập mật khẩu hiện tại"
                className={`w-full pl-9 pr-3 py-2 text-xs bg-white border rounded-lg text-gray-900 focus:outline-none focus:ring-2 transition-all ${
                  errors.oldPassword
                    ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                    : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                }`}
              />
            </div>
          </FormField>

          <FormField
            label={t('auth.newPassword')}
            error={errors.newPassword?.message}
            required
            helperText="Tối thiểu 6 ký tự"
          >
            <div className="relative">
              <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
              <input
                type="password"
                {...register('newPassword')}
                placeholder="Nhập mật khẩu mới"
                className={`w-full pl-9 pr-3 py-2 text-xs bg-white border rounded-lg text-gray-900 focus:outline-none focus:ring-2 transition-all ${
                  errors.newPassword
                    ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                    : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                }`}
              />
            </div>
          </FormField>

          <FormField
            label={t('auth.confirmPassword')}
            error={errors.confirmPassword?.message}
            required
          >
            <div className="relative">
              <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
              <input
                type="password"
                {...register('confirmPassword')}
                placeholder="Nhập lại mật khẩu mới"
                className={`w-full pl-9 pr-3 py-2 text-xs bg-white border rounded-lg text-gray-900 focus:outline-none focus:ring-2 transition-all ${
                  errors.confirmPassword
                    ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                    : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                }`}
              />
            </div>
          </FormField>

          <div className="pt-2">
            <button
              type="submit"
              disabled={isSubmitting}
              className="px-4 py-2 text-xs font-semibold text-white bg-blue-600 hover:bg-blue-700 rounded-lg flex items-center gap-1.5 shadow-xs transition-colors cursor-pointer disabled:opacity-50"
            >
              {isSubmitting ? (
                <span className="w-3.5 h-3.5 border-2 border-white border-t-transparent rounded-full animate-spin" />
              ) : (
                <Save className="w-3.5 h-3.5" />
              )}
              <span>{t('actions.save')}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
