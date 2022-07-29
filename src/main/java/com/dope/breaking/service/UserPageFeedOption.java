package com.dope.breaking.service;

public enum UserPageFeedOption {

    WRITE,
    BUY,
    BOOKMARK;

    public static UserPageFeedOption findMatchedEnum(String str) {
        for (UserPageFeedOption el : UserPageFeedOption.values()) {
            if (el.name().equalsIgnoreCase(str))
                return el;
        }
        return WRITE;
    }
}
