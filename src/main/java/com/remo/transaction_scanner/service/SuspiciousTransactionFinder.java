package com.remo.transaction_scanner.service;

import com.remo.transaction_scanner.model.TransactionResponse;
import com.remo.transaction_scanner.repository.*;
import com.remo.transaction_scanner.repository.model.SuspiciousFrequentTransactions;
import com.remo.transaction_scanner.repository.model.SuspiciousHighVolumeTransactions;
import com.remo.transaction_scanner.repository.model.SuspiciousRapidTransactions;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SuspiciousTransactionFinder {

  private final SuspiciousHighVolumeTransactionsRepository
      suspiciousHighVolumeTransactionsRepository;
  private final SuspiciousRapidTransactionsRepository suspiciousRapidTransactionsRepository;
  private final SuspiciousFrequentTransactionsRepository suspiciousFrequentTransactionsRepository;

  @Autowired
  public SuspiciousTransactionFinder(
      SuspiciousHighVolumeTransactionsRepository suspiciousHighVolumeTransactionsRepository,
      SuspiciousRapidTransactionsRepository suspiciousRapidTransactionsRepository,
      SuspiciousFrequentTransactionsRepository suspiciousFrequentTransactionsRepository) {
    this.suspiciousHighVolumeTransactionsRepository = suspiciousHighVolumeTransactionsRepository;
    this.suspiciousRapidTransactionsRepository = suspiciousRapidTransactionsRepository;
    this.suspiciousFrequentTransactionsRepository = suspiciousFrequentTransactionsRepository;
  }

  public List<TransactionResponse> getAllSuspiciousTransactionForUserId(String userId) {
    List<SuspiciousFrequentTransactions> suspiciousFrequentTransactions =
        suspiciousFrequentTransactionsRepository.findAll();
    List<SuspiciousHighVolumeTransactions> suspiciousHighVolumeTransactions =
        suspiciousHighVolumeTransactionsRepository.findAll();
    List<SuspiciousRapidTransactions> suspiciousRapidTransactions =
        suspiciousRapidTransactionsRepository.findAll();

    List<TransactionResponse> allSuspiciousTransactions = new ArrayList<>();

    suspiciousFrequentTransactions.forEach(
        sft -> {
          TransactionResponse transactionResponse =
              TransactionResponse.builder()
                  .userId(sft.getUserId())
                  .amount(sft.getAmount())
                  .timestamp(sft.getTimestamp())
                  .type(sft.getType())
                  .suspicious(true) // You can add additional suspicious flag logic if needed
                  .suspiciousReason("Frequent transaction")
                  .build();
          allSuspiciousTransactions.add(transactionResponse);
        });

    suspiciousHighVolumeTransactions.forEach(
        shvt -> {
          TransactionResponse transactionResponse =
              TransactionResponse.builder()
                  .userId(shvt.getUserId())
                  .amount(shvt.getAmount())
                  .timestamp(shvt.getTimestamp())
                  .type(shvt.getType())
                  .suspicious(true)
                  .suspiciousReason("High volume transaction")
                  .build();
          allSuspiciousTransactions.add(transactionResponse);
        });

    suspiciousRapidTransactions.forEach(
        srt -> {
          TransactionResponse transactionResponse =
              TransactionResponse.builder()
                  .userId(srt.getUserId())
                  .amount(srt.getAmount())
                  .timestamp(srt.getTimestamp())
                  .type(srt.getType())
                  .suspicious(true)
                  .suspiciousReason("Rapid transaction")
                  .build();
          allSuspiciousTransactions.add(transactionResponse);
        });

    return allSuspiciousTransactions;
  }
}
