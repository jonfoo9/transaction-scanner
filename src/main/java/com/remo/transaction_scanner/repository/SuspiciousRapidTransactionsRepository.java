package com.remo.transaction_scanner.repository;

import com.remo.transaction_scanner.repository.model.SuspiciousRapidTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuspiciousRapidTransactionsRepository
    extends JpaRepository<SuspiciousRapidTransactions, Long> {}
