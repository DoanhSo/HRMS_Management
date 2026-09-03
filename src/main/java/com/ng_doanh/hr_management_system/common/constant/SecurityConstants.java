package com.ng_doanh.hr_management_system.common.constant;

public final class SecurityConstants {

    private SecurityConstants() {
        // Private constructor to prevent instantiation
    }

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    // Public URL Patterns for SecurityConfig permitAll using ApiPaths
    public static final String[] PUBLIC_URL_PATTERNS = {
            ApiPaths.AUTH_LOGIN,
            ApiPaths.AUTH_REFRESH_TOKEN,
            ApiPaths.WS_ENDPOINT,
            ApiPaths.SWAGGER_DOCS,
            ApiPaths.SWAGGER_UI,
            ApiPaths.SWAGGER_UI_HTML,
            "/actuator/**"
    };

    // Role Names
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_HR = "HR";
    public static final String ROLE_MANAGER = "MANAGER";
    public static final String ROLE_EMPLOYEE = "EMPLOYEE";

    // PreAuthorize Expressions for Controllers
    public static final String HAS_ROLE_ADMIN = "hasRole('ADMIN')";
    public static final String HAS_ROLE_HR_OR_ADMIN = "hasRole('ADMIN') or hasRole('HR')";
    public static final String HAS_ROLE_MANAGER_OR_ABOVE = "hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER')";
    public static final String HAS_ANY_ROLE = "hasRole('ADMIN') or hasRole('HR') or hasRole('MANAGER') or hasRole('EMPLOYEE')";
}
