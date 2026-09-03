package com.ng_doanh.hr_management_system.position.repository;

import com.ng_doanh.hr_management_system.position.entity.Position;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

    Optional<Position> findByCode(String code);

    boolean existsByCode(String code);

    List<Position> findByDepartmentId(Long departmentId);

    List<Position> findByActiveTrue();

    @Query("SELECT p FROM Position p WHERE " +
           "(:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:departmentId IS NULL OR p.department.id = :departmentId) AND " +
           "(:active IS NULL OR p.active = :active)")
    Page<Position> searchPositions(
            @Param("keyword") String keyword,
            @Param("departmentId") Long departmentId,
            @Param("active") Boolean active,
            Pageable pageable
    );
}
