package com.dope.breaking.exception.post;

import com.dope.breaking.exception.BreakingException;
import org.springframework.http.HttpStatus;

public class PostCreateException extends BreakingException {

    private static final String MESSAGE = "유저 인증에 실패하였습니다." ;

    public PostCreateException() {
        super(MESSAGE, HttpStatus.UNAUTHORIZED); //적절한 에러 코드 사용 강조
    }
}
