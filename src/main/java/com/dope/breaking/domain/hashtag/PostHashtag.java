package com.dope.breaking.domain.hashtag;


import com.dope.breaking.domain.post.Post;
import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
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
