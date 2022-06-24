package com.dope.breaking.domain.hashtag;
import com.dope.breaking.domain.comment.Comment;
import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
public class CommentHashtag {

    @Id @GeneratedValue
    @Column(name =  "POST_HASHTAG_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "COMMENT_ID")
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "HASHTAG_ID")
    private Hashtag hashtag;

}
