package com.remo.transaction_scanner.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
  private String userId;
  private BigDecimal amount;
  private Timestamp timestamp;
  private String type;
  private Boolean suspicious;
  private String suspiciousReason;
}
