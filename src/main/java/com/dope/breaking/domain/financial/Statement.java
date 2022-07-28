package com.dope.breaking.domain.financial;

import com.dope.breaking.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class Statement {

    @Id @GeneratedValue
    @Column(name="STATEMENT_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "USER_ID")
    private User user;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    private int amount;

    @Builder
    public Statement(User user, TransactionType transactionType, int amount){

        this.user = user;
        this.transactionType = transactionType;
        this.amount = amount;

    }

}
