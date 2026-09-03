package com.ng_doanh.hr_management_system.leave.entity;

import com.ng_doanh.hr_management_system.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "leave_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveType extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    private boolean paid = true;

    @Column(name = "default_days_per_year", nullable = false)
    @Builder.Default
    private int defaultDaysPerYear = 12;

    @Builder.Default
    private boolean active = true;
}
