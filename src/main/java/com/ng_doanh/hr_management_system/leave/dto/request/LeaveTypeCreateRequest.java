package com.ng_doanh.hr_management_system.leave.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveTypeCreateRequest {

    @NotBlank(message = "Leave type name is required")
    private String name;

    @Size(max = 20, message = "Leave type code must not exceed 20 characters")
    private String code;

    private String description;

    @NotNull(message = "Paid status is required")
    private Boolean paid;

    @Min(value = 0, message = "Default days per year must be non-negative")
    private int defaultDaysPerYear;
}
