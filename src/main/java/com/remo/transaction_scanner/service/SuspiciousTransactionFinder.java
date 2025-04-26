package com.remo.transaction_scanner.service;

import com.remo.transaction_scanner.model.TransactionResponse;
import com.remo.transaction_scanner.repository.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SuspiciousTransactionFinder {

  private final JdbcTemplate jdbcTemplate;

  private static final RowMapper<TransactionResponse> suspiciousViewRowMapper =
      (rs, rowNum) ->
          TransactionResponse.builder()
              .id(rs.getLong("id"))
              .userId(rs.getString("user_id"))
              .amount(rs.getBigDecimal("amount"))
              .timestamp(rs.getTimestamp("timestamp"))
              .build();

  @Autowired
  public SuspiciousTransactionFinder(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<TransactionResponse> getAllSuspiciousTransactionForUserId(String userId) {
    List<TransactionResponse> allSuspiciousTransactions = new ArrayList<>();
    Map<Long, TransactionResponse> suspiciousTransactions = new HashMap<>();

    String freqSql =
        "SELECT id, user_id, amount, timestamp, cnt FROM transaction_scanner.suspicious_frequent_transactions WHERE user_id = ?";
    List<TransactionResponse> frequent =
        jdbcTemplate.query(freqSql, suspiciousViewRowMapper, userId);
    frequent.forEach(
        tr -> {
          tr.getSuspiciousReason().add("Frequent transaction");
          suspiciousTransactions.put(tr.getId(), tr);
        });

    log.info(
        "Found {} frequent transactions for user {}",
        frequent != null ? frequent.size() : 0,
        userId);

    String highVolSql =
        "SELECT id, user_id, amount, timestamp FROM transaction_scanner.suspicious_high_volume_transactions WHERE user_id = ?";
    List<TransactionResponse> highVolume =
        jdbcTemplate.query(highVolSql, suspiciousViewRowMapper, userId);
    highVolume.forEach(
        tr -> {
          if (suspiciousTransactions.containsKey(tr.getId())) {
            TransactionResponse existing = suspiciousTransactions.get(tr.getId());
            existing.getSuspiciousReason().add("High volume transaction");
          } else {
            tr.getSuspiciousReason().add("High volume transaction");
            suspiciousTransactions.put(tr.getId(), tr);
          }
        });

    log.info(
        "Found {} high volume transactions for user {}",
        highVolume != null ? highVolume.size() : 0,
        userId);

    String rapidSql =
        "SELECT id, user_id, amount, timestamp, five_min_count FROM transaction_scanner.suspicious_rapid_transactions WHERE user_id = ?";
    List<TransactionResponse> rapid = jdbcTemplate.query(rapidSql, suspiciousViewRowMapper, userId);
    rapid.forEach(
        tr -> {
          if (suspiciousTransactions.containsKey(tr.getId())) {
            TransactionResponse existing = suspiciousTransactions.get(tr.getId());
            existing.getSuspiciousReason().add("Rapid transaction");
          } else {
            tr.getSuspiciousReason().add("Rapid transaction");
            suspiciousTransactions.put(tr.getId(), tr);
          }
        });

    log.info("Found {} rapid transactions for user {}", rapid != null ? rapid.size() : 0, userId);
    allSuspiciousTransactions.addAll(rapid);

    return suspiciousTransactions.values().stream()
        .sorted((tr1, tr2) -> tr2.getTimestamp().compareTo(tr1.getTimestamp()))
        .toList();
  }
}
