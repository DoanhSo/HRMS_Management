package com.ng_doanh.hr_management_system.attendance.mapper;

import com.ng_doanh.hr_management_system.attendance.dto.response.AttendanceResponse;
import com.ng_doanh.hr_management_system.attendance.entity.Attendance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AttendanceMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeCode", source = "employee.employeeCode")
    @Mapping(target = "employeeName", expression = "java(attendance.getEmployee() != null ? attendance.getEmployee().getFirstName() + \" \" + attendance.getEmployee().getLastName() : null)")
    AttendanceResponse toResponse(Attendance attendance);
}
