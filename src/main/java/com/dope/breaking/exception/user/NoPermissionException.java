package com.dope.breaking.exception.user;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class NoPermissionException extends BreakingException {

    public NoPermissionException(){
        super(ErrorCode.NO_PERMISSION, HttpStatus.NOT_ACCEPTABLE);
    }
}
