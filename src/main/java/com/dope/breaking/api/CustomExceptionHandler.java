package com.dope.breaking.api;

import com.dope.breaking.dto.ErrorResponseDto;
import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.CustomInternalErrorException;
import com.dope.breaking.exception.ErrorCode;
import com.dope.breaking.exception.NotValidRequestBodyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(CustomInternalErrorException.class)
    protected ResponseEntity<ErrorResponseDto> handleCustomInternalErrorException(CustomInternalErrorException e) {

        log.error(e.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto(ErrorCode.INTERNAL_SERVER_ERROR));
    }


    @ExceptionHandler(BreakingException.class)
    protected ResponseEntity<ErrorResponseDto> handleBreakingCustomException(BreakingException e) {

        log.info(e.getMessage());
        return ResponseEntity
                .status(e.getStatus())
                .body(new ErrorResponseDto(e.getErrorCode()));
    }


    @ExceptionHandler(NotValidRequestBodyException.class)
    protected  ResponseEntity<ErrorResponseDto> handleNotValidRequestBody(NotValidRequestBodyException e){
        log.info(e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(new ErrorResponseDto(e.getErrorCode(), e.getMessage()));
    }


}
