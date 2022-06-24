package com.dope.breaking.domain.comment;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.User;
import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Comment {

    @Id @GeneratedValue
    @Column (name="COMMENT_ID")
    private Long id;

    //내가 단 댓글은 알 필요가 없으니, 일단 단방향으로 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name="USER_ID")
    private User user;

    //제보 하나에 무엇이 필요한지는 알 필요가 있으니 양방향 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name="POST_ID")
    private Post post;

    //댓글, 대댓글 self-referencing
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name="PARENT_ID")
    private Comment parent;

    @OneToMany(mappedBy = "parent")
    private List<Comment> children = new ArrayList<Comment>();


    @OneToMany(mappedBy="comment")
    private List<CommentLike> commentLikeList = new ArrayList<CommentLike>();

    private String content;

    private LocalDateTime eventTime;

}
