package com.ng_doanh.hr_management_system.common.config;

import com.ng_doanh.hr_management_system.common.constant.ApiPaths;
import com.ng_doanh.hr_management_system.common.constant.SecurityConstants;
import com.ng_doanh.hr_management_system.common.security.CustomAccessDeniedHandler;
import com.ng_doanh.hr_management_system.common.security.CustomAuthenticationEntryPoint;
import com.ng_doanh.hr_management_system.common.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        // 1. Public Endpoints
                        .requestMatchers(SecurityConstants.PUBLIC_URL_PATTERNS).permitAll()

                        // 2. Employee Module Path Restrictions
                        .requestMatchers(HttpMethod.POST, ApiPaths.EMPLOYEES_WILDCARD).hasAnyRole(SecurityConstants.ROLE_ADMIN, SecurityConstants.ROLE_HR)
                        .requestMatchers(HttpMethod.PUT, ApiPaths.EMPLOYEES_WILDCARD).hasAnyRole(SecurityConstants.ROLE_ADMIN, SecurityConstants.ROLE_HR)
                        .requestMatchers(HttpMethod.DELETE, ApiPaths.EMPLOYEES_WILDCARD).hasAnyRole(SecurityConstants.ROLE_ADMIN, SecurityConstants.ROLE_HR)
                        .requestMatchers(HttpMethod.GET, ApiPaths.EMPLOYEES_ME).authenticated()
                        .requestMatchers(HttpMethod.GET, ApiPaths.EMPLOYEES_WILDCARD).hasAnyRole(SecurityConstants.ROLE_ADMIN, SecurityConstants.ROLE_HR, SecurityConstants.ROLE_MANAGER)

                        // 3. Department Module Path Restrictions
                        .requestMatchers(HttpMethod.POST, ApiPaths.DEPARTMENTS_WILDCARD).hasAnyRole(SecurityConstants.ROLE_ADMIN, SecurityConstants.ROLE_HR)
                        .requestMatchers(HttpMethod.PUT, ApiPaths.DEPARTMENTS_WILDCARD).hasAnyRole(SecurityConstants.ROLE_ADMIN, SecurityConstants.ROLE_HR)
                        .requestMatchers(HttpMethod.DELETE, ApiPaths.DEPARTMENTS_WILDCARD).hasAnyRole(SecurityConstants.ROLE_ADMIN, SecurityConstants.ROLE_HR)
                        .requestMatchers(HttpMethod.GET, ApiPaths.DEPARTMENTS_WILDCARD).authenticated()

                        // 4. Position Module Path Restrictions
                        .requestMatchers(HttpMethod.POST, ApiPaths.POSITIONS_WILDCARD).hasAnyRole(SecurityConstants.ROLE_ADMIN, SecurityConstants.ROLE_HR)
                        .requestMatchers(HttpMethod.PUT, ApiPaths.POSITIONS_WILDCARD).hasAnyRole(SecurityConstants.ROLE_ADMIN, SecurityConstants.ROLE_HR)
                        .requestMatchers(HttpMethod.DELETE, ApiPaths.POSITIONS_WILDCARD).hasAnyRole(SecurityConstants.ROLE_ADMIN, SecurityConstants.ROLE_HR)
                        .requestMatchers(HttpMethod.GET, ApiPaths.POSITIONS_WILDCARD).authenticated()

                        // 5. Attendance Module Path Restrictions
                        .requestMatchers(ApiPaths.ATTENDANCES_CHECK_IN, ApiPaths.ATTENDANCES_CHECK_OUT, ApiPaths.ATTENDANCES_MY_HISTORY).authenticated()
                        .requestMatchers(ApiPaths.ATTENDANCES_WILDCARD).hasAnyRole(SecurityConstants.ROLE_ADMIN, SecurityConstants.ROLE_HR, SecurityConstants.ROLE_MANAGER)

                        // 6. Leave Module Path Restrictions
                        .requestMatchers(ApiPaths.LEAVES_MY_REQUESTS, ApiPaths.LEAVES_MY_BALANCES).authenticated()
                        .requestMatchers(HttpMethod.PUT, ApiPaths.LEAVES_APPROVE, ApiPaths.LEAVES_REJECT).hasAnyRole(SecurityConstants.ROLE_ADMIN, SecurityConstants.ROLE_HR, SecurityConstants.ROLE_MANAGER)
                        .requestMatchers(ApiPaths.LEAVES_WILDCARD).authenticated()

                        // 7. Payroll Module Path Restrictions
                        .requestMatchers(ApiPaths.PAYROLL_MY_RECORDS).authenticated()
                        .requestMatchers(ApiPaths.PAYROLL_WILDCARD).hasAnyRole(SecurityConstants.ROLE_ADMIN, SecurityConstants.ROLE_HR)

                        // 8. Dashboard Module Path Restrictions
                        .requestMatchers(ApiPaths.DASHBOARD_WILDCARD).hasAnyRole(SecurityConstants.ROLE_ADMIN, SecurityConstants.ROLE_HR, SecurityConstants.ROLE_MANAGER)

                        // 9. KPI Performance Module Path Restrictions
                        .requestMatchers(ApiPaths.KPI_MY_EVALUATIONS).authenticated()
                        .requestMatchers(ApiPaths.KPI_WILDCARD).hasAnyRole(SecurityConstants.ROLE_ADMIN, SecurityConstants.ROLE_HR, SecurityConstants.ROLE_MANAGER)

                        // 10. Salary Scales Module Path Restrictions
                        .requestMatchers(ApiPaths.SALARY_SCALES_WILDCARD).hasAnyRole(SecurityConstants.ROLE_ADMIN, SecurityConstants.ROLE_HR)

                        // 11. All other endpoints must be authenticated
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
