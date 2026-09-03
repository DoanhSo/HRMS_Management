package com.ng_doanh.hr_management_system.auth.service;

import com.ng_doanh.hr_management_system.auth.dto.request.ChangePasswordRequest;
import com.ng_doanh.hr_management_system.auth.dto.request.LoginRequest;
import com.ng_doanh.hr_management_system.auth.dto.request.RefreshTokenRequest;
import com.ng_doanh.hr_management_system.auth.dto.response.TokenResponse;
import com.ng_doanh.hr_management_system.auth.dto.response.UserResponse;
import com.ng_doanh.hr_management_system.auth.entity.RefreshToken;
import com.ng_doanh.hr_management_system.auth.entity.Role;
import com.ng_doanh.hr_management_system.auth.entity.User;
import com.ng_doanh.hr_management_system.auth.mapper.AuthMapper;
import com.ng_doanh.hr_management_system.auth.repository.RefreshTokenRepository;
import com.ng_doanh.hr_management_system.auth.repository.UserRepository;
import com.ng_doanh.hr_management_system.auth.service.impl.AuthServiceImpl;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import com.ng_doanh.hr_management_system.common.exception.BusinessException;
import com.ng_doanh.hr_management_system.common.security.CustomUserDetails;
import com.ng_doanh.hr_management_system.common.security.JwtProperties;
import com.ng_doanh.hr_management_system.common.security.JwtTokenProvider;
import com.ng_doanh.hr_management_system.common.security.RedisTokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private RedisTokenBlacklistService redisTokenBlacklistService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private Role role;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        role = Role.builder()
                .name("ROLE_ADMIN")
                .description("Administrator")
                .build();
        role.setId(1L);

        user = User.builder()
                .username("admin")
                .email("admin@example.com")
                .password("$2a$10$encodedpassword")
                .enabled(true)
                .roles(Set.of(role))
                .build();
        user.setId(1L);

        refreshToken = RefreshToken.builder()
                .token("refresh-token-uuid-123")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(604800))
                .revoked(false)
                .build();
        refreshToken.setId(1L);
    }

    // ===================================================
    // LOGIN TESTS
    // ===================================================

    @Test
    @DisplayName("Login successfully returns TokenResponse with access and refresh tokens")
    void login_Success() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("password123");

        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateAccessToken(authentication)).thenReturn("access-token-123");
        when(jwtProperties.getRefreshTokenExpirationMs()).thenReturn(604800000L);
        when(jwtProperties.getAccessTokenExpirationMs()).thenReturn(3600000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        TokenResponse result = authService.login(loginRequest);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("access-token-123");
        assertThat(result.getRefreshToken()).isNotBlank();
        assertThat(result.getTokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("Login with invalid credentials throws BadCredentialsException")
    void login_BadCredentials_ThrowsException() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }

    // ===================================================
    // REFRESH TOKEN TESTS
    // ===================================================

    @Test
    @DisplayName("Refresh token successfully generates new access token")
    void refreshToken_Success() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token-uuid-123");

        when(refreshTokenRepository.findByToken("refresh-token-uuid-123")).thenReturn(Optional.of(refreshToken));
        when(jwtTokenProvider.generateAccessTokenFromUsername(eq("admin"), anyString())).thenReturn("new-access-token-456");
        when(jwtProperties.getAccessTokenExpirationMs()).thenReturn(3600000L);

        TokenResponse result = authService.refreshToken(request);

        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isEqualTo("new-access-token-456");
        assertThat(result.getRefreshToken()).isEqualTo("refresh-token-uuid-123");
    }

    @Test
    @DisplayName("Refresh token with revoked token throws INVALID_TOKEN")
    void refreshToken_Revoked_ThrowsException() {
        RefreshToken revokedToken = RefreshToken.builder()
                .token("revoked-token-xyz")
                .user(user)
                .expiryDate(Instant.now().plusSeconds(604800))
                .revoked(true) // already revoked
                .build();

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("revoked-token-xyz");

        when(refreshTokenRepository.findByToken("revoked-token-xyz")).thenReturn(Optional.of(revokedToken));

        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("responseCode", ResponseCode.INVALID_TOKEN);
    }

    @Test
    @DisplayName("Refresh token with expired token throws TOKEN_EXPIRED")
    void refreshToken_Expired_ThrowsException() {
        RefreshToken expiredToken = RefreshToken.builder()
                .token("expired-token-abc")
                .user(user)
                .expiryDate(Instant.now().minusSeconds(3600)) // expired 1 hour ago
                .revoked(false)
                .build();

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("expired-token-abc");

        when(refreshTokenRepository.findByToken("expired-token-abc")).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> authService.refreshToken(request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("responseCode", ResponseCode.TOKEN_EXPIRED);

        verify(refreshTokenRepository).delete(expiredToken);
    }

    // ===================================================
    // LOGOUT TESTS
    // ===================================================

    @Test
    @DisplayName("Logout successfully revokes refresh token in database")
    void logout_Success() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token-uuid-123");

        when(refreshTokenRepository.findByToken("refresh-token-uuid-123")).thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);

        authService.logout(request);

        assertThat(refreshToken.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(refreshToken);
    }

    @Test
    @DisplayName("Logout with unknown token does nothing (idempotent)")
    void logout_UnknownToken_DoesNothing() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("unknown-token");

        when(refreshTokenRepository.findByToken("unknown-token")).thenReturn(Optional.empty());

        authService.logout(request);

        verify(refreshTokenRepository, never()).save(any());
    }

    // ===================================================
    // CHANGE PASSWORD TESTS
    // ===================================================

    @Test
    @DisplayName("Change password successfully updates encoded password and revokes all refresh tokens")
    void changePassword_Success() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPassword123");
        request.setNewPassword("newPassword456!");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPassword123", user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPassword456!")).thenReturn("$2a$10$newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        authService.changePassword(1L, request);

        assertThat(user.getPassword()).isEqualTo("$2a$10$newEncodedPassword");
        verify(userRepository).save(user);
        verify(refreshTokenRepository).deleteByUser(user);
    }

    @Test
    @DisplayName("Change password with wrong old password throws BAD_CREDENTIALS")
    void changePassword_WrongOldPassword_ThrowsException() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongOldPassword");
        request.setNewPassword("newPassword456!");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOldPassword", user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.changePassword(1L, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("responseCode", ResponseCode.BAD_CREDENTIALS);

        verify(userRepository, never()).save(any());
    }

    // ===================================================
    // GET CURRENT USER TESTS
    // ===================================================

    @Test
    @DisplayName("Get current user returns UserResponse for valid username")
    void getCurrentUser_Success() {
        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .username("admin")
                .email("admin@example.com")
                .enabled(true)
                .build();

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(authMapper.toUserResponse(user)).thenReturn(userResponse);

        UserResponse result = authService.getCurrentUser("admin");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("admin");
        assertThat(result.getEmail()).isEqualTo("admin@example.com");
    }

    @Test
    @DisplayName("Get current user with non-existent username throws RESOURCE_NOT_FOUND")
    void getCurrentUser_NotFound_ThrowsException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.getCurrentUser("ghost"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("responseCode", ResponseCode.RESOURCE_NOT_FOUND);
    }
}
