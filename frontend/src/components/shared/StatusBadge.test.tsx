import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import React from 'react';
import { StatusBadge } from './StatusBadge';
import '@/i18n';

describe('StatusBadge Component', () => {
  it('renders ACTIVE status correctly', () => {
    render(<StatusBadge status="ACTIVE" />);
    expect(screen.getByText('Hoạt động')).toBeInTheDocument();
  });

  it('renders PROBATION status correctly', () => {
    render(<StatusBadge status="PROBATION" />);
    expect(screen.getByText('Thử việc')).toBeInTheDocument();
  });

  it('renders PENDING status correctly', () => {
    render(<StatusBadge status="PENDING" />);
    expect(screen.getByText('Chờ duyệt')).toBeInTheDocument();
  });

  it('renders fallback for unknown status', () => {
    render(<StatusBadge status="UNKNOWN_CODE" />);
    expect(screen.getByText('UNKNOWN_CODE')).toBeInTheDocument();
  });
});
