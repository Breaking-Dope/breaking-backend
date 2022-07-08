package com.dope.breaking.dto.post;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PostResType {

    NOT_FOUND_USER("not found user"),
    NOT_REGISTERED_USER("not registered user"),
    POST_FAILED("posting failed"),

    MODIFY_FAILED("modification failed"),

    NO_PERMISSION("no permission to modify");

    private final String message;

    public String getMessage() {

        return this.message;

    }

}
