package com.ng_doanh.hr_management_system.payroll.service;

import com.ng_doanh.hr_management_system.payroll.dto.request.PayrollPeriodCreateRequest;
import com.ng_doanh.hr_management_system.payroll.dto.response.PayrollPeriodResponse;
import com.ng_doanh.hr_management_system.payroll.dto.response.PayslipResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PayrollService {

    PayrollPeriodResponse createPayrollPeriod(PayrollPeriodCreateRequest request);

    Page<PayrollPeriodResponse> getAllPayrollPeriods(Pageable pageable);

    PayrollPeriodResponse getPayrollPeriodById(Long periodId);

    void calculatePayrollForPeriod(Long periodId);

    void approvePayrollPeriod(Long periodId);

    Page<PayslipResponse> getMyPayslips(Long userId, Pageable pageable);

    Page<PayslipResponse> searchPayslips(Long periodId, String keyword, Long departmentId, Pageable pageable);

    PayslipResponse getPayslipById(Long payslipId);
}
