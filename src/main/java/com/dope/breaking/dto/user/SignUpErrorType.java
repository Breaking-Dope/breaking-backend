package com.dope.breaking.dto.user;

public enum SignUpErrorType {

    INVALID_ROLE("invalid role"),
    INVALID_EMAIL("invalid email"),
    EMAIL_DUPLICATE("used email"),
    PHONE_NUMBER_DUPLICATE("used phone number"),
    NICKNAME_DUPLICATE("used nickname");


    private String message;

    SignUpErrorType(String message) {

        this.message = message;

    }

    public String getMessage() {

        return message;

    }

}
