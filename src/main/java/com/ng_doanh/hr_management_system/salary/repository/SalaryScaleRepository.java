package com.ng_doanh.hr_management_system.salary.repository;

import com.ng_doanh.hr_management_system.salary.entity.SalaryScale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalaryScaleRepository extends JpaRepository<SalaryScale, Long> {

    Optional<SalaryScale> findByCode(String code);

    boolean existsByCode(String code);

    List<SalaryScale> findByActiveTrue();

    @Query("SELECT s FROM SalaryScale s WHERE " +
           "(:keyword IS NULL OR LOWER(s.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:positionId IS NULL OR s.position.id = :positionId) AND " +
           "(:active IS NULL OR s.active = :active)")
    Page<SalaryScale> searchSalaryScales(
            @Param("keyword") String keyword,
            @Param("positionId") Long positionId,
            @Param("active") Boolean active,
            Pageable pageable
    );
}
