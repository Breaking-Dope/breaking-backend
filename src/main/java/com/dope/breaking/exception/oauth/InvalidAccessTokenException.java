package com.dope.breaking.exception.oauth;

import com.dope.breaking.exception.BreakingException;
import org.springframework.http.HttpStatus;

/**
 * oauth2.0 에서 발급된 username이 데이터베이스에 없을 때 발생하는 예외입니다.
 */
public class InvalidAccessTokenException extends BreakingException {

    private static final String MESSAGE = "유저 인증에 실패하였습니다." ;

    public InvalidAccessTokenException() {
        super(MESSAGE, HttpStatus.UNAUTHORIZED);
    }

}