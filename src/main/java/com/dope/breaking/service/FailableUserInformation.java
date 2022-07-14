package com.dope.breaking.service;

import com.dope.breaking.exception.CustomInternalErrorException;
import com.dope.breaking.exception.ErrorCode;

public enum FailableUserInformation {

    PHONENUMBER("휴대폰 번호", ErrorCode.INVALID_PHONE_NUMBER_FORMAT, ErrorCode.DUPLICATED_PHONE_NUMBER),
    EMAIL("이메일", ErrorCode.INVALID_EMAIL_FORMAT, ErrorCode.DUPLICATED_EMAIL),
    NICKNAME("닉네임", ErrorCode.INVALID_NICKNAME_FORMAT, ErrorCode.DUPLICATED_NICKNAME),
    ROLE("유저 구분", ErrorCode.INVALID_USER_ROLE, ErrorCode.BAD_REQUEST);

    private final String label;
    private final ErrorCode invalid_format_errorCode;
    private final ErrorCode duplicated_errorCode;

    FailableUserInformation(String label, ErrorCode invalid_format_errorCode, ErrorCode duplicated_errorCode) {

        this.label = label;
        this.invalid_format_errorCode = invalid_format_errorCode;
        this.duplicated_errorCode = duplicated_errorCode;
    }

    public String getLabel() {
        return label;
    }

    public ErrorCode getInvalid_format_errorCode() {
        return invalid_format_errorCode;
    }

    public ErrorCode getDuplicated_errorCode() {
        return duplicated_errorCode;
    }
}
