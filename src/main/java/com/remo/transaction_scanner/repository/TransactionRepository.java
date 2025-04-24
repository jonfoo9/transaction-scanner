package com.remo.transaction_scanner.repository;

import com.remo.transaction_scanner.repository.model.Transaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

  List<Transaction> findByUserId(String userId);
}
