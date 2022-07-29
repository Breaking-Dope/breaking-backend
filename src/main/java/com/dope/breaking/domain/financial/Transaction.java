package com.dope.breaking.domain.financial;


import com.dope.breaking.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="TRANSACTION_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name="USER_ID")
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "STATEMENT_ID")
    private Statement statement;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "PURCHASE_ID")
    private Purchase purchase;

    private int amount;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Builder
    public Transaction(User user, Statement statement, Purchase purchase, int amount, TransactionType transactionType){

        this.user = user;
        this.statement =  statement;
        this.purchase = purchase;
        this.amount = amount;
        this.transactionType = transactionType;

    }


}
