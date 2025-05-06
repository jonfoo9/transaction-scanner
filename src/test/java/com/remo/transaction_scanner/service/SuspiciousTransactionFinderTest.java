package com.remo.transaction_scanner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.remo.transaction_scanner.model.TransactionResponse;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
class SuspiciousTransactionFinderTest {

  @Mock private JdbcTemplate jdbcTemplate;

  @InjectMocks private SuspiciousTransactionFinder finder;

  private final String USER_ID = "user123";

  private TransactionResponse buildResponse(
      long id, String userId, BigDecimal amount, Timestamp ts) {
    return TransactionResponse.builder().id(id).userId(userId).amount(amount).timestamp(ts).build();
  }

  @Test
  @DisplayName("Should return empty list when no suspicious transactions found")
  void noSuspiciousTransactions() {
    given(jdbcTemplate.query(any(String.class), any(RowMapper.class), eq(USER_ID)))
        .willReturn(Collections.emptyList());

    List<TransactionResponse> results = finder.getAllSuspiciousTransactionForUserId(USER_ID);

    assertThat(results).isEmpty();
  }

  @Test
  @DisplayName("Should tag only frequent transactions correctly")
  void onlyFrequentTransactions() {
    TransactionResponse tr1 =
        buildResponse(
            1L, USER_ID, new BigDecimal("10.00"), Timestamp.valueOf("2025-04-25 10:00:00"));
    given(
            jdbcTemplate.query(
                eq(
                    "SELECT id, user_id, amount, timestamp, cnt, transaction_type FROM transaction_scanner.suspicious_frequent_transactions WHERE user_id = ?"),
                any(RowMapper.class),
                eq(USER_ID)))
        .willReturn(List.of(tr1));
    given(
            jdbcTemplate.query(
                eq(
                    "SELECT id, user_id, amount, timestamp, transaction_type FROM transaction_scanner.suspicious_high_volume_transactions WHERE user_id = ?"),
                any(RowMapper.class),
                eq(USER_ID)))
        .willReturn(Collections.emptyList());
    given(
            jdbcTemplate.query(
                eq(
                    "SELECT id, user_id, amount, timestamp, five_min_count, transaction_type FROM transaction_scanner.suspicious_rapid_transactions WHERE user_id = ?"),
                any(RowMapper.class),
                eq(USER_ID)))
        .willReturn(Collections.emptyList());

    List<TransactionResponse> results = finder.getAllSuspiciousTransactionForUserId(USER_ID);

    assertThat(results).hasSize(1);
    TransactionResponse result = results.get(0);
    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getSuspiciousReason()).containsExactly("Frequent transaction");
  }

  @Test
  @DisplayName("Should merge reasons for overlapping frequent and high volume transactions")
  void frequentAndHighVolumeMerge() {
    TransactionResponse shared =
        buildResponse(
            5L, USER_ID, new BigDecimal("100.00"), Timestamp.valueOf("2025-04-25 11:00:00"));
    TransactionResponse hv =
        buildResponse(
            5L, USER_ID, new BigDecimal("100.00"), Timestamp.valueOf("2025-04-25 11:00:00"));

    given(
            jdbcTemplate.query(
                eq(
                    "SELECT id, user_id, amount, timestamp, cnt, transaction_type FROM transaction_scanner.suspicious_frequent_transactions WHERE user_id = ?"),
                any(RowMapper.class),
                eq(USER_ID)))
        .willReturn(List.of(shared));
    given(
            jdbcTemplate.query(
                eq(
                    "SELECT id, user_id, amount, timestamp, transaction_type FROM transaction_scanner.suspicious_high_volume_transactions WHERE user_id = ?"),
                any(RowMapper.class),
                eq(USER_ID)))
        .willReturn(List.of(hv));
    given(
            jdbcTemplate.query(
                eq(
                    "SELECT id, user_id, amount, timestamp, five_min_count, transaction_type FROM transaction_scanner.suspicious_rapid_transactions WHERE user_id = ?"),
                any(RowMapper.class),
                eq(USER_ID)))
        .willReturn(Collections.emptyList());

    List<TransactionResponse> results = finder.getAllSuspiciousTransactionForUserId(USER_ID);

    assertThat(results).hasSize(1);
    TransactionResponse tx = results.get(0);
    assertThat(tx.getId()).isEqualTo(5L);
    assertThat(tx.getSuspiciousReason())
        .containsExactlyInAnyOrder("Frequent transaction", "High volume transaction");
  }

  @Test
  @DisplayName("Should handle rapid transactions and merge multiple sources")
  void allSourcesMerged() {
    TransactionResponse f =
        buildResponse(
            1L, USER_ID, new BigDecimal("10.00"), Timestamp.valueOf("2025-04-25 09:00:00"));
    TransactionResponse h =
        buildResponse(
            2L, USER_ID, new BigDecimal("20.00"), Timestamp.valueOf("2025-04-25 09:05:00"));
    TransactionResponse r =
        buildResponse(
            1L, USER_ID, new BigDecimal("10.00"), Timestamp.valueOf("2025-04-25 09:00:00"));

    given(
            jdbcTemplate.query(
                eq(
                    "SELECT id, user_id, amount, timestamp, cnt, transaction_type FROM transaction_scanner.suspicious_frequent_transactions WHERE user_id = ?"),
                any(RowMapper.class),
                eq(USER_ID)))
        .willReturn(List.of(f));
    given(
            jdbcTemplate.query(
                eq(
                    "SELECT id, user_id, amount, timestamp, transaction_type FROM transaction_scanner.suspicious_high_volume_transactions WHERE user_id = ?"),
                any(RowMapper.class),
                eq(USER_ID)))
        .willReturn(List.of(h));
    given(
            jdbcTemplate.query(
                eq(
                    "SELECT id, user_id, amount, timestamp, five_min_count, transaction_type FROM transaction_scanner.suspicious_rapid_transactions WHERE user_id = ?"),
                any(RowMapper.class),
                eq(USER_ID)))
        .willReturn(List.of(r));

    List<TransactionResponse> results = finder.getAllSuspiciousTransactionForUserId(USER_ID);

    assertThat(results).hasSize(2);
    TransactionResponse tx1 =
        results.stream().filter(t -> t.getId() == 1L).findFirst().orElseThrow();
    TransactionResponse tx2 =
        results.stream().filter(t -> t.getId() == 2L).findFirst().orElseThrow();

    assertThat(tx1.getSuspiciousReason())
        .containsExactlyInAnyOrder("Frequent transaction", "Rapid transaction");
    assertThat(tx2.getSuspiciousReason()).containsExactly("High volume transaction");
  }
}
