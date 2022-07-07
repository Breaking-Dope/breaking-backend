package com.dope.breaking.service;

public enum MyPagePostOption {

    WRITE,
    BUY,
    BOOKMARK;

    public static MyPagePostOption findMatchedEnum(String str) {
        for (MyPagePostOption el : MyPagePostOption.values()) {
            if (el.name().equalsIgnoreCase(str))
                return el;
        }
        return WRITE;
    }
}
