package com.ng_doanh.hr_management_system.kpi.dto.request;

import jakarta.validation.constraints.Max;
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
public class KpiCriteriaCreateRequest {

    @Size(max = 20, message = "Mã tiêu chí không được vượt quá 20 ký tự")
    private String code;

    @NotBlank(message = "Tên tiêu chí không được để trống")
    private String name;

    private Long departmentId;

    @NotNull(message = "Trọng số không được để trống")
    @Min(value = 1, message = "Trọng số phải từ 1% đến 100%")
    @Max(value = 100, message = "Trọng số phải từ 1% đến 100%")
    private Integer weight;

    private String targetDescription;

    @Builder.Default
    private Boolean active = true;
}
