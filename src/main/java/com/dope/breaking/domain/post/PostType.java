package com.dope.breaking.domain.post;

import lombok.*;

@Getter
@ToString
@RequiredArgsConstructor
public enum PostType {
    EXCLUSIVE("EXCLUSIVE"),
    CHARGED("CHARGED"),
    FREE("FREE");

    private final String title;
}
