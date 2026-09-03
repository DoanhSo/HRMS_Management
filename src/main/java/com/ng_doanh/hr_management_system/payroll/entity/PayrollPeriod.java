package com.ng_doanh.hr_management_system.payroll.entity;

import com.ng_doanh.hr_management_system.common.entity.BaseEntity;
import com.ng_doanh.hr_management_system.payroll.enums.PayrollPeriodStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "payroll_periods", uniqueConstraints = {
        @UniqueConstraint(name = "uk_payroll_year_month", columnNames = {"year", "month"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayrollPeriod extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int year;

    @Column(nullable = false)
    private int month;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "working_days", nullable = false)
    @Builder.Default
    private int workingDays = 26;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PayrollPeriodStatus status = PayrollPeriodStatus.DRAFT;
}
