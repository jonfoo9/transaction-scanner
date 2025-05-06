package com.remo.transaction_scanner.repository.model;

import com.remo.transaction_scanner.model.TransactionType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import lombok.*;

@Entity
@Table(name = "transactions", schema = "transaction_scanner")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Column(name = "amount", nullable = false)
  private BigDecimal amount;

  @Column(name = "timestamp", nullable = false)
  private Timestamp timestamp;

  @Column(name = "transaction_type", nullable = false)
  @Enumerated(EnumType.STRING)
  private TransactionType transactionType;
}
