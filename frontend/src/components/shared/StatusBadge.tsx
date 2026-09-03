import React from 'react';
import { useTranslation } from 'react-i18next';
import { cn } from '@/lib/utils';

interface StatusBadgeProps {
  status: string | null | undefined;
  className?: string;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ status, className }) => {
  const { t } = useTranslation();

  if (!status) return <span>-</span>;

  // Minimalist clean badge styling tokens
  const getBadgeStyle = (val: string) => {
    switch (val) {
      case 'ACTIVE':
      case 'PRESENT':
      case 'APPROVED':
      case 'PAID':
        return 'bg-emerald-50 text-emerald-700 border-emerald-200/60';
      case 'PROBATION':
      case 'LATE':
      case 'PENDING':
      case 'EARLY_LEAVE':
      case 'LATE_AND_EARLY_LEAVE':
        return 'bg-amber-50 text-amber-700 border-amber-200/60';
      case 'TERMINATED':
      case 'ABSENT':
      case 'REJECTED':
        return 'bg-rose-50 text-rose-700 border-rose-200/60';
      case 'ON_LEAVE':
        return 'bg-indigo-50 text-indigo-700 border-indigo-200/60';
      case 'CALCULATED':
        return 'bg-blue-50 text-blue-700 border-blue-200/60';
      case 'CANCELLED':
      case 'DRAFT':
      default:
        return 'bg-gray-100 text-gray-700 border-gray-200/60';
    }
  };

  const label = t(`status.${status}`, status);

  return (
    <span
      className={cn(
        'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium border transition-colors',
        getBadgeStyle(status),
        className
      )}
    >
      <span className="w-1.5 h-1.5 rounded-full mr-1.5 bg-current opacity-70" />
      {label}
    </span>
  );
};
