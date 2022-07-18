package com.dope.breaking.service;

public enum SoldOption {

    ALL,
    SOLD,
    UNSOLD;

    public static SoldOption findMatchedEnum(String str) {
        for (SoldOption el : SoldOption.values()) {
            if (el.name().equalsIgnoreCase(str))
                return el;
        }
        return ALL;
    }
}
