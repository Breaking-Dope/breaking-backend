package com.dope.breaking.service;

public enum SortFilter {
    LIKE,
    VIEW,
    CHRONOLOGICAL;

    public static SortFilter findMatchedEnum(String str) {
        for (SortFilter el : SortFilter.values()) {
            if (el.name().equalsIgnoreCase(str))
                return el;
        }
        return null;
    }
}
