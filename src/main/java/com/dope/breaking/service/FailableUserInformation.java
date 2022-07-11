package com.dope.breaking.service;

public enum FailableUserInformation {

    PHONENUMBER("휴대폰 번호"),
    EMAIL("이메일"),
    NICKNAME("닉네임"),
    ROLE("유저 구분");

    private final String label;

    FailableUserInformation(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

}
