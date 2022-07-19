package com.dope.breaking.exception.post;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class NoSuchPostException extends BreakingException {

    public NoSuchPostException() {
        super(ErrorCode.NO_SUCH_POST, HttpStatus.NOT_FOUND);
    }
}
