package com.dope.breaking.exception.follow;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class AlreadyUnfollowingException extends BreakingException {

    public AlreadyUnfollowingException() { super (ErrorCode.ALREADY_UNFOLLOWING, HttpStatus.BAD_REQUEST);}

}
