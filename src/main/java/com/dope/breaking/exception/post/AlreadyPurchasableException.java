package com.dope.breaking.exception.post;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class AlreadyPurchasableException extends BreakingException {

    public AlreadyPurchasableException() {super(ErrorCode.ALREADY_PURCHASABLE, HttpStatus.BAD_REQUEST);}

}

