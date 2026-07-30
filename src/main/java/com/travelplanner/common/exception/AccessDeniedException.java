package com.travelplanner.common.exception;

/**
 * Exception dùng chung cho lỗi phân quyền (authorization) trong toàn hệ thống.
 * Được GlobalExceptionHandler bắt và trả về HTTP 403 Forbidden.
 *
 * KHÔNG dùng org.springframework.security.access.AccessDeniedException vì exception đó
 * bị Spring Security ExceptionTranslationFilter chặn trước khi đến @RestControllerAdvice,
 * dẫn đến response không theo format ApiResponse chuẩn.
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }

    public static AccessDeniedException of(String resourceName) {
        return new AccessDeniedException("Bạn không có quyền truy cập " + resourceName + " này.");
    }
}
