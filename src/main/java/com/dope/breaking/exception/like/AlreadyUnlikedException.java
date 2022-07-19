package com.dope.breaking.exception.like;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class AlreadyUnlikedException extends BreakingException {

    public  AlreadyUnlikedException() { super (ErrorCode.ALREADY_UNLIKED, HttpStatus.BAD_REQUEST);}

}
