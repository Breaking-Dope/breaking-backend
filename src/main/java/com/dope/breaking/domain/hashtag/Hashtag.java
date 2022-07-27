package com.dope.breaking.domain.hashtag;


import com.dope.breaking.domain.comment.Comment;
import com.dope.breaking.domain.post.Post;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Getter
public class Hashtag {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name =  "POST_HASHTAG_ID")
    private Long id;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "POST_ID")
    private Post post;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMMENT_ID")
    private Comment comment;

    @Enumerated(EnumType.STRING)
    private HashtagType hashtagType;

    private String content;

    @Builder
    public Hashtag(Post post, Comment comment, HashtagType hashtagType, String content){

        this.post = post;
        this.comment = comment;
        this.hashtagType = hashtagType;
        this.content = content;

    }

}
