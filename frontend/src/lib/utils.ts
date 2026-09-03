import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';
import { format, formatDistanceToNow } from 'date-fns';
import { vi } from 'date-fns/locale';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export function formatCurrency(amount: number | null | undefined): string {
  if (amount == null) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0,
  }).format(amount);
}

export function formatDate(dateString: string | null | undefined): string {
  if (!dateString) return '-';
  try {
    const date = new Date(dateString);
    return format(date, 'dd/MM/yyyy');
  } catch {
    return dateString;
  }
}

export function formatDateTime(dateTimeString: string | null | undefined): string {
  if (!dateTimeString) return '-';
  try {
    const date = new Date(dateTimeString);
    return format(date, 'dd/MM/yyyy HH:mm');
  } catch {
    return dateTimeString;
  }
}

export function timeAgo(dateTimeString: string | null | undefined): string {
  if (!dateTimeString) return '';
  try {
    const date = new Date(dateTimeString);
    return formatDistanceToNow(date, { addSuffix: true, locale: vi });
  } catch {
    return dateTimeString;
  }
}
