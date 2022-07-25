package com.dope.breaking.domain.hashtag;


import com.dope.breaking.domain.comment.Comment;
import com.dope.breaking.domain.post.Post;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Getter
public class PostCommentHashtag {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name =  "POST_HASHTAG_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "POST_ID")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "HASHTAG_ID")
    private Hashtag hashtag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMMENT_ID")
    private Comment comment;

    @Enumerated(EnumType.STRING)
    private HashtagType hashtagType;



    @Builder
    public PostCommentHashtag(Post post, Comment comment, HashtagType hashtagType, Hashtag hashtag){
        this.post = post;
        this.comment = comment;
        this.hashtagType = hashtagType;
        this.hashtag = hashtag;
    }
}
