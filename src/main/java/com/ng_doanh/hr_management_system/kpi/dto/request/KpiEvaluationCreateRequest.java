package com.ng_doanh.hr_management_system.kpi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiEvaluationCreateRequest {

    @NotNull(message = "ID nhân viên không được để trống")
    private Long employeeId;

    @NotNull(message = "Tháng đánh giá không được để trống")
    @Min(value = 1, message = "Tháng phải từ 1 đến 12")
    @Max(value = 12, message = "Tháng phải từ 1 đến 12")
    private Integer periodMonth;

    @NotNull(message = "Năm đánh giá không được để trống")
    @Min(value = 2000, message = "Năm không hợp lệ")
    private Integer periodYear;

    private String feedback;

    @NotEmpty(message = "Danh sách điểm chi tiết không được để trống")
    @Valid
    private List<KpiEvaluationDetailRequest> details;
}
