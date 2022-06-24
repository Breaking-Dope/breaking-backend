package com.dope.breaking.domain.user;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.User;
import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
public class Bookmark {

    @Id @GeneratedValue
    @Column(name = "BOOKMARK_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "USER_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "POST_ID")
    private Post post;

}
