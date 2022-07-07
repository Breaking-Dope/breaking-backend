package com.dope.breaking.domain.user;




import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="FOLLOW_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "FOLLOWING_USER_ID")
    private User following;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "FOLLOWED_USER_ID")
    private User followed;

    public void updateFollowing(User user){
        this.following = user;
    }

    public void updateFollowed(User following){
        this.followed = following;
    }

}
