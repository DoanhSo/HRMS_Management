package com.ng_doanh.hr_management_system.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ng_doanh.hr_management_system.common.enums.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private int code;
    private String message;
    private boolean success;
    private T data;
    private LocalDateTime timestamp;
    private String path;

    public static <T> ApiResponse<T> success(ResponseCode responseCode, String path) {
        return success(responseCode, null, path);
    }

    public static <T> ApiResponse<T> success(ResponseCode responseCode, T data, String path) {
        return ApiResponse.<T>builder()
                .code(responseCode.getCode())
                .message(responseCode.getMessage())
                .success(true)
                .data(data)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }

    public static <T> ApiResponse<T> error(ResponseCode responseCode, String path) {
        return error(responseCode, null, path);
    }

    public static <T> ApiResponse<T> error(ResponseCode responseCode, T data, String path) {
        return ApiResponse.<T>builder()
                .code(responseCode.getCode())
                .message(responseCode.getMessage())
                .success(false)
                .data(data)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }
}
