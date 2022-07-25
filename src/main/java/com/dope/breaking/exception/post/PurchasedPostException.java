package com.dope.breaking.exception.post;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class PurchasedPostException extends BreakingException {

    public PurchasedPostException(){
        super(ErrorCode.PURCHASED_POST, HttpStatus.NOT_ACCEPTABLE);

    }
}
