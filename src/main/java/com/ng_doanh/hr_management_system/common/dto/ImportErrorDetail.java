package com.ng_doanh.hr_management_system.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportErrorDetail {

    private int rowNumber;
    private String identifier;
    private String fieldName;
    private String errorMessage;
}
