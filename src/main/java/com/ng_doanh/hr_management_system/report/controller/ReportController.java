package com.ng_doanh.hr_management_system.report.controller;

import com.ng_doanh.hr_management_system.common.constant.SecurityConstants;
import com.ng_doanh.hr_management_system.report.service.ExcelExportService;
import com.ng_doanh.hr_management_system.report.service.PdfExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Reports & Exports", description = "APIs for downloading Excel attendance sheets and PDF salary payslips")
public class ReportController {

    private final ExcelExportService excelExportService;
    private final PdfExportService pdfExportService;

    @GetMapping("/attendance/excel")
    @PreAuthorize(SecurityConstants.HAS_ROLE_MANAGER_OR_ABOVE)
    @Operation(summary = "Export attendance report to Excel", description = "Generates and downloads attendance report in .xlsx format")
    public ResponseEntity<byte[]> exportAttendanceExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long departmentId
    ) throws IOException {
        LocalDate start = startDate != null ? startDate : LocalDate.now().withDayOfMonth(1);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        byte[] excelBytes = excelExportService.exportMonthlyAttendanceToExcel(start, end, departmentId);

        String filename = String.format("Attendance_Report_%s_to_%s.xlsx", start, end);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }

    @GetMapping("/payslips/{id}/pdf")
    @Operation(summary = "Download payslip as PDF", description = "Generates and downloads employee salary payslip in .pdf format")
    public ResponseEntity<byte[]> exportPayslipPdf(@PathVariable Long id) {
        byte[] pdfBytes = pdfExportService.generatePayslipPdf(id);

        String filename = String.format("Payslip_%d.pdf", id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
