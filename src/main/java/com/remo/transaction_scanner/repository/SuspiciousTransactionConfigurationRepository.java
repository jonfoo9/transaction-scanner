package com.remo.transaction_scanner.repository;

import com.remo.transaction_scanner.repository.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuspiciousTransactionConfigurationRepository
    extends JpaRepository<Transaction, Long> {}
