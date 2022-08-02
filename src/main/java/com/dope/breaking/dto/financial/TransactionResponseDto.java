package com.dope.breaking.dto.financial;

import lombok.AllArgsConstructor;
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

}
