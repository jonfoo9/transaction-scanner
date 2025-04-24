package com.remo.transaction_scanner.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class TransactionRequest {

  @NotNull(message = "Amount must not be null")
  @Positive(message = "Amount must be positive")
  private BigDecimal amount;

  @NotNull(message = "Timestamp must not be null")
  private Timestamp timestamp;

  @NotNull(message = "UserId cant be null")
  private String userId;
}
