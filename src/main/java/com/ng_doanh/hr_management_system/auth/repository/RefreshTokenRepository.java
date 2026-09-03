package com.ng_doanh.hr_management_system.auth.repository;

import com.ng_doanh.hr_management_system.auth.entity.RefreshToken;
import com.ng_doanh.hr_management_system.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    int deleteByUser(User user);
}
