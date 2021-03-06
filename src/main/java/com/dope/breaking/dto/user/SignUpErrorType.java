package com.dope.breaking.dto.user;

public enum SignUpErrorType {

    INVALID_ROLE("invalid role"),
    INVALID_EMAIL("invalid email"),
    INVALID_PHONE_NUMBER("invalid phone number"),
    EMAIL_DUPLICATE("used email"),
    PHONE_NUMBER_DUPLICATE("used phone number"),
    NICKNAME_DUPLICATE("used nickname"),
    NOT_FOUND_USER("not found user"),
    NOT_REGISTERED_USER("not registered user");

    private String message;

    SignUpErrorType(String message) {

        this.message = message;

    }

    public String getMessage() {

        return message;

    }

}
