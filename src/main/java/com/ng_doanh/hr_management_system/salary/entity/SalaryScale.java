package com.ng_doanh.hr_management_system.salary.entity;

import com.ng_doanh.hr_management_system.common.entity.BaseEntity;
import com.ng_doanh.hr_management_system.position.entity.Position;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "salary_scales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalaryScale extends BaseEntity {

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    @Column(nullable = false, precision = 4, scale = 2)
    @Builder.Default
    private BigDecimal coefficient = BigDecimal.ONE;

    @Column(name = "base_salary", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal baseSalary = BigDecimal.valueOf(5000000);

    @Column(name = "standard_bonus", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal standardBonus = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
