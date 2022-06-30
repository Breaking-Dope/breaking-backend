package com.dope.breaking.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
public enum Role {


    PRESS("ROLE_PRESS", "언론사"),
    USER("ROLE_USER", "사용자");

    private final String key;
    private final String value;

}
