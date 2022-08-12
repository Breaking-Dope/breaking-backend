package com.dope.breaking.exception.pagination;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidCursorException extends BreakingException {

    public InvalidCursorException() {super(ErrorCode.INVALID_CURSOR,HttpStatus.BAD_REQUEST);}

}
