package com.dope.breaking.exception.auth;

import com.dope.breaking.exception.BreakingException;
import org.springframework.http.HttpStatus;

/**
 * Access Token이 올바르지 않을 때 발생하는 예외입니다.
 * 1. oauth2.0 에서 발급된 username이 데이터베이스에 없을 때.
 * 2. Access Token이 디코딩이 안될때.
 */
public class InvalidAccessTokenException extends BreakingException {

    public InvalidAccessTokenException() {
        super(ErrorCode.INVALID_ACCESS_TOKEN, HttpStatus.UNAUTHORIZED);
    }

}
