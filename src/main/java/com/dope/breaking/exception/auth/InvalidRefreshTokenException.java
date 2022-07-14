package com.dope.breaking.exception.auth;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Refresh Token이 올바르지 않을 때 발생하는 예외입니다.
 * 1. Refresh Token이 디코딩이 안될때.
 */
public class InvalidRefreshTokenException extends BreakingException {

    public InvalidRefreshTokenException() {
        super(ErrorCode.INVALID_REFRESH_TOKEN, HttpStatus.UNAUTHORIZED);
    }

}
