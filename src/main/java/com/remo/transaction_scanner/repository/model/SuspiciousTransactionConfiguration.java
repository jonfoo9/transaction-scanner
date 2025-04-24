package com.remo.transaction_scanner.repository.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "transactions", schema = "transaction_scanner")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuspiciousTransactionConfiguration {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
}
