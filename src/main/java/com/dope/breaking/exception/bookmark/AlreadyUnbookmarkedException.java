package com.dope.breaking.exception.bookmark;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class AlreadyUnbookmarkedException extends BreakingException {

    public AlreadyUnbookmarkedException(){
        super(ErrorCode.ALREADY_UNBOOKMARKED, HttpStatus.BAD_REQUEST);
    }
}
