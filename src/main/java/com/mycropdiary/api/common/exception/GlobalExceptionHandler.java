package com.mycropdiary.api.common.exception;

import com.mycropdiary.api.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex, HttpServletRequest request) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.error(ex.getMessageCode(), ex.getMessage(), request.getHeader("X-Request-Id")));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Map<String, String>>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, "VALIDATION_ERROR",
                "Submitted data is invalid", errors, null, java.time.Instant.now()));
    }

    @ExceptionHandler({ConstraintViolationException.class, HandlerMethodValidationException.class})
    ResponseEntity<ApiResponse<Void>> handleParameterValidation(Exception ex, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(ApiResponse.error(
                "VALIDATION_ERROR",
                "Request parameters are invalid",
                request.getHeader("X-Request-Id")
        ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                          HttpServletRequest request) {
        String message = "Invalid value for parameter: " + ex.getName();
        return ResponseEntity.badRequest().body(ApiResponse.error(
                "INVALID_PARAMETER",
                message,
                request.getHeader("X-Request-Id")
        ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiResponse<Void>> handleUnreadableBody(HttpMessageNotReadableException ex,
                                                            HttpServletRequest request) {
        return ResponseEntity.badRequest().body(ApiResponse.error(
                "INVALID_REQUEST_BODY",
                "Request body is malformed or contains an unsupported value",
                request.getHeader("X-Request-Id")
        ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiResponse<Void>> handleDataIntegrity(DataIntegrityViolationException ex,
                                                           HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(
                "DATA_CONFLICT",
                "The request conflicts with existing or referenced data",
                request.getHeader("X-Request-Id")
        ));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception ex, HttpServletRequest request) {
        String detailMessage = ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred";
        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            detailMessage += " -> " + ex.getCause().getMessage();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", detailMessage, request.getHeader("X-Request-Id")));
    }
}
