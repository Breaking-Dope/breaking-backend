package com.dope.breaking.exception.post;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class NotPurchasablePostException extends BreakingException {

    public NotPurchasablePostException() { super(ErrorCode.NOT_PURCHASABLE_POST, HttpStatus.BAD_REQUEST); }

}



