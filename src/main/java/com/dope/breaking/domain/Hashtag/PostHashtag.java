package com.dope.breaking.domain.Hashtag;


import com.dope.breaking.domain.Post;

import javax.persistence.*;

@Entity
public class PostHashtag {
    @Id @GeneratedValue
    @Column(name =  "POST_HASHTAG_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "POST_ID")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "HASHTAG_ID")
    private Hashtag hashtag;
}
