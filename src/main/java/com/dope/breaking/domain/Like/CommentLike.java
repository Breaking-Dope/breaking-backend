package com.dope.breaking.domain.Like;


import com.dope.breaking.domain.Comment;
import com.dope.breaking.domain.User;

import javax.persistence.*;

@Entity
public class CommentLike {

    @Id
    @GeneratedValue
    @Column(name="COMMENT_LIKE_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name="USER_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name="COMMENT_ID")
    private Comment comment;
}
