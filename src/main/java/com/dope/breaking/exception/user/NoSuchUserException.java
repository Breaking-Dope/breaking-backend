package com.dope.breaking.exception.user;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.exception.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * 입력으로 userId 를 받았을 때, 해당 유저가 조회되지 않을 때 발생하는 예외입니다.
 */
public class NoSuchUserException extends BreakingException {

    public NoSuchUserException() {
        super(ErrorCode.NO_SUCH_USER, HttpStatus.NOT_FOUND);
    }

}
