package com.ng_doanh.hr_management_system.salary.mapper;

import com.ng_doanh.hr_management_system.salary.dto.request.SalaryScaleCreateRequest;
import com.ng_doanh.hr_management_system.salary.dto.response.SalaryScaleResponse;
import com.ng_doanh.hr_management_system.salary.entity.SalaryScale;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SalaryScaleMapper {

    @Mapping(target = "position", ignore = true)
    SalaryScale toEntity(SalaryScaleCreateRequest request);

    @Mapping(target = "positionId", source = "position.id")
    @Mapping(target = "positionTitle", source = "position.title")
    @Mapping(target = "calculatedSalary", expression = "java(entity.getBaseSalary() != null && entity.getCoefficient() != null ? entity.getBaseSalary().multiply(entity.getCoefficient()) : java.math.BigDecimal.ZERO)")
    SalaryScaleResponse toResponse(SalaryScale entity);
}
