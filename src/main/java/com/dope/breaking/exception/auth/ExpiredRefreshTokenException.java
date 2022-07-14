package com.dope.breaking.exception.auth;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Refresh Token이 만료되었을 때 발생하는 예외입니다.
 */
public class ExpiredRefreshTokenException extends BreakingException {

    public ExpiredRefreshTokenException() {
        super(ErrorCode.EXPIRED_REFRESH_TOKEN, HttpStatus.UNAUTHORIZED);
    }

}
