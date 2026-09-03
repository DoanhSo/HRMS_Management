package com.ng_doanh.hr_management_system.notification.service;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface EmailService {

    void sendLeaveRequestCreatedNotification(
            String toEmail,
            String employeeName,
            LocalDate startDate,
            LocalDate endDate,
            String reason
    );

    void sendLeaveDecisionNotification(
            String toEmail,
            String employeeName,
            String decisionStatus,
            String approverName,
            String rejectionReason
    );

    void sendPayslipGeneratedNotification(
            String toEmail,
            String employeeName,
            String periodName,
            BigDecimal netSalary
    );
}
