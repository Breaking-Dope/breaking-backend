package com.dope.breaking.domain.UserRelationship;



import com.dope.breaking.domain.User;

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

}
