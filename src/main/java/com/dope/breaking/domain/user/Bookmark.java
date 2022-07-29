package com.dope.breaking.domain.user;

import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
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


    @Builder
    public Bookmark(User user, Post post){
        this.post = post;
        this.user = user;

    }


}
