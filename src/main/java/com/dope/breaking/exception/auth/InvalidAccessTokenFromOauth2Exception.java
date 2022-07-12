package com.dope.breaking.exception.auth;

import com.dope.breaking.exception.BreakingException;
import org.springframework.http.HttpStatus;

public class InvalidAccessTokenFromOauth2Exception extends BreakingException {

    private static final String MESSAGE = "유효하지 않은 Oauth2 AccessToken입니다." ;

    public InvalidAccessTokenFromOauth2Exception() {
        super(MESSAGE, HttpStatus.NOT_ACCEPTABLE);
    }
}

