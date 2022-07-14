package com.dope.breaking.exception.post;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * 포스트 작성이 서버 내부 문제로 실패했을때 발생하는 예외입니다.
 * PostAPI 리팩토링 하면서 같이 수정이 필요합니다. 좀 더 세부적으로 에러 코드를 작성해주세요. @마틴
 */
public class PostCreateException extends BreakingException {

    private static final String MESSAGE = "포스트 작성에 실패하였습니다." ;

    public PostCreateException() {
        super(ErrorCode.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
