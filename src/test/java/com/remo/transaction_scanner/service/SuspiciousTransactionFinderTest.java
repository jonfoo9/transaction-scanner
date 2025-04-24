package com.remo.transaction_scanner.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.remo.transaction_scanner.model.TransactionResponse;
import com.remo.transaction_scanner.repository.SuspiciousFrequentTransactionsRepository;
import com.remo.transaction_scanner.repository.SuspiciousHighVolumeTransactionsRepository;
import com.remo.transaction_scanner.repository.SuspiciousRapidTransactionsRepository;
import com.remo.transaction_scanner.repository.model.SuspiciousFrequentTransactions;
import com.remo.transaction_scanner.repository.model.SuspiciousHighVolumeTransactions;
import com.remo.transaction_scanner.repository.model.SuspiciousRapidTransactions;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SuspiciousTransactionFinderTest {

  @Mock private SuspiciousFrequentTransactionsRepository frequentRepo;

  @Mock private SuspiciousHighVolumeTransactionsRepository highVolumeRepo;

  @Mock private SuspiciousRapidTransactionsRepository rapidRepo;

  @InjectMocks private SuspiciousTransactionFinder finder;

  private SuspiciousFrequentTransactions sft;
  private SuspiciousHighVolumeTransactions shvt;
  private SuspiciousRapidTransactions srt;

  @BeforeEach
  void setUp() {
    sft = new SuspiciousFrequentTransactions();
    sft.setUserId("user1");
    sft.setAmount(new BigDecimal("50.00"));
    sft.setTimestamp(Timestamp.valueOf(LocalDateTime.of(2025, 4, 24, 10, 0)));
    sft.setType("TYPE1");

    shvt = new SuspiciousHighVolumeTransactions();
    shvt.setUserId("user2");
    shvt.setAmount(new BigDecimal("10000.00"));
    shvt.setTimestamp(Timestamp.valueOf(LocalDateTime.of(2025, 4, 24, 11, 0)));
    shvt.setType("TYPE2");

    srt = new SuspiciousRapidTransactions();
    srt.setUserId("user3");
    srt.setAmount(new BigDecimal("20.00"));
    srt.setTimestamp(Timestamp.valueOf(LocalDateTime.of(2025, 4, 24, 10, 5)));
    srt.setType("TYPE3");
  }

  @Test
  void whenNoSuspicious_thenReturnEmptyList() {
    when(frequentRepo.findAll()).thenReturn(List.of());
    when(highVolumeRepo.findAll()).thenReturn(List.of());
    when(rapidRepo.findAll()).thenReturn(List.of());

    List<TransactionResponse> result = finder.getAllSuspiciousTransactionForUserId("any");
    assertThat(result).isEmpty();
  }

  @Test
  void whenOnlyFrequent_thenMapCorrectly() {
    when(frequentRepo.findAll()).thenReturn(List.of(sft));
    when(highVolumeRepo.findAll()).thenReturn(List.of());
    when(rapidRepo.findAll()).thenReturn(List.of());

    List<TransactionResponse> result = finder.getAllSuspiciousTransactionForUserId("user1");
    assertThat(result).hasSize(1);
    TransactionResponse tr = result.get(0);
    assertThat(tr.getUserId()).isEqualTo("user1");
    assertThat(tr.getAmount()).isEqualByComparingTo("50.00");
    assertThat(tr.getTimestamp())
        .isEqualTo(Timestamp.valueOf(LocalDateTime.of(2025, 4, 24, 10, 0)));
    assertThat(tr.getType()).isEqualTo("TYPE1");
    assertThat(tr.getSuspicious()).isTrue();
    assertThat(tr.getSuspiciousReason()).isEqualTo("Frequent transaction");
  }

  @Test
  void whenOnlyHighVolume_thenMapCorrectly() {
    when(frequentRepo.findAll()).thenReturn(List.of());
    when(highVolumeRepo.findAll()).thenReturn(List.of(shvt));
    when(rapidRepo.findAll()).thenReturn(List.of());

    List<TransactionResponse> result = finder.getAllSuspiciousTransactionForUserId("user2");
    assertThat(result).hasSize(1);
    TransactionResponse tr = result.get(0);
    assertThat(tr.getUserId()).isEqualTo("user2");
    assertThat(tr.getSuspiciousReason()).isEqualTo("High volume transaction");
  }

  @Test
  void whenOnlyRapid_thenMapCorrectly() {
    when(frequentRepo.findAll()).thenReturn(List.of());
    when(highVolumeRepo.findAll()).thenReturn(List.of());
    when(rapidRepo.findAll()).thenReturn(List.of(srt));

    List<TransactionResponse> result = finder.getAllSuspiciousTransactionForUserId("user3");
    assertThat(result).hasSize(1);
    TransactionResponse tr = result.get(0);
    assertThat(tr.getUserId()).isEqualTo("user3");
    assertThat(tr.getSuspiciousReason()).isEqualTo("Rapid transaction");
  }

  @Test
  void whenAllTypes_thenCombineAll() {
    when(frequentRepo.findAll()).thenReturn(List.of(sft));
    when(highVolumeRepo.findAll()).thenReturn(List.of(shvt));
    when(rapidRepo.findAll()).thenReturn(List.of(srt));

    List<TransactionResponse> result = finder.getAllSuspiciousTransactionForUserId("all");
    assertThat(result).hasSize(3);
  }
}
