package com.ng_doanh.hr_management_system.common.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ResponseCode {
    // Success Codes (1000 - 1999)
    SUCCESS(1000, "Success", HttpStatus.OK),
    CREATED(1001, "Resource created successfully", HttpStatus.CREATED),
    UPDATED(1002, "Resource updated successfully", HttpStatus.OK),
    DELETED(1003, "Resource deleted successfully", HttpStatus.OK),

    // Client Input & Validation Errors (2000 - 2999)
    VALIDATION_ERROR(2001, "Validation failed", HttpStatus.BAD_REQUEST),
    RESOURCE_NOT_FOUND(2002, "Resource not found", HttpStatus.NOT_FOUND),
    DUPLICATE_RESOURCE(2003, "Resource already exists", HttpStatus.CONFLICT),
    BAD_REQUEST(2004, "Bad request", HttpStatus.BAD_REQUEST),

    // Auth & Security Errors (3000 - 3999)
    UNAUTHORIZED(3001, "Unauthorized access", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(3002, "Access denied", HttpStatus.FORBIDDEN),
    TOKEN_EXPIRED(3003, "Token has expired", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(3004, "Invalid token", HttpStatus.UNAUTHORIZED),
    BAD_CREDENTIALS(3005, "Invalid username or password", HttpStatus.UNAUTHORIZED),
    ACCOUNT_LOCKED(3006, "Account is locked", HttpStatus.FORBIDDEN),
    ACCOUNT_DISABLED(3007, "Account is disabled", HttpStatus.FORBIDDEN),

    // Server Errors (4000 - 4999)
    INTERNAL_SERVER_ERROR(4001, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE(4002, "Service unavailable", HttpStatus.SERVICE_UNAVAILABLE);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ResponseCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
