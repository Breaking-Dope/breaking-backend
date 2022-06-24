package com.dope.breaking.domain.comment;


import com.dope.breaking.domain.user.User;
import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
public class CommentLike {

    @Id @GeneratedValue
    @Column(name="COMMENT_LIKE_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name="USER_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name="COMMENT_ID")
    private Comment comment;

}
