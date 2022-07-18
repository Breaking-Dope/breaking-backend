package com.dope.breaking.exception.like;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class AlreadyLikedException extends  BreakingException{

    public  AlreadyLikedException() { super (ErrorCode.ALREADY_LIKED, HttpStatus.BAD_REQUEST);}

}


