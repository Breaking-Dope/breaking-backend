package com.dope.breaking.domain.UserRelationship;


import com.dope.breaking.domain.User;

import javax.persistence.*;

@Entity
public class Block {
    @Id
    @GeneratedValue
    @Column(name="BLOCK_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "USER_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "BLOCKING_USER_ID")
    private User blocking;
}
