package com.ng_doanh.hr_management_system.leave.mapper;

import com.ng_doanh.hr_management_system.leave.dto.request.LeaveTypeCreateRequest;
import com.ng_doanh.hr_management_system.leave.dto.response.LeaveBalanceResponse;
import com.ng_doanh.hr_management_system.leave.dto.response.LeaveRequestResponse;
import com.ng_doanh.hr_management_system.leave.dto.response.LeaveTypeResponse;
import com.ng_doanh.hr_management_system.leave.entity.LeaveBalance;
import com.ng_doanh.hr_management_system.leave.entity.LeaveRequest;
import com.ng_doanh.hr_management_system.leave.entity.LeaveType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LeaveMapper {

    @Mapping(target = "active", ignore = true)
    LeaveType toEntity(LeaveTypeCreateRequest request);

    LeaveTypeResponse toResponse(LeaveType leaveType);

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", expression = "java(balance.getEmployee() != null ? balance.getEmployee().getFirstName() + \" \" + balance.getEmployee().getLastName() : null)")
    @Mapping(target = "leaveTypeId", source = "leaveType.id")
    @Mapping(target = "leaveTypeName", source = "leaveType.name")
    @Mapping(target = "leaveTypeCode", source = "leaveType.code")
    LeaveBalanceResponse toResponse(LeaveBalance balance);

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeCode", source = "employee.employeeCode")
    @Mapping(target = "employeeName", expression = "java(request.getEmployee() != null ? request.getEmployee().getFirstName() + \" \" + request.getEmployee().getLastName() : null)")
    @Mapping(target = "leaveTypeId", source = "leaveType.id")
    @Mapping(target = "leaveTypeName", source = "leaveType.name")
    @Mapping(target = "approverId", source = "approver.id")
    @Mapping(target = "approverName", expression = "java(request.getApprover() != null ? request.getApprover().getFirstName() + \" \" + request.getApprover().getLastName() : null)")
    LeaveRequestResponse toResponse(LeaveRequest request);
}
