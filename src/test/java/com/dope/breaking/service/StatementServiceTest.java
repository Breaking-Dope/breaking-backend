package com.dope.breaking.service;

import com.dope.breaking.domain.financial.TransactionType;
import com.dope.breaking.domain.user.Role;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.exception.financial.NotEnoughBalanceException;
import com.dope.breaking.repository.StatementRepository;
import com.dope.breaking.repository.TransactionRepository;
import com.dope.breaking.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Transactional
class StatementServiceTest {

    @Autowired
    private StatementService statementService;

    @Autowired
    private StatementRepository statementRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @DisplayName("유저네임이 일치할 때 입금을 시도할 경우, 입금이 정상적으로 진행된다.")
    @Test
    void deposit() {

        //Given
        User user = new User();
        user.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username", Role.USER);

        userRepository.save(user);

        //When
        statementService.depositOrWithdraw("username", 10000, TransactionType.DEPOSIT);
        statementService.depositOrWithdraw("username", 20000, TransactionType.DEPOSIT);

        //Then
        Assertions.assertEquals(2, statementRepository.findAll().size());
        Assertions.assertEquals(2, transactionRepository.findAll().size());
        Assertions.assertEquals(30000, user.getBalance());

    }

    @DisplayName("잔액이 출금액보다 많을 경우, 출금이 정상적으로 진행된다.")
    @Test
    void withdraw() {

        //Given
        User user = new User();
        user.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username", Role.USER);

        userRepository.save(user);

        //When
        statementService.depositOrWithdraw("username", 30000, TransactionType.DEPOSIT);
        statementService.depositOrWithdraw("username", 20000, TransactionType.WITHDRAW);

        //Then
        Assertions.assertEquals(2, statementRepository.findAll().size());
        Assertions.assertEquals(2, transactionRepository.findAll().size());
        Assertions.assertEquals(10000, user.getBalance());

    }

    @DisplayName("잔액이 출금액이 동일할 경우, 출금이 정상적으로 진행된다.")
    @Test
    void withdrawAll() {

        //Given
        User user = new User();
        user.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username", Role.USER);

        userRepository.save(user);

        //When
        statementService.depositOrWithdraw("username", 30000, TransactionType.DEPOSIT);
        statementService.depositOrWithdraw("username", 30000, TransactionType.WITHDRAW);

        //Then
        Assertions.assertEquals(2, statementRepository.findAll().size());
        Assertions.assertEquals(2, transactionRepository.findAll().size());
        Assertions.assertEquals(00000, user.getBalance());

    }

    @DisplayName("잔액보다 많이 출금할 경우, 예외가 발생한다.")
    @Test
    void withdrawMoreThanBalance() {

        //Given
        User user = new User();
        user.setRequestFields("URL","anyURL","nickname", "01012345678","mwk300@nyu.edu","Minwu Kim","msg","username", Role.USER);

        userRepository.save(user);

        //When
        statementService.depositOrWithdraw("username", 30000, TransactionType.DEPOSIT);

        //Then
        Assertions.assertThrows ( NotEnoughBalanceException.class, ()
                -> statementService.depositOrWithdraw("username", 40000, TransactionType.WITHDRAW)); //When

    }
}