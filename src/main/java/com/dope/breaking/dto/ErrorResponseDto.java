package com.dope.breaking.dto;

import com.dope.breaking.exception.ErrorCode;
import lombok.Data;

@Data
public class ErrorResponseDto {

    private String code;

    private String message;

    public ErrorResponseDto(ErrorCode code) {
        this.code = code.getCode();
        this.message = code.getMessage();
    }

    public ErrorResponseDto(String errorCode, String message){
        this.code = errorCode;
        this.message = message;
    }
}
