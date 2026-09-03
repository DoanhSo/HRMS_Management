package com.ng_doanh.hr_management_system.report.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import com.ng_doanh.hr_management_system.common.exception.BusinessException;
import com.ng_doanh.hr_management_system.payroll.entity.Payslip;
import com.ng_doanh.hr_management_system.payroll.repository.PayslipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class PdfExportService {

    private final PayslipRepository payslipRepository;

    public byte[] generatePayslipPdf(Long payslipId) {
        Payslip payslip = payslipRepository.findById(payslipId)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.DARK_GRAY);
            Paragraph title = new Paragraph("HR MANAGEMENT SYSTEM - SALARY PAYSLIP", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Subtitle
            Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.GRAY);
            Paragraph period = new Paragraph("Period: " + (payslip.getPayrollPeriod() != null ? payslip.getPayrollPeriod().getName() : "N/A"), subFont);
            period.setAlignment(Element.ALIGN_CENTER);
            period.setSpacingAfter(20);
            document.add(period);

            // Employee Info Table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(20);

            addTableCell(infoTable, "Employee Code:", true);
            addTableCell(infoTable, payslip.getEmployee() != null ? payslip.getEmployee().getEmployeeCode() : "N/A", false);

            addTableCell(infoTable, "Employee Name:", true);
            addTableCell(infoTable, payslip.getEmployee() != null ? payslip.getEmployee().getFirstName() + " " + payslip.getEmployee().getLastName() : "N/A", false);

            document.add(infoTable);

            // Salary Details Table
            PdfPTable salaryTable = new PdfPTable(2);
            salaryTable.setWidthPercentage(100);
            salaryTable.setSpacingAfter(30);

            addSalaryRow(salaryTable, "Basic Salary", String.format("%,.2f VND", payslip.getBasicSalary()));
            addSalaryRow(salaryTable, "Actual Work Days", String.valueOf(payslip.getActualWorkDays()));
            addSalaryRow(salaryTable, "Allowances (+)", String.format("%,.2f VND", payslip.getAllowances()));
            addSalaryRow(salaryTable, "Gross Salary", String.format("%,.2f VND", payslip.getGrossSalary()));
            addSalaryRow(salaryTable, "Personal Income Tax (-)", String.format("%,.2f VND", payslip.getTax()));
            addSalaryRow(salaryTable, "Other Deductions (-)", String.format("%,.2f VND", payslip.getDeductions()));

            // Net Salary Highlight
            Font netFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLUE);
            PdfPCell netLabel = new PdfPCell(new Phrase("NET SALARY (TAKE HOME):", netFont));
            netLabel.setPadding(10);
            netLabel.setBackgroundColor(new Color(230, 240, 255));
            salaryTable.addCell(netLabel);

            PdfPCell netValue = new PdfPCell(new Phrase(String.format("%,.2f VND", payslip.getNetSalary()), netFont));
            netValue.setPadding(10);
            netValue.setBackgroundColor(new Color(230, 240, 255));
            netValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
            salaryTable.addCell(netValue);

            document.add(salaryTable);

            // Footer note
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.GRAY);
            Paragraph footer = new Paragraph("This is an electronically generated payslip and requires no signature.", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new BusinessException(ResponseCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void addTableCell(PdfPTable table, String text, boolean isBold) {
        Font font = isBold ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11) : FontFactory.getFont(FontFactory.HELVETICA, 11);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addSalaryRow(PdfPTable table, String label, String value) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 11);
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setPadding(8);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setPadding(8);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }
}
