package com.dope.breaking.domain.post;

import lombok.*;

@Getter
@ToString
@RequiredArgsConstructor
public enum PostType {

    EXCLUSIVE("EXCLUSIVE"),
    CHARGED("CHARGED"),
    FREE("FREE"),
    MISSION("MISSION");

    private final String title;

    public static PostType findMatchedEnum(String str) {
        for (PostType el : PostType.values()) {
            if (el.name().equalsIgnoreCase(str))
                return el;
        }
        return null;
    }
}
