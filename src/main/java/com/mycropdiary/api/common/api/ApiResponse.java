package com.mycropdiary.api.common.api;

import java.time.Instant;

public record ApiResponse<T>(
        boolean success,
        String messageCode,
        String message,
        T data,
        String requestId,
        Instant timestamp
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "SUCCESS", "Operation completed successfully", data, null, Instant.now());
    }

    public static <T> ApiResponse<T> success(String code, String message, T data) {
        return new ApiResponse<>(true, code, message, data, null, Instant.now());
    }

    public static ApiResponse<Void> error(String code, String message, String requestId) {
        return new ApiResponse<>(false, code, message, null, requestId, Instant.now());
    }
}
