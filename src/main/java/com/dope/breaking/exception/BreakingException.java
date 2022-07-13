package com.dope.breaking.exception;

import org.springframework.http.HttpStatus;

public class BreakingException extends RuntimeException {

    private ErrorCode errorCode;
    private final HttpStatus status;

    public BreakingException(final ErrorCode errorCode, final HttpStatus status) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

}
