package com.dope.breaking.exception.auth;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Access Token이 만료되었을 때 발생하는 예외입니다.
 */
public class ExpiredAccessTokenException extends BreakingException {

    public ExpiredAccessTokenException() {
        super(ErrorCode.EXPIRED_ACCESS_TOKEN, HttpStatus.UNAUTHORIZED);
    }

}
