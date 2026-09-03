package com.ng_doanh.hr_management_system.leave.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveTypeResponse {

    private Long id;
    private String name;
    private String code;
    private String description;
    private boolean paid;
    private int defaultDaysPerYear;
    private boolean active;
    private LocalDateTime createdAt;
}
