package com.dope.breaking.exception.post;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class SoldExclusivePostException extends BreakingException {

    public SoldExclusivePostException() { super(ErrorCode.SOLD_EXCLUSIVE_POST, HttpStatus.BAD_REQUEST); }

}


