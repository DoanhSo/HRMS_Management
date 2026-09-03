package com.ng_doanh.hr_management_system.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultResponse {

    private int totalRows;
    private int successCount;
    private int failedCount;

    @Builder.Default
    private List<ImportErrorDetail> errors = new ArrayList<>();
}
