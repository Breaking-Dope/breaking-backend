package com.dope.breaking.dto.financial;

import com.dope.breaking.domain.financial.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponseDto {

    private LocalDateTime transactionTime;
    private String transactionType;
    private int amount;
    private int balance;

    @Builder
    public TransactionResponseDto (Transaction transaction){

        this.transactionTime = transaction.getTransactionTime();
        this.transactionType = transaction.getTransactionType().getTitle();
        this.amount = transaction.getAmount();
        this.balance = transaction.getBalance();

    }

}
