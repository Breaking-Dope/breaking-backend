package com.dope.breaking.domain.user;


import com.dope.breaking.domain.user.User;

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
