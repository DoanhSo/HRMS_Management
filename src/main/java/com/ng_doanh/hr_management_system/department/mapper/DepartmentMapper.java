package com.ng_doanh.hr_management_system.department.mapper;

import com.ng_doanh.hr_management_system.department.dto.request.DepartmentCreateRequest;
import com.ng_doanh.hr_management_system.department.dto.request.DepartmentUpdateRequest;
import com.ng_doanh.hr_management_system.department.dto.response.DepartmentResponse;
import com.ng_doanh.hr_management_system.department.entity.Department;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DepartmentMapper {

    @Mapping(target = "manager", ignore = true)
    @Mapping(target = "parentDepartment", ignore = true)
    @Mapping(target = "subDepartments", ignore = true)
    @Mapping(target = "active", ignore = true)
    Department toEntity(DepartmentCreateRequest request);

    @Mapping(target = "managerId", source = "manager.id")
    @Mapping(target = "managerName", expression = "java(department.getManager() != null ? department.getManager().getFirstName() + \" \" + department.getManager().getLastName() : null)")
    @Mapping(target = "parentDepartmentId", source = "parentDepartment.id")
    @Mapping(target = "parentDepartmentName", source = "parentDepartment.name")
    @Mapping(target = "employeeCount", ignore = true)
    DepartmentResponse toResponse(Department department);

    @Mapping(target = "code", ignore = true)
    @Mapping(target = "manager", ignore = true)
    @Mapping(target = "parentDepartment", ignore = true)
    @Mapping(target = "subDepartments", ignore = true)
    void updateEntityFromRequest(DepartmentUpdateRequest request, @MappingTarget Department department);
}
