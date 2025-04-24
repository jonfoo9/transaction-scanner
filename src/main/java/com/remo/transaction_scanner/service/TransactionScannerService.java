package com.remo.transaction_scanner.service;

import com.remo.transaction_scanner.model.TransactionRequest;
import com.remo.transaction_scanner.model.TransactionResponse;
import com.remo.transaction_scanner.repository.TransactionRepository;
import com.remo.transaction_scanner.repository.model.Transaction;
import jakarta.persistence.PersistenceException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TransactionScannerService {

  private final TransactionRepository transactionRepository;
  private final SuspiciousTransactionFinder suspiciousTransactionFinder;

  @Autowired
  public TransactionScannerService(
      TransactionRepository transactionRepository,
      SuspiciousTransactionFinder suspiciousTransactionFinder) {
    this.transactionRepository = transactionRepository;
    this.suspiciousTransactionFinder = suspiciousTransactionFinder;
  }

  public TransactionResponse saveTransaction(TransactionRequest transactionRequest) {
    try {
      Transaction transaction =
          Transaction.builder()
              .userId(transactionRequest.getUserId())
              .amount(transactionRequest.getAmount())
              .timestamp(transactionRequest.getTimestamp())
              .build();

      Transaction saved = transactionRepository.save(transaction);

      log.info("Saved transaction {}", saved);

      return TransactionResponse.builder()
          .userId(saved.getUserId())
          .amount(saved.getAmount())
          .timestamp(saved.getTimestamp())
          .build();

    } catch (DataIntegrityViolationException | PersistenceException e) {
      throw new RuntimeException("Error saving transaction: " + e.getMessage());
    } catch (Exception e) {
      throw new RuntimeException("Unexpected error occurred: " + e.getMessage());
    }
  }

  public List<TransactionResponse> getSuspiciousTransactions(String userId) {
    return suspiciousTransactionFinder.getAllSuspiciousTransactionForUserId(userId);
  }
}
