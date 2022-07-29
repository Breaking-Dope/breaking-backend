package com.dope.breaking.service;

import com.dope.breaking.domain.financial.Statement;
import com.dope.breaking.domain.financial.TransactionType;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.financial.NotEnoughBalanceException;
import com.dope.breaking.repository.StatementRepository;
import com.dope.breaking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StatementService {

    private final StatementRepository statementRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;

    public void depositOrWithdraw (String username, int amount, TransactionType transactionType) {

        User user = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);

        if (transactionType == TransactionType.DEPOSIT) {
            user.updateBalance(amount);
        }

        else if (transactionType == TransactionType.WITHDRAW) {

            if (user.getBalance() < amount) {
                throw new NotEnoughBalanceException();
            }

            user.updateBalance(amount*(-1));

        }

        Statement statement = new Statement(user, transactionType, amount);
        statementRepository.save(statement);

        transactionService.depositOrWithdrawTransaction(user, statement);

    }

}