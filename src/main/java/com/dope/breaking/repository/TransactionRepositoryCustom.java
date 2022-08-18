package com.dope.breaking.repository;

import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.financial.TransactionInfoResponseDto;

import java.util.List;

public interface TransactionRepositoryCustom {

    public List<TransactionInfoResponseDto> transactionList(User me, Long cursorId, int size);

}
