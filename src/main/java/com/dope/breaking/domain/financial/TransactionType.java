package com.dope.breaking.domain.financial;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public enum TransactionType {
    DEPOSIT ("deposit"),
    WITHDRAW("withdraw"),
    BUY_POST("buy_post"),
    SELL_POST("sell_post");

    private final String title;
}
