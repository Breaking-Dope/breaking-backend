package com.dope.breaking.exception.user;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.service.FailableUserInformation;
import org.springframework.http.HttpStatus;

/**
 * 휴대폰번호, 닉네임, 휴대폰 번호의 형식 오류에 의한 예외입니다.
 */
public class InvalidUserInformationFormatException extends BreakingException {

    public InvalidUserInformationFormatException(FailableUserInformation failableUserInformation) {
        super(failableUserInformation.getInvalid_format_errorCode(), HttpStatus.BAD_REQUEST);
    }
}
