package com.dope.breaking.exception.auth;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class AlreadyLoginException extends BreakingException {

    public AlreadyLoginException(){
        super(ErrorCode.ALREADY_LOGIN,  HttpStatus.BAD_REQUEST);
    }
}
