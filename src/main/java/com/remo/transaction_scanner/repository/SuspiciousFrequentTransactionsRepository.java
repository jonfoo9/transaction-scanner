package com.remo.transaction_scanner.repository;

import com.remo.transaction_scanner.repository.model.SuspiciousFrequentTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuspiciousFrequentTransactionsRepository
    extends JpaRepository<SuspiciousFrequentTransactions, Long> {}
