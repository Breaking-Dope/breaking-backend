package com.dope.breaking.domain.Financials;

import com.dope.breaking.domain.Post;
import com.dope.breaking.domain.User;

import javax.persistence.*;

@Entity
public class Purchase {

    @Id @GeneratedValue
    @Column (name = "PURCHASE_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "USER_ID")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "POST_ID")
    private Post post;
}
