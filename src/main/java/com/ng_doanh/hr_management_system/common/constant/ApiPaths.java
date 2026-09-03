package com.ng_doanh.hr_management_system.common.constant;

public final class ApiPaths {

    private ApiPaths() {
        // Private constructor
    }

    public static final String API_V1_PREFIX = "/api/v1";

    // Auth Module Paths
    public static final String AUTH_BASE = API_V1_PREFIX + "/auth";
    public static final String AUTH_LOGIN = AUTH_BASE + "/login";
    public static final String AUTH_REFRESH_TOKEN = AUTH_BASE + "/refresh-token";
    public static final String AUTH_LOGOUT = AUTH_BASE + "/logout";
    public static final String AUTH_CHANGE_PASSWORD = AUTH_BASE + "/change-password";
    public static final String AUTH_ME = AUTH_BASE + "/me";

    // User Management Paths
    public static final String USERS_BASE = API_V1_PREFIX + "/users";
    public static final String USERS_WILDCARD = USERS_BASE + "/**";

    // Employee Module Paths
    public static final String EMPLOYEES_BASE = API_V1_PREFIX + "/employees";
    public static final String EMPLOYEES_WILDCARD = EMPLOYEES_BASE + "/**";
    public static final String EMPLOYEES_ME = EMPLOYEES_BASE + "/me";

    // Department Module Paths
    public static final String DEPARTMENTS_BASE = API_V1_PREFIX + "/departments";
    public static final String DEPARTMENTS_WILDCARD = DEPARTMENTS_BASE + "/**";
    public static final String DEPARTMENTS_ACTIVE = DEPARTMENTS_BASE + "/active";

    // Position Module Paths
    public static final String POSITIONS_BASE = API_V1_PREFIX + "/positions";
    public static final String POSITIONS_WILDCARD = POSITIONS_BASE + "/**";

    // Attendance Module Paths
    public static final String ATTENDANCES_BASE = API_V1_PREFIX + "/attendances";
    public static final String ATTENDANCES_WILDCARD = ATTENDANCES_BASE + "/**";
    public static final String ATTENDANCES_CHECK_IN = ATTENDANCES_BASE + "/check-in";
    public static final String ATTENDANCES_CHECK_OUT = ATTENDANCES_BASE + "/check-out";
    public static final String ATTENDANCES_MY_HISTORY = ATTENDANCES_BASE + "/my-history";

    // Leave Module Paths
    public static final String LEAVES_BASE = API_V1_PREFIX + "/leaves";
    public static final String LEAVES_WILDCARD = LEAVES_BASE + "/**";
    public static final String LEAVES_MY_REQUESTS = LEAVES_BASE + "/requests/my";
    public static final String LEAVES_MY_BALANCES = LEAVES_BASE + "/balances/my";
    public static final String LEAVES_APPROVE = LEAVES_BASE + "/requests/*/approve";
    public static final String LEAVES_REJECT = LEAVES_BASE + "/requests/*/reject";

    // Payroll Module Paths
    public static final String PAYROLL_BASE = API_V1_PREFIX + "/payroll";
    public static final String PAYROLL_WILDCARD = PAYROLL_BASE + "/**";
    public static final String PAYROLL_MY_RECORDS = PAYROLL_BASE + "/my-records";

    // Dashboard Module Paths
    public static final String DASHBOARD_BASE = API_V1_PREFIX + "/dashboard";
    public static final String DASHBOARD_WILDCARD = DASHBOARD_BASE + "/**";

    // Notification Module Paths
    public static final String NOTIFICATIONS_BASE = API_V1_PREFIX + "/notifications";
    public static final String NOTIFICATIONS_WILDCARD = NOTIFICATIONS_BASE + "/**";

    // KPI Performance Module Paths
    public static final String KPI_BASE = API_V1_PREFIX + "/kpi";
    public static final String KPI_WILDCARD = KPI_BASE + "/**";
    public static final String KPI_MY_EVALUATIONS = KPI_BASE + "/evaluations/my";

    // Salary Scale Module Paths
    public static final String SALARY_SCALES_BASE = API_V1_PREFIX + "/salary-scales";
    public static final String SALARY_SCALES_WILDCARD = SALARY_SCALES_BASE + "/**";

    // WebSocket Endpoint
    public static final String WS_ENDPOINT = "/ws/**";

    // Swagger & OpenAPI Paths
    public static final String SWAGGER_DOCS = "/v3/api-docs/**";
    public static final String SWAGGER_UI = "/swagger-ui/**";
    public static final String SWAGGER_UI_HTML = "/swagger-ui.html";
}
