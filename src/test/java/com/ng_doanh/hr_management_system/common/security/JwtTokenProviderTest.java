package com.ng_doanh.hr_management_system.common.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtTokenProvider Unit Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecret("9a2f8c4e1b7d6a5e3f2c1b4a6d8e0f2a4b6c8d0e2f4a6b8c0d2e4f6a8b0c2d4e");
        jwtProperties.setAccessTokenExpirationMs(3600000L); // 1 hour
        jwtProperties.setRefreshTokenExpirationMs(604800000L); // 7 days

        jwtTokenProvider = new JwtTokenProvider(jwtProperties);
    }

    @Test
    @DisplayName("Should generate valid JWT access token from Authentication")
    void shouldGenerateAccessTokenFromAuthentication() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "john_doe",
                "password123",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        String token = jwtTokenProvider.generateAccessToken(auth);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo("john_doe");
    }

    @Test
    @DisplayName("Should generate valid JWT access token from username and authorities")
    void shouldGenerateAccessTokenFromUsername() {
        String token = jwtTokenProvider.generateAccessTokenFromUsername("jane_doe", "ROLE_HR,ROLE_EMPLOYEE");

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo("jane_doe");
    }

    @Test
    @DisplayName("Should return false for malformed or invalid JWT token")
    void shouldReturnFalseForInvalidToken() {
        boolean isValid = jwtTokenProvider.validateToken("invalid.token.string");
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should return false for expired token")
    void shouldReturnFalseForExpiredToken() {
        // configure provider with 0ms expiration
        JwtProperties expiredProps = new JwtProperties();
        expiredProps.setSecret("9a2f8c4e1b7d6a5e3f2c1b4a6d8e0f2a4b6c8d0e2f4a6b8c0d2e4f6a8b0c2d4e");
        expiredProps.setAccessTokenExpirationMs(-1000L); // already expired
        JwtTokenProvider expiredProvider = new JwtTokenProvider(expiredProps);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "john_doe", "password", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        String expiredToken = expiredProvider.generateAccessToken(auth);

        assertThat(jwtTokenProvider.validateToken(expiredToken)).isFalse();
    }

    @Test
    @DisplayName("Print BCrypt hash for Admin@123")
    void printAdminHash() {
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        System.out.println("GENERATED_BCRYPT_HASH=" + encoder.encode("Admin@123"));
    }
}
