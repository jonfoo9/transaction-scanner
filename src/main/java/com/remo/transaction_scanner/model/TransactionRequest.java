package com.remo.transaction_scanner.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
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

  @NotNull(message = "UserId cant be null")
  private String userId;

  @NotNull(message = "Transaction type must not be null")
  private TransactionType transactionType;
}
