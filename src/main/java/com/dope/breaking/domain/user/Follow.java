package com.dope.breaking.domain.user;



import com.dope.breaking.domain.user.User;

import javax.persistence.*;

@Entity
public class Follow {

    @Id
    @GeneratedValue
    @Column(name="FOLLOW_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "USER_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "FOLLOWING_USER_ID")
    private User following;

    public void updateUser(User user){
        this.user = user;
    }

    public void updateFollowing(User following){
        this.following = following;
    }

}
