package com.ng_doanh.hr_management_system.leave.entity;

import com.ng_doanh.hr_management_system.common.entity.BaseEntity;
import com.ng_doanh.hr_management_system.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "leave_balances", uniqueConstraints = {
        @UniqueConstraint(name = "uk_emp_leave_year", columnNames = {"employee_id", "leave_type_id", "year"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(nullable = false)
    private int year;

    @Column(name = "total_days", nullable = false, precision = 4, scale = 1)
    @Builder.Default
    private BigDecimal totalDays = BigDecimal.valueOf(12.0);

    @Column(name = "used_days", nullable = false, precision = 4, scale = 1)
    @Builder.Default
    private BigDecimal usedDays = BigDecimal.ZERO;

    @Column(name = "remaining_days", nullable = false, precision = 4, scale = 1)
    @Builder.Default
    private BigDecimal remainingDays = BigDecimal.valueOf(12.0);
}
