package com.remo.transaction_scanner.repository;

import com.remo.transaction_scanner.repository.model.SuspiciousHighVolumeTransactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuspiciousHighVolumeTransactionsRepository
    extends JpaRepository<SuspiciousHighVolumeTransactions, Long> {}
