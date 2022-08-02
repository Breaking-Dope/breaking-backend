package com.dope.breaking.exception.bookmark;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class AlreadyBookmarkedException extends BreakingException {

    public AlreadyBookmarkedException(){
        super(ErrorCode.ALREADY_BOOKMARKED, HttpStatus.BAD_REQUEST);
    }
}
