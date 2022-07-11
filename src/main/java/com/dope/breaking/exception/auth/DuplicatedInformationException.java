package com.dope.breaking.exception.auth;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.service.FailableUserInformation;
import org.springframework.http.HttpStatus;

/**
 * 휴대폰번호, 닉네임, 휴대폰 번호 등의 중복에 의한 예외입니다.
 */
public class DuplicatedInformationException extends BreakingException {

    private static String createMessage(FailableUserInformation failableUserInformation) {
        return "이미 사용중인" + failableUserInformation.toString() + "입니다.";

    }

    public DuplicatedInformationException(FailableUserInformation failableUserInformation) {
        super(createMessage(failableUserInformation), HttpStatus.BAD_REQUEST);
    }


}
