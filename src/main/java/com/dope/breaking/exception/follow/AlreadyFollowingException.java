package com.dope.breaking.exception.follow;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class AlreadyFollowingException extends BreakingException{

    public AlreadyFollowingException() { super (ErrorCode.ALREADY_FOLLOWING, HttpStatus.BAD_REQUEST);}

}

