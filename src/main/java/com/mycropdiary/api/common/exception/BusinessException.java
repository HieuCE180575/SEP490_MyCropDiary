package com.mycropdiary.api.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {
    private final String messageCode;
    private final HttpStatus status;

    public BusinessException(String messageCode, String message, HttpStatus status) {
        super(message);
        this.messageCode = messageCode;
        this.status = status;
    }
}
