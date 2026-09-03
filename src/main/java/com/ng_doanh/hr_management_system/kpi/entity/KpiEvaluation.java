package com.ng_doanh.hr_management_system.kpi.entity;

import com.ng_doanh.hr_management_system.common.entity.BaseEntity;
import com.ng_doanh.hr_management_system.employee.entity.Employee;
import com.ng_doanh.hr_management_system.kpi.enums.KpiEvaluationStatus;
import com.ng_doanh.hr_management_system.kpi.enums.KpiRating;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "kpi_evaluations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_emp_period_kpi", columnNames = {"employee_id", "period_year", "period_month"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KpiEvaluation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "period_month", nullable = false)
    private Integer periodMonth;

    @Column(name = "period_year", nullable = false)
    private Integer periodYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluator_id")
    private Employee evaluator;

    @Column(name = "total_score", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal totalScore = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private KpiRating rating = KpiRating.C;

    @Column(name = "kpi_coefficient", nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal kpiCoefficient = BigDecimal.ONE;

    @Column(name = "bonus_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal bonusAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private KpiEvaluationStatus status = KpiEvaluationStatus.DRAFT;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @OneToMany(mappedBy = "kpiEvaluation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<KpiEvaluationDetail> details = new ArrayList<>();
}
