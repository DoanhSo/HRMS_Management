package com.ng_doanh.hr_management_system.kpi.repository;

import com.ng_doanh.hr_management_system.kpi.entity.KpiCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KpiCriteriaRepository extends JpaRepository<KpiCriteria, Long> {

    Optional<KpiCriteria> findByCode(String code);

    boolean existsByCode(String code);

    List<KpiCriteria> findByActiveTrue();

    @Query("SELECT c FROM KpiCriteria c WHERE " +
           "(:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:departmentId IS NULL OR c.department.id = :departmentId OR c.department IS NULL) AND " +
           "(:active IS NULL OR c.active = :active)")
    Page<KpiCriteria> searchCriteria(
            @Param("keyword") String keyword,
            @Param("departmentId") Long departmentId,
            @Param("active") Boolean active,
            Pageable pageable
    );
}
