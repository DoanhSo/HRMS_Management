package com.ng_doanh.hr_management_system.employee.mapper;

import com.ng_doanh.hr_management_system.employee.dto.request.EmployeeCreateRequest;
import com.ng_doanh.hr_management_system.employee.dto.request.EmployeeUpdateRequest;
import com.ng_doanh.hr_management_system.employee.dto.response.EmployeeResponse;
import com.ng_doanh.hr_management_system.employee.entity.Employee;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmployeeMapper {

    @Mapping(target = "employeeCode", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "manager", ignore = true)
    Employee toEntity(EmployeeCreateRequest request);

    @Mapping(target = "fullName", expression = "java(employee.getFirstName() + \" \" + employee.getLastName())")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "managerId", source = "manager.id")
    @Mapping(target = "managerName", expression = "java(employee.getManager() != null ? employee.getManager().getFirstName() + \" \" + employee.getManager().getLastName() : null)")
    EmployeeResponse toResponse(Employee employee);

    @Mapping(target = "employeeCode", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "hireDate", ignore = true)
    @Mapping(target = "manager", ignore = true)
    void updateEntityFromRequest(EmployeeUpdateRequest request, @MappingTarget Employee employee);
}
