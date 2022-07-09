package com.dope.breaking.service;

public enum DuplicableUserInformation {

    PHONENUMBER("휴대폰 번호"),
    EMAIL("이메일"),
    NICKNAME("닉네임");

    private final String label;

    DuplicableUserInformation(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

}
