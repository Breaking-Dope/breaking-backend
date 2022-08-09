package com.dope.breaking.exception.auth;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class NotFoundUserAgent extends BreakingException {

    public NotFoundUserAgent(){
        super(ErrorCode.NOT_FOUND_USER_AGENT, HttpStatus.BAD_REQUEST);
    }
}
