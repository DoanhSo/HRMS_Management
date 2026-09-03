package com.ng_doanh.hr_management_system.auth.repository;

import com.ng_doanh.hr_management_system.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN u.roles r WHERE " +
           "(:keyword IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:roleName IS NULL OR r.name = :roleName OR r.name = CONCAT('ROLE_', :roleName) OR CONCAT('ROLE_', r.name) = :roleName) AND " +
           "(:enabled IS NULL OR u.enabled = :enabled)")
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("roleName") String roleName,
            @Param("enabled") Boolean enabled,
            Pageable pageable
    );
}
