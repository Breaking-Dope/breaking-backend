package com.dope.breaking.service;

public enum SortStrategy {
    LIKE,
    VIEW,
    CHRONOLOGICAL;

    public static SortStrategy findMatchedEnum(String str) {
        for (SortStrategy el : SortStrategy.values()) {
            if (el.name().equalsIgnoreCase(str))
                return el;
        }
        return CHRONOLOGICAL;
    }

}
