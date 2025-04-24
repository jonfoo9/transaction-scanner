package com.remo.transaction_scanner.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RestErrorResponse {
  private int status;
  private String error;
  private String message;
}
