package com.dope.breaking.exception.post;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class AlreadyNotPurchasableException extends BreakingException {

    public AlreadyNotPurchasableException() {super(ErrorCode.ALREADY_NOT_PURCHASABLE, HttpStatus.BAD_REQUEST);}

}
