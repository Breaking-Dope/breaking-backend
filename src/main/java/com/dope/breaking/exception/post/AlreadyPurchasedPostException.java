package com.dope.breaking.exception.post;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class AlreadyPurchasedPostException extends BreakingException {

    public AlreadyPurchasedPostException() { super(ErrorCode.ALREADY_PURCHASED_POST, HttpStatus.BAD_REQUEST); }

}