package com.ng_doanh.hr_management_system.payroll.mapper;

import com.ng_doanh.hr_management_system.payroll.dto.request.PayrollPeriodCreateRequest;
import com.ng_doanh.hr_management_system.payroll.dto.response.PayrollPeriodResponse;
import com.ng_doanh.hr_management_system.payroll.dto.response.PayslipResponse;
import com.ng_doanh.hr_management_system.payroll.entity.PayrollPeriod;
import com.ng_doanh.hr_management_system.payroll.entity.Payslip;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PayrollMapper {

    @Mapping(target = "status", ignore = true)
    PayrollPeriod toEntity(PayrollPeriodCreateRequest request);

    PayrollPeriodResponse toResponse(PayrollPeriod period);

    @Mapping(target = "payrollPeriodId", source = "payrollPeriod.id")
    @Mapping(target = "payrollPeriodName", source = "payrollPeriod.name")
    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeCode", source = "employee.employeeCode")
    @Mapping(target = "employeeName", expression = "java(payslip.getEmployee() != null ? payslip.getEmployee().getFirstName() + \" \" + payslip.getEmployee().getLastName() : null)")
    PayslipResponse toResponse(Payslip payslip);
}
