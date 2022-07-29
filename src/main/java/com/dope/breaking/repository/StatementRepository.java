package com.dope.breaking.repository;

import com.dope.breaking.domain.financial.Statement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatementRepository extends JpaRepository<Statement, Long> {
}
