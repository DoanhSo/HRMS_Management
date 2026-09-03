package com.ng_doanh.hr_management_system.payroll.repository;

import com.ng_doanh.hr_management_system.payroll.entity.PayrollPeriod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayrollPeriodRepository extends JpaRepository<PayrollPeriod, Long> {

    Optional<PayrollPeriod> findByYearAndMonth(int year, int month);

    boolean existsByYearAndMonth(int year, int month);

    Page<PayrollPeriod> findAllByOrderByYearDescMonthDesc(Pageable pageable);
}
