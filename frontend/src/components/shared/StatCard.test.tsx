import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import React from 'react';
import { Users } from 'lucide-react';
import { StatCard } from './StatCard';

describe('StatCard Component', () => {
  it('renders title, value, and subtitle properly', () => {
    render(
      <StatCard
        title="Tổng Nhân Sự"
        value={150}
        subtitle="12 nhân viên thử việc"
        icon={Users}
      />
    );

    expect(screen.getByText('Tổng Nhân Sự')).toBeInTheDocument();
    expect(screen.getByText('150')).toBeInTheDocument();
    expect(screen.getByText('12 nhân viên thử việc')).toBeInTheDocument();
  });
});
