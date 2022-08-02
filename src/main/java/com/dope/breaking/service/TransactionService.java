package com.dope.breaking.service;

import com.dope.breaking.domain.financial.Purchase;
import com.dope.breaking.domain.financial.Statement;
import com.dope.breaking.domain.financial.Transaction;
import com.dope.breaking.domain.financial.TransactionType;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.financial.TransactionResponseDto;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.user.NoPermissionException;
import com.dope.breaking.repository.TransactionRepository;
import com.dope.breaking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
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

    public List<TransactionResponseDto> transactionList(String username){
        
        User user = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);

        List<Transaction> transactionList = transactionRepository.findAllByUser(user);
        List<TransactionResponseDto> transactionResponseDtoList= new ArrayList<>();
        
        if(transactionList!=null){
            Collections.reverse(transactionList);
            for (Transaction transaction : transactionList) {
                transactionResponseDtoList.add(new TransactionResponseDto(transaction.getTransactionTime(),transaction.getTransactionType().getTitle(), transaction.getAmount(), transaction.getBalance()));
            }
        }
        return transactionResponseDtoList;
    }


}
