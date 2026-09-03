import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Lock, User, Eye, EyeOff, AlertCircle } from 'lucide-react';
import { authApi } from '@/api/auth.api';
import { useAuthStore } from '@/stores/authStore';
import { loginSchema, LoginFormData } from '@/lib/validations';
import { FormField } from '@/components/shared/FormField';
import { toast } from 'sonner';

export const LoginPage: React.FC = () => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const login = useAuthStore((state) => state.login);
  const [showPassword, setShowPassword] = useState(false);
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      username: '',
      password: '',
    },
  });

  const onSubmit = async (data: LoginFormData) => {
    setServerError(null);
    try {
      const tokenResponse = await authApi.login({
        username: data.username,
        password: data.password,
      });

      // Fetch user profile
      let userResponse = null;
      try {
        localStorage.setItem('hrms_access_token', tokenResponse.accessToken);
        userResponse = await authApi.getCurrentUser();
      } catch (err) {
        console.warn('Could not fetch user profile immediately:', err);
      }

      login(tokenResponse, userResponse || undefined);
      toast.success(t('auth.loginSuccess'));
      navigate('/dashboard', { replace: true });
    } catch (error: any) {
      console.error('Login error:', error);
      const msg =
        error.response?.data?.message ||
        (error.response?.status === 401
          ? 'Tên đăng nhập hoặc mật khẩu không chính xác'
          : t('auth.loginFailed'));
      setServerError(msg);
      toast.error(msg);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center p-4 sm:p-6">
      {/* Brand Header */}
      <div className="w-full max-w-md text-center mb-8">
        <div className="inline-flex items-center justify-center w-12 h-12 rounded-xl bg-blue-600 text-white font-bold text-lg mb-3 shadow-md shadow-blue-500/20">
          HR
        </div>
        <h1 className="text-2xl font-bold text-gray-900 tracking-tight">{t('app.name')}</h1>
        <p className="text-xs text-gray-500 mt-1">{t('app.tagline')}</p>
      </div>

      {/* Card */}
      <div className="w-full max-w-md bg-white rounded-2xl border border-gray-200/80 shadow-[0_4px_24px_rgba(0,0,0,0.04)] p-6 sm:p-8">
        <div className="mb-6 text-center sm:text-left">
          <h2 className="text-lg font-bold text-gray-900 tracking-tight">{t('auth.signInTitle')}</h2>
          <p className="text-xs text-gray-500 mt-1">{t('auth.signInSubtitle')}</p>
        </div>

        {serverError && (
          <div className="mb-5 p-3 rounded-lg bg-rose-50 border border-rose-200/80 flex items-start gap-2.5 text-rose-700 text-xs animate-in fade-in">
            <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
            <span className="leading-relaxed font-medium">{serverError}</span>
          </div>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4" noValidate>
          {/* Username Input */}
          <FormField label={t('auth.username')} error={errors.username?.message} required>
            <div className="relative">
              <User className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
              <input
                type="text"
                {...register('username')}
                placeholder={t('auth.usernamePlaceholder')}
                disabled={isSubmitting}
                className={`w-full pl-9 pr-3 py-2.5 text-xs bg-white border rounded-lg text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 transition-all shadow-[0_1px_2px_rgba(0,0,0,0.02)] ${
                  errors.username
                    ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                    : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                }`}
              />
            </div>
          </FormField>

          {/* Password Input */}
          <FormField label={t('auth.password')} error={errors.password?.message} required>
            <div className="relative">
              <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
              <input
                type={showPassword ? 'text' : 'password'}
                {...register('password')}
                placeholder={t('auth.passwordPlaceholder')}
                disabled={isSubmitting}
                className={`w-full pl-9 pr-10 py-2.5 text-xs bg-white border rounded-lg text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 transition-all shadow-[0_1px_2px_rgba(0,0,0,0.02)] ${
                  errors.password
                    ? 'border-rose-400 focus:ring-rose-500/20 focus:border-rose-500'
                    : 'border-gray-200 focus:ring-blue-500/20 focus:border-blue-500'
                }`}
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 focus:outline-none"
              >
                {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
          </FormField>

          {/* Submit Button */}
          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full mt-2 py-2.5 px-4 bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold rounded-lg shadow-sm hover:shadow transition-all disabled:opacity-50 flex items-center justify-center gap-2 cursor-pointer"
          >
            {isSubmitting ? (
              <span className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              t('auth.signIn')
            )}
          </button>
        </form>
      </div>

      <p className="mt-8 text-center text-xs text-gray-400">
        &copy; {new Date().getFullYear()} {t('app.fullName')}. All rights reserved.
      </p>
    </div>
  );
};
