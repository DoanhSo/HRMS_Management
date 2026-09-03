package com.ng_doanh.hr_management_system.kpi.entity;

import com.ng_doanh.hr_management_system.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "kpi_evaluation_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KpiEvaluationDetail extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kpi_evaluation_id", nullable = false)
    private KpiEvaluation kpiEvaluation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kpi_criteria_id", nullable = false)
    private KpiCriteria kpiCriteria;

    @Column(nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal score = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Integer weight = 20;

    @Column(columnDefinition = "TEXT")
    private String comments;
}
