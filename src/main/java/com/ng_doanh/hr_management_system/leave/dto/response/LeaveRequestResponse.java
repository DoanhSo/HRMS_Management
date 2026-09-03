package com.ng_doanh.hr_management_system.leave.dto.response;

import com.ng_doanh.hr_management_system.leave.enums.LeaveRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestResponse {

    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private Long leaveTypeId;
    private String leaveTypeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalDays;
    private String reason;
    private LeaveRequestStatus status;
    private Long approverId;
    private String approverName;
    private String rejectionReason;
    private LocalDateTime createdAt;
}
