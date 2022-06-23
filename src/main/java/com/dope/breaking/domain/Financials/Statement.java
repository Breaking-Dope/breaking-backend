package com.dope.breaking.domain.Financials;

import com.dope.breaking.domain.User;

import javax.persistence.*;

@Entity
public class Statement {
    @Id
    @GeneratedValue
    @Column(name="STATEMENT_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "USER_ID")
    private User user;

    private int amount;
}
