package com.dope.breaking.exception.post;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class AlreadyNotHiddenException extends BreakingException {

    public AlreadyNotHiddenException() { super(ErrorCode.ALREADY_NOT_HIDDEN, HttpStatus.BAD_REQUEST); }

}
