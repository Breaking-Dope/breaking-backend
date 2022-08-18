package com.dope.breaking.dto.financial;

import com.dope.breaking.dto.post.WriterDto;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class TransactionInfoResponseDto {

    private Long cursorId;
    private LocalDateTime transactionDate;
    private String transactionType;
    private int amount;
    private int balance;

    private Long postId;
    private String postTitle;

    private WriterDto targetUser;

    @Builder
    @QueryProjection
    public TransactionInfoResponseDto(Long cursorId, LocalDateTime transactionDate, String transactionType, int amount, int balance){

        this.cursorId = cursorId;
        this.transactionDate = transactionDate;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balance = balance;

    }

}
