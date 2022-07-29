package com.dope.breaking.exception.financial;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public class NotEnoughBalanceException extends BreakingException {

    public NotEnoughBalanceException() { super (ErrorCode.NOT_ENOUGH_BALANCE, HttpStatus.BAD_REQUEST);}

}


