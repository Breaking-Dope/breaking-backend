package com.dope.breaking.exception.auth;

import com.dope.breaking.exception.BreakingException;
import com.dope.breaking.service.DuplicableUserInformation;
import org.springframework.http.HttpStatus;

/**
 * 휴대폰번호, 닉네임, 휴대폰 번호의 형식 오류에 의한 예외입니다.
 */
public class invalidUserInformationFormatException extends BreakingException {

    private static String createMessage(DuplicableUserInformation duplicableUserInformation) {
        switch (duplicableUserInformation) {
            case PHONENUMBER:
                return "올바른 전화번호 형식이 아닙니다.";
            case EMAIL:
                return "올바른 이메일 형식이 아닙니다.";
            case NICKNAME:
                return "2~10자의 영문, 한글, 숫자만 입력해주시기 바랍니다.";
            default:
                return "잘못된 형식입니다.";
        }

    }

    public invalidUserInformationFormatException(DuplicableUserInformation duplicableUserInformation) {
        super(createMessage(duplicableUserInformation), HttpStatus.BAD_REQUEST);
    }
}
