package com.ng_doanh.hr_management_system.auth.service.impl;

import com.ng_doanh.hr_management_system.auth.dto.request.ChangePasswordRequest;
import com.ng_doanh.hr_management_system.auth.dto.request.LoginRequest;
import com.ng_doanh.hr_management_system.auth.dto.request.RefreshTokenRequest;
import com.ng_doanh.hr_management_system.auth.dto.response.TokenResponse;
import com.ng_doanh.hr_management_system.auth.dto.response.UserResponse;
import com.ng_doanh.hr_management_system.auth.entity.RefreshToken;
import com.ng_doanh.hr_management_system.auth.entity.User;
import com.ng_doanh.hr_management_system.auth.mapper.AuthMapper;
import com.ng_doanh.hr_management_system.auth.repository.RefreshTokenRepository;
import com.ng_doanh.hr_management_system.auth.repository.UserRepository;
import com.ng_doanh.hr_management_system.auth.service.AuthService;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import com.ng_doanh.hr_management_system.common.exception.BusinessException;
import com.ng_doanh.hr_management_system.common.security.CustomUserDetails;
import com.ng_doanh.hr_management_system.common.security.JwtProperties;
import com.ng_doanh.hr_management_system.common.security.JwtTokenProvider;
import com.ng_doanh.hr_management_system.common.security.RedisTokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final RedisTokenBlacklistService redisTokenBlacklistService;

    @Override
    @Transactional
    public TokenResponse login(LoginRequest request) {
        log.info("Attempting login for username: {}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        // 1. Generate Access Token
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);

        // 2. Generate Refresh Token
        RefreshToken refreshToken = createRefreshToken(user);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpirationMs())
                .build();
    }

    @Override
    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new BusinessException(ResponseCode.INVALID_TOKEN));

        if (refreshToken.isRevoked()) {
            throw new BusinessException(ResponseCode.INVALID_TOKEN);
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException(ResponseCode.TOKEN_EXPIRED);
        }

        User user = refreshToken.getUser();

        // Get Authorities (Roles + Permissions)
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // Generate New Access Token
        String newAccessToken = jwtTokenProvider.generateAccessTokenFromUsername(user.getUsername(), authorities);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpirationMs())
                .build();
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        String tokenStr = request.getRefreshToken();

        refreshTokenRepository.findByToken(tokenStr).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            log.info("Refresh token revoked for user: {}", refreshToken.getUser().getUsername());
        });
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResponseCode.BAD_CREDENTIALS);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Revoke all existing refresh tokens for security
        refreshTokenRepository.deleteByUser(user);
        log.info("Password changed successfully for user ID: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ResponseCode.RESOURCE_NOT_FOUND));

        return authMapper.toUserResponse(user);
    }

    private RefreshToken createRefreshToken(User user) {
        // Delete old refresh tokens for this user
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(jwtProperties.getRefreshTokenExpirationMs()))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }
}
