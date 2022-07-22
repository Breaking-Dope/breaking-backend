package com.dope.breaking.exception.comment;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class NoSuchCommentException extends BreakingException {

    public NoSuchCommentException() { super (ErrorCode.NO_SUCH_COMMENT, HttpStatus.NOT_FOUND);}

}
