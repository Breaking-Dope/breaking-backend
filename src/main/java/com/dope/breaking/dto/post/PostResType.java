package com.dope.breaking.dto.post;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PostErrorType {

    NOT_FOUND_USER("not found user"),
    NOT_REGISTERED_USER("not registered user"),
    POST_FAILED("posting failed");


    private final String message;


    public String getMessage() {

        return this.message;

    }
}
