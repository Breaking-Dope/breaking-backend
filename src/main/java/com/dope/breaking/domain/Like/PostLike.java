package com.dope.breaking.domain.Like;


import com.dope.breaking.domain.Post;
import com.dope.breaking.domain.User;

import javax.persistence.*;

@Entity
public class PostLike {

    @Id @GeneratedValue
    @Column(name="POST_LIKE_ID")
    private Long id;

    @ManyToOne
    @JoinColumn (name="USER_ID")
    private User user;

    @ManyToOne
    @JoinColumn (name="POST_ID")
    private Post post;
}
