package com.dope.breaking.domain.post;

import lombok.*;

@Getter
@ToString
@RequiredArgsConstructor
public enum PostType {
    EXCLUSIVE("exclusive"),
    CHARGED("charged"),
    FREE("free");

    private final String title;
}
