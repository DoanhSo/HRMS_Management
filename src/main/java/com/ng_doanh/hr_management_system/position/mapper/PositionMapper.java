package com.ng_doanh.hr_management_system.position.mapper;

import com.ng_doanh.hr_management_system.position.dto.request.PositionCreateRequest;
import com.ng_doanh.hr_management_system.position.dto.request.PositionUpdateRequest;
import com.ng_doanh.hr_management_system.position.dto.response.PositionResponse;
import com.ng_doanh.hr_management_system.position.entity.Position;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PositionMapper {

    @Mapping(target = "department", ignore = true)
    @Mapping(target = "active", ignore = true)
    Position toEntity(PositionCreateRequest request);

    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "employeeCount", ignore = true)
    PositionResponse toResponse(Position position);

    @Mapping(target = "code", ignore = true)
    @Mapping(target = "department", ignore = true)
    void updateEntityFromRequest(PositionUpdateRequest request, @MappingTarget Position position);
}
