package com.dope.breaking.repository;

import com.dope.breaking.domain.financial.Transaction;
import com.dope.breaking.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository <Transaction, Long> {

    List<Transaction> findAllByUserOrderByTransactionTimeDesc(User user);

}
