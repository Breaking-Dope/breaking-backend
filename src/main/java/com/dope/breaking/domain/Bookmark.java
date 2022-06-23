package com.dope.breaking.domain;

import javax.persistence.*;

@Entity
public class Bookmark {
    @Id @GeneratedValue
    @Column(name = "BOOKMARK_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "USER_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "POST_ID")
    private Post post;
}
