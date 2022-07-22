package com.dope.breaking.domain.comment;

import com.dope.breaking.domain.hashtag.PostCommentHashtag;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Comment {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name="COMMENT_ID")
    private Long id;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name="USER_ID")
    private User user;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name="POST_ID")
    private Post post;

    //댓글, 대댓글 self-referencing
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name="PARENT_ID")
    private Comment parent;

    @OneToMany(mappedBy = "parent")
    private List<Comment> children = new ArrayList<Comment>();

    @OneToMany(mappedBy = "comment", cascade = CascadeType.REMOVE)
    private List<PostCommentHashtag> postCommentHashtags;

    @OneToMany(mappedBy="comment")
    private List<CommentLike> commentLikeList = new ArrayList<CommentLike>();

    private String content;

    private LocalDateTime eventTime;

    @Builder
    public Comment(User user, Post post, String content){
        this.user = user;
        this.post = post;
        this.content = content;
    }

    @Builder
    public Comment(User user, Post post, Comment parent, String content){
        this.user = user;
        this.post = post;
        this.content = content;
        this.parent = parent;
    }

    public void updateComment(String content){

        this.content = content;

    }

}
