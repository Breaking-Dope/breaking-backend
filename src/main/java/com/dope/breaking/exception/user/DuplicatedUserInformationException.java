package com.dope.breaking.exception.user;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.service.FailableUserInformation;
import org.springframework.http.HttpStatus;

/**
 * 휴대폰번호, 닉네임, 휴대폰 번호의 형식 오류에 의한 예외입니다.
 */
public class DuplicatedUserInformationException extends BreakingException {

    public DuplicatedUserInformationException(FailableUserInformation failableUserInformation) {
        super(failableUserInformation.getDuplicated_errorCode(), HttpStatus.BAD_REQUEST);
    }
}
