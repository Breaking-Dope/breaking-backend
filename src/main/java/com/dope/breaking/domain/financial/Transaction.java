package com.dope.breaking.domain.financial;


import com.dope.breaking.domain.baseTimeEntity.BaseTimeEntity;
import com.dope.breaking.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Transaction extends BaseTimeEntity {

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

    private int balance;

    @CreatedDate
    private LocalDateTime transactionDate;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Builder
    public Transaction(User user, Statement statement, Purchase purchase, int amount, int balance, TransactionType transactionType){

        this.user = user;
        this.statement =  statement;
        this.purchase = purchase;
        this.amount = amount;
        this.balance = balance;
        this.transactionType = transactionType;

    }


}
