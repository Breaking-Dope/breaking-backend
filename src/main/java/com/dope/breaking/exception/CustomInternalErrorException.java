package com.dope.breaking.exception;

/**
 * 서버 내부 에러가 발생했을 때 발생하는 예외.
 * 응답으로는, 디테일한 정보를 넘기지 않습니다.
 */
public class CustomInternalErrorException extends RuntimeException {

    public CustomInternalErrorException(String message) {
        super(message);
    }

}
