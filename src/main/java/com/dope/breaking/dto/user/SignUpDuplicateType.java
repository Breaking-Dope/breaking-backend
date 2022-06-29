package com.dope.breaking.dto.user;

public enum SignUpDuplicateType {

    EMAIL_DUPLICATE("used email"),
    PHONE_NUMBER_DUPLICATE("used phone number"),
    NICKNAME_DUPLICATE("used nickname");

    private String message;

    SignUpDuplicateType(String message) {

        this.message = message;

    }

    public String getMessage() {

        return message;

    }

}
