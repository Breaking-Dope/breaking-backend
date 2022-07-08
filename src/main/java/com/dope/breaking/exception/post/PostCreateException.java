package com.dope.breaking.exception.post;

import com.dope.breaking.exception.BreakingException;
import org.springframework.http.HttpStatus;

/**
 * 포스트 작성이 서버 내부 문제로 실패했을때 발생하는 예외입니다.
 */
public class PostCreateException extends BreakingException {

    private static final String MESSAGE = "포스트 작성에 실패하였습니다." ;

    public PostCreateException() {
        super(MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
