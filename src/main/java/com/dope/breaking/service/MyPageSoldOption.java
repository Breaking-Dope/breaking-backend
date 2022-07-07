package com.dope.breaking.service;

public enum MyPageSoldOption {

    ALL,
    SOLD,
    UNSOLD;

    public static MyPageSoldOption findMatchedEnum(String str) {
        for (MyPageSoldOption el : MyPageSoldOption.values()) {
            if (el.name().equalsIgnoreCase(str))
                return el;
        }
        return ALL;
    }
}
