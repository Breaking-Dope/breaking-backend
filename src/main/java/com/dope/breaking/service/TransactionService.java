package com.dope.breaking.service;

import com.dope.breaking.domain.financial.Purchase;
import com.dope.breaking.domain.financial.Statement;
import com.dope.breaking.domain.financial.Transaction;
import com.dope.breaking.domain.financial.TransactionType;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public void depositOrWithdrawTransaction (User user, Statement statement){

        Transaction transaction = new Transaction(user, statement,null, statement.getAmount(), statement.getTransactionType());
        transactionRepository.save(transaction);

    }

    public void purchasePostTransaction (User buyer, User seller, Purchase purchase){

        Transaction buyerTransaction = new Transaction(buyer, null, purchase, purchase.getPrice(), TransactionType.BUY_POST);
        Transaction sellerTransaction = new Transaction(seller, null, purchase, purchase.getPrice(), TransactionType.SELL_POST);
        transactionRepository.save(buyerTransaction);
        transactionRepository.save(sellerTransaction);

    }


}
