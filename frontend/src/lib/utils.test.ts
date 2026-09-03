import { describe, it, expect } from 'vitest';
import { cn, formatCurrency, formatDate, formatDateTime, timeAgo } from './utils';

describe('Frontend Utils', () => {
  it('cn should merge class names correctly', () => {
    const result = cn('bg-red-500', false && 'hidden', 'text-white', undefined);
    expect(result).toBe('bg-red-500 text-white');
  });

  it('formatCurrency should format VND currency properly', () => {
    expect(formatCurrency(15000000)).toMatch(/15\.000\.000\s?₫/);
    expect(formatCurrency(0)).toMatch(/0\s?₫/);
    expect(formatCurrency(undefined)).toBe('0 ₫');
    expect(formatCurrency(null)).toBe('0 ₫');
  });

  it('formatDate should format YYYY-MM-DD properly', () => {
    expect(formatDate('2026-08-20')).toBe('20/08/2026');
    expect(formatDate(undefined)).toBe('-');
  });

  it('formatDateTime should format ISO string into date and time', () => {
    const dt = '2026-08-20T08:30:00';
    const formatted = formatDateTime(dt);
    expect(formatted).toContain('20/08/2026');
    expect(formatted).toContain('08:30');
  });

  it('timeAgo should calculate human readable relative time', () => {
    const now = new Date();
    expect(timeAgo(now.toISOString())).toContain('trước');
  });
});
