package com.remo.transaction_scanner.repository.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import lombok.Data;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Data
@Table(name = "suspicious_high_volume_transactions", schema = "transaction_scanner")
public class SuspiciousHighVolumeTransactions {

  @Id private Long id;

  private String userId;
  private Integer cnt;
  private BigDecimal amount;
  private Timestamp timestamp;
  private String type;
}
