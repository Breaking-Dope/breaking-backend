package com.dope.breaking.repository;

import com.dope.breaking.domain.financial.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository <Transaction, Long> {
}
