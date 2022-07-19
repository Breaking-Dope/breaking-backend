package com.dope.breaking.exception;

import org.springframework.http.HttpStatus;

public class NotValidRequestBodyException extends RuntimeException{

    private final String errorCode = "BSE403";

    private final HttpStatus status = HttpStatus.BAD_REQUEST;


    public NotValidRequestBodyException(String nullField){
        super("요청 body에 [" + nullField + "]가 누락되었습니다.");
    }

    public HttpStatus getStatus(){
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
