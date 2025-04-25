package com.remo.transaction_scanner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.remo.transaction_scanner.model.TransactionRequest;
import com.remo.transaction_scanner.model.TransactionResponse;
import com.remo.transaction_scanner.repository.TransactionRepository;
import com.remo.transaction_scanner.repository.model.Transaction;
import jakarta.persistence.PersistenceException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.*;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class TransactionScannerServiceTest {

  @Mock private TransactionRepository transactionRepository;

  @Mock private SuspiciousTransactionFinder suspiciousTransactionFinder;

  @Mock private Clock clock;

  @InjectMocks private TransactionScannerService service;

  private TransactionRequest request;
  private Transaction savedEntity;
  private TransactionResponse expectedResponse;
  private final Instant fixedInstant = Instant.parse("2025-04-25T10:15:30Z");

  @BeforeEach
  void setUp() {
    request = new TransactionRequest();
    request.setUserId("user1");
    request.setAmount(new BigDecimal("123.45"));

    savedEntity =
        Transaction.builder()
            .userId(request.getUserId())
            .amount(request.getAmount())
            .timestamp(Timestamp.from(fixedInstant))
            .build();
    savedEntity.setId(1);

    expectedResponse =
        TransactionResponse.builder()
            .userId("user1")
            .amount(new BigDecimal("123.45"))
            .timestamp(Timestamp.from(fixedInstant))
            .build();
  }

  @Test
  void saveTransaction_success() {
    when(transactionRepository.save(any(Transaction.class))).thenReturn(savedEntity);
    ZoneId zone = ZoneOffset.UTC;
    when(clock.instant()).thenReturn(fixedInstant);
    when(clock.getZone()).thenReturn(zone);

    TransactionResponse response = service.saveTransaction(request);

    assertThat(response).isNotNull();
    assertThat(response.getUserId()).isEqualTo("user1");
    assertThat(response.getAmount()).isEqualByComparingTo("123.45");
    assertThat(response.getTimestamp()).isEqualTo(Timestamp.from(fixedInstant));
  }

  @Test
  void saveTransaction_dataIntegrityViolation_throwsRuntime() {
    ZoneId zone = ZoneOffset.UTC;
    when(clock.instant()).thenReturn(fixedInstant);
    when(clock.getZone()).thenReturn(zone);
    when(transactionRepository.save(any(Transaction.class)))
        .thenThrow(new DataIntegrityViolationException("constraint"));

    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> service.saveTransaction(request));
    assertThat(ex.getMessage()).contains("Error saving transaction");
  }

  @Test
  void saveTransaction_persistenceException_throwsRuntime() {
    ZoneId zone = ZoneOffset.UTC;
    when(clock.instant()).thenReturn(fixedInstant);
    when(clock.getZone()).thenReturn(zone);
    when(transactionRepository.save(any(Transaction.class)))
        .thenThrow(new PersistenceException("db error"));

    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> service.saveTransaction(request));
    assertThat(ex.getMessage()).contains("Error saving transaction");
  }

  @Test
  void saveTransaction_unexpectedException_throwsRuntime() {
    ZoneId zone = ZoneOffset.UTC;
    when(clock.instant()).thenReturn(fixedInstant);
    when(clock.getZone()).thenReturn(zone);
    when(transactionRepository.save(any(Transaction.class)))
        .thenThrow(new RuntimeException("oops"));

    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> service.saveTransaction(request));
    assertThat(ex.getMessage()).contains("Unexpected error occurred");
  }

  @Test
  void getSuspiciousTransactions_delegatesToFinder() {
    List<TransactionResponse> list = Arrays.asList(expectedResponse);
    when(suspiciousTransactionFinder.getAllSuspiciousTransactionForUserId("user1"))
        .thenReturn(list);

    List<TransactionResponse> result = service.getSuspiciousTransactions("user1");

    assertThat(result).hasSize(1);
    assertThat(result).containsExactly(expectedResponse);
  }
}
