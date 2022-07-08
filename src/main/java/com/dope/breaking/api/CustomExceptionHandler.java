package com.dope.breaking.api;

import com.dope.breaking.dto.ErrorResponseDto;
import com.dope.breaking.exception.BreakingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(BreakingException.class)
    protected ResponseEntity<ErrorResponseDto> handleCustomException(BreakingException e) {

        log.info(e.getMessage());
        return ResponseEntity
                .status(e.getStatus())
                .body(new ErrorResponseDto(e.getMessage()));
    }

}
