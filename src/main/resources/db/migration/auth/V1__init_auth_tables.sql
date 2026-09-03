-- =============================================================================
-- Flyway Migration: V1__init_auth_tables.sql
-- Module: Auth (Users, Roles, Permissions, UserRoles, RolePermissions, RefreshTokens)
-- =============================================================================

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    lock_time DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by BIGINT NULL,
    updated_by BIGINT NULL
);

CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    module VARCHAR(50) NOT NULL,
    description VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    expiry_date DATETIME NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- =============================================================================
-- Seed Initial Auth Data
-- Default password for 'admin': Admin@123 (BCrypt hash)
-- =============================================================================

INSERT INTO roles (id, name, description) VALUES
(1, 'ROLE_ADMIN', 'System Administrator with full access'),
(2, 'ROLE_HR', 'Human Resources Manager'),
(3, 'ROLE_MANAGER', 'Department Manager'),
(4, 'ROLE_EMPLOYEE', 'Standard Employee');

INSERT INTO permissions (id, name, module, description) VALUES
(1, 'EMPLOYEE_CREATE', 'EMPLOYEE', 'Create new employee'),
(2, 'EMPLOYEE_READ', 'EMPLOYEE', 'Read employee details'),
(3, 'EMPLOYEE_UPDATE', 'EMPLOYEE', 'Update employee details'),
(4, 'EMPLOYEE_DELETE', 'EMPLOYEE', 'Delete employee'),
(5, 'DEPARTMENT_MANAGE', 'DEPARTMENT', 'Manage departments'),
(6, 'POSITION_MANAGE', 'POSITION', 'Manage positions'),
(7, 'ATTENDANCE_MANAGE', 'ATTENDANCE', 'Manage attendances'),
(8, 'LEAVE_APPROVE', 'LEAVE', 'Approve or reject leave requests'),
(9, 'PAYROLL_MANAGE', 'PAYROLL', 'Manage payroll');

-- Admin gets all permissions
INSERT INTO role_permissions (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9),
(2, 1), (2, 2), (2, 3), (2, 5), (2, 6), (2, 7), (2, 8), (2, 9),
(3, 2), (3, 7), (3, 8),
(4, 2);

-- Default Admin Account: admin / Admin@123
INSERT INTO users (id, username, email, password, enabled, account_non_locked, failed_login_attempts) VALUES
(1, 'admin', 'admin@hrms.com', '$2a$10$O2WLrs1Exotc9JDIMTQMue7bEiRVvAJ4jz6Ix/d4D8B1aLIK8y5.i', TRUE, TRUE, 0);

INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);
