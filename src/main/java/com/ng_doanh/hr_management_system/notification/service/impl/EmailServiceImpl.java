package com.ng_doanh.hr_management_system.notification.service.impl;

import com.ng_doanh.hr_management_system.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Async
    @Override
    public void sendLeaveRequestCreatedNotification(
            String toEmail,
            String employeeName,
            LocalDate startDate,
            LocalDate endDate,
            String reason
    ) {
        if (toEmail == null || toEmail.isBlank()) return;

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("[HRMS] New Leave Request Submitted by " + employeeName);
            message.setText(String.format(
                    "Hello,\n\nEmployee %s has submitted a leave request from %s to %s.\nReason: %s\n\nPlease log in to HRMS to review and approve/reject.\n\nBest regards,\nHRMS Team",
                    employeeName, startDate, endDate, reason != null ? reason : "N/A"
            ));

            mailSender.send(message);
            log.info("Leave request notification email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send leave request email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    @Override
    public void sendLeaveDecisionNotification(
            String toEmail,
            String employeeName,
            String decisionStatus,
            String approverName,
            String rejectionReason
    ) {
        if (toEmail == null || toEmail.isBlank()) return;

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("[HRMS] Your Leave Request has been " + decisionStatus);
            String content = String.format(
                    "Dear %s,\n\nYour leave request has been %s by %s.",
                    employeeName, decisionStatus, approverName != null ? approverName : "HR/Manager"
            );
            if (rejectionReason != null && !rejectionReason.isBlank()) {
                content += "\nReason for rejection: " + rejectionReason;
            }
            content += "\n\nBest regards,\nHRMS Team";

            message.setText(content);
            mailSender.send(message);
            log.info("Leave decision email ({}) sent to {}", decisionStatus, toEmail);
        } catch (Exception e) {
            log.error("Failed to send leave decision email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    @Override
    public void sendPayslipGeneratedNotification(
            String toEmail,
            String employeeName,
            String periodName,
            BigDecimal netSalary
    ) {
        if (toEmail == null || toEmail.isBlank()) return;

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("[HRMS] Your Monthly Payslip for " + periodName);
            message.setText(String.format(
                    "Dear %s,\n\nYour payslip for period %s is now available in the portal.\nNet Salary: %,.2f VND\n\nPlease log in to HRMS to download your detailed PDF payslip.\n\nBest regards,\nHRMS Payroll Department",
                    employeeName, periodName, netSalary
            ));

            mailSender.send(message);
            log.info("Payslip notification email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send payslip email to {}: {}", toEmail, e.getMessage());
        }
    }
}
