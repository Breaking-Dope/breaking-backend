package com.dope.breaking.domain.Hashtag;
import com.dope.breaking.domain.Comment;

import javax.persistence.*;

public class CommentHashtag {
    @Id
    @GeneratedValue
    @Column(name =  "POST_HASHTAG_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "COMMENT_ID")
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "HASHTAG_ID")
    private Hashtag hashtag;
}
