package com.dope.breaking.domain.post;


import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.User;
import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
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
