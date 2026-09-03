package com.ng_doanh.hr_management_system.auth.scheduler;

import com.ng_doanh.hr_management_system.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Runs every Sunday at 02:00 AM to clean up expired refresh tokens.
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting expired refresh token cleanup scheduler...");
        Instant now = Instant.now();
        refreshTokenRepository.findAll().stream()
                .filter(t -> t.getExpiryDate().isBefore(now) || t.isRevoked())
                .forEach(refreshTokenRepository::delete);
        log.info("Expired refresh token cleanup completed.");
    }
}
