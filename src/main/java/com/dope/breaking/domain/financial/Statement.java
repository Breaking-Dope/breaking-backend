package com.dope.breaking.domain.financial;

import com.dope.breaking.domain.user.User;
import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
public class Statement {

    @Id @GeneratedValue
    @Column(name="STATEMENT_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "USER_ID")
    private User user;

    private int amount;

}
