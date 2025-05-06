package com.remo.transaction_scanner.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
  private long id;
  private String userId;
  private BigDecimal amount;
  private Timestamp timestamp;
  private Boolean suspicious;
  private TransactionType transactionType;
  @Builder.Default private List<String> suspiciousReason = new ArrayList<>();
}
