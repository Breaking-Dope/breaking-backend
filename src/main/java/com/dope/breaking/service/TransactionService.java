package com.dope.breaking.service;

import com.dope.breaking.domain.financial.Purchase;
import com.dope.breaking.domain.financial.Statement;
import com.dope.breaking.domain.financial.Transaction;
import com.dope.breaking.domain.financial.TransactionType;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.financial.TransactionInfoResponseDto;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.repository.TransactionRepository;
import com.dope.breaking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public void depositOrWithdrawTransaction(User user, Statement statement){

        Transaction transaction = new Transaction(user, statement,null, statement.getAmount(), user.getBalance(),statement.getTransactionType());
        transactionRepository.save(transaction);

    }

    public void purchasePostTransaction(User buyer, User seller, Purchase purchase){

        Transaction buyerTransaction = new Transaction(buyer, null, purchase, purchase.getPrice(), buyer.getBalance(), TransactionType.BUY_POST);
        Transaction sellerTransaction = new Transaction(seller, null, purchase, purchase.getPrice(), seller.getBalance(), TransactionType.SELL_POST);
        transactionRepository.save(buyerTransaction);
        transactionRepository.save(sellerTransaction);

    }

    public List<TransactionInfoResponseDto> transactionInfoList(String username, Long cursorId, int size){

        User user = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        return transactionRepository.transactionList(user,cursorId,size);

    }

}
