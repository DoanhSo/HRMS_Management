package com.ng_doanh.hr_management_system.salary.service.impl;

import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import com.ng_doanh.hr_management_system.common.exception.BusinessException;
import com.ng_doanh.hr_management_system.common.util.CodeGeneratorUtil;
import com.ng_doanh.hr_management_system.position.entity.Position;
import com.ng_doanh.hr_management_system.position.repository.PositionRepository;
import com.ng_doanh.hr_management_system.salary.dto.request.SalaryScaleCreateRequest;
import com.ng_doanh.hr_management_system.salary.dto.request.SalaryScaleUpdateRequest;
import com.ng_doanh.hr_management_system.salary.dto.response.SalaryScaleResponse;
import com.ng_doanh.hr_management_system.salary.entity.SalaryScale;
import com.ng_doanh.hr_management_system.salary.mapper.SalaryScaleMapper;
import com.ng_doanh.hr_management_system.salary.repository.SalaryScaleRepository;
import com.ng_doanh.hr_management_system.salary.service.SalaryScaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalaryScaleServiceImpl implements SalaryScaleService {

    private final SalaryScaleRepository salaryScaleRepository;
    private final PositionRepository positionRepository;
    private final SalaryScaleMapper salaryScaleMapper;

    @Override
    @Transactional
    public SalaryScaleResponse createSalaryScale(SalaryScaleCreateRequest request) {
        String code;
        if (request.getCode() == null || request.getCode().isBlank()) {
            code = CodeGeneratorUtil.generateCode("SS-", 5, salaryScaleRepository.count(), salaryScaleRepository::existsByCode);
        } else {
            code = request.getCode().trim();
            if (salaryScaleRepository.existsByCode(code)) {
                throw new BusinessException(ResponseCode.DUPLICATE_RESOURCE);
            }
        }

        SalaryScale scale = salaryScaleMapper.toEntity(request);
        scale.setCode(code);
        if (request.getPositionId() != null) {
            Position position = positionRepository.findById(request.getPositionId())
                    .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));
            scale.setPosition(position);
        }

        SalaryScale saved = salaryScaleRepository.save(scale);
        log.info("Salary scale created: {} ({}) with coefficient {}", saved.getTitle(), saved.getCode(), saved.getCoefficient());
        return salaryScaleMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public SalaryScaleResponse updateSalaryScale(Long id, SalaryScaleUpdateRequest request) {
        SalaryScale scale = salaryScaleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        if (request.getCode() != null && !request.getCode().isBlank()) {
            String newCode = request.getCode().trim();
            if (!newCode.equalsIgnoreCase(scale.getCode())) {
                if (salaryScaleRepository.existsByCode(newCode)) {
                    throw new BusinessException(ResponseCode.DUPLICATE_RESOURCE);
                }
                scale.setCode(newCode);
            }
        }

        scale.setTitle(request.getTitle());
        scale.setCoefficient(request.getCoefficient());
        scale.setBaseSalary(request.getBaseSalary());
        scale.setStandardBonus(request.getStandardBonus());
        scale.setActive(request.getActive());

        if (request.getPositionId() != null) {
            Position position = positionRepository.findById(request.getPositionId())
                    .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));
            scale.setPosition(position);
        } else {
            scale.setPosition(null);
        }

        SalaryScale updated = salaryScaleRepository.save(scale);
        return salaryScaleMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteSalaryScale(Long id) {
        if (!salaryScaleRepository.existsById(id)) {
            throw new BusinessException(ResponseCode.RESOURCE_NOT_FOUND);
        }
        salaryScaleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public SalaryScaleResponse getSalaryScaleById(Long id) {
        SalaryScale scale = salaryScaleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));
        return salaryScaleMapper.toResponse(scale);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalaryScaleResponse> getAllActiveSalaryScales() {
        return salaryScaleRepository.findByActiveTrue().stream()
                .map(salaryScaleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SalaryScaleResponse> searchSalaryScales(String keyword, Long positionId, Boolean active, Pageable pageable) {
        return salaryScaleRepository.searchSalaryScales(keyword, positionId, active, pageable)
                .map(salaryScaleMapper::toResponse);
    }
}
