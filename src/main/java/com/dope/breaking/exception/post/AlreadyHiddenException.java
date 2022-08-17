package com.dope.breaking.exception.post;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class AlreadyHiddenException extends BreakingException {

    public AlreadyHiddenException() {super(ErrorCode.ALREADY_HIDDEN, HttpStatus.BAD_REQUEST);}

}