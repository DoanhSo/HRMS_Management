package com.ng_doanh.hr_management_system.kpi.entity;

import com.ng_doanh.hr_management_system.common.entity.BaseEntity;
import com.ng_doanh.hr_management_system.department.entity.Department;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "kpi_criteria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KpiCriteria extends BaseEntity {

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(nullable = false)
    @Builder.Default
    private Integer weight = 20;

    @Column(name = "target_description", columnDefinition = "TEXT")
    private String targetDescription;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
}
