package com.ng_doanh.hr_management_system.common.exception;

import com.ng_doanh.hr_management_system.common.dto.ApiResponse;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import jakarta.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        log.warn("Business Exception: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        ResponseCode responseCode = ex.getResponseCode();
        return ResponseEntity.status(responseCode.getHttpStatus())
                .body(ApiResponse.error(responseCode, request.getRequestURI()));
    }

    @ExceptionHandler({UsernameNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(Exception ex, HttpServletRequest request) {
        log.warn("Bad Credentials Exception: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(ResponseCode.BAD_CREDENTIALS.getHttpStatus())
                .body(ApiResponse.error(ResponseCode.BAD_CREDENTIALS, request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("Validation Error on path: {}", request.getRequestURI());
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.status(ResponseCode.VALIDATION_ERROR.getHttpStatus())
                .body(ApiResponse.error(ResponseCode.VALIDATION_ERROR, errors, request.getRequestURI()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        log.warn("Authentication Exception: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(ResponseCode.UNAUTHORIZED.getHttpStatus())
                .body(ApiResponse.error(ResponseCode.UNAUTHORIZED, request.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access Denied: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(ResponseCode.FORBIDDEN.getHttpStatus())
                .body(ApiResponse.error(ResponseCode.FORBIDDEN, request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled Exception on path {}: ", request.getRequestURI(), ex);
        return ResponseEntity.status(ResponseCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ApiResponse.error(ResponseCode.INTERNAL_SERVER_ERROR, request.getRequestURI()));
    }
}
