package com.dope.breaking.exception.user;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class LoginRequireException extends BreakingException {

    public LoginRequireException(){
        super(ErrorCode.REQUIRE_LOGIN, HttpStatus.NOT_ACCEPTABLE);
    }
}
