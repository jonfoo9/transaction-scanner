package com.remo.transaction_scanner.controller;

import com.remo.transaction_scanner.exception.RestErrorResponse;
import com.remo.transaction_scanner.model.TransactionRequest;
import com.remo.transaction_scanner.model.TransactionResponse;
import com.remo.transaction_scanner.service.TransactionScannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.PersistenceException;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Transaction Management", description = "Endpoints for managing transactions")
public class TransactionController {

  private final TransactionScannerService transactionService;

  @Autowired
  public TransactionController(TransactionScannerService transactionService) {
    this.transactionService = transactionService;
  }

  @Operation(summary = "Post a transaction", description = "Posts transaction.")
  @PostMapping("/transactions")
  public ResponseEntity<?> postTransaction(@Valid @RequestBody TransactionRequest transaction) {
    try {
      TransactionResponse savedTransaction = transactionService.saveTransaction(transaction);
      return new ResponseEntity<>(savedTransaction, HttpStatus.CREATED);

    } catch (DataIntegrityViolationException e) {
      RestErrorResponse error =
          new RestErrorResponse(
              HttpStatus.BAD_REQUEST.value(),
              "INVALID_REQUEST",
              "The transaction data is invalid or violates integrity constraints.");
      return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    } catch (PersistenceException e) {
      RestErrorResponse error =
          new RestErrorResponse(
              HttpStatus.INTERNAL_SERVER_ERROR.value(),
              "SERVER_ERROR",
              "There was an error while processing the transaction.");
      return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (Exception e) {
      RestErrorResponse error =
          new RestErrorResponse(
              HttpStatus.INTERNAL_SERVER_ERROR.value(),
              "UNKNOWN_ERROR",
              "An unexpected error occurred. Please try again later.");
      return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Operation(
      summary = "Get suspicious transactions for a user",
      description = "Retrieves all suspicious transactions for the given user ID.")
  @GetMapping("/users/{userId}/transactions/suspicious")
  public ResponseEntity<List<TransactionResponse>> getSuspiciousTransactions(
      @PathVariable String userId) {
    try {
      List<TransactionResponse> suspiciousTransactions =
          transactionService.getSuspiciousTransactions(userId);

      if (suspiciousTransactions.isEmpty()) {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      }
      return new ResponseEntity<>(suspiciousTransactions, HttpStatus.OK);
    } catch (DataIntegrityViolationException e) {
      RestErrorResponse error =
          new RestErrorResponse(
              HttpStatus.BAD_REQUEST.value(),
              "INVALID_REQUEST",
              "The transaction data is invalid or violates integrity constraints.");
      return new ResponseEntity(error, HttpStatus.BAD_REQUEST);
    } catch (PersistenceException e) {
      RestErrorResponse error =
          new RestErrorResponse(
              HttpStatus.INTERNAL_SERVER_ERROR.value(),
              "SERVER_ERROR",
              "There was an error while processing the transaction.");
      return new ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR);
    } catch (Exception e) {
      RestErrorResponse error =
          new RestErrorResponse(
              HttpStatus.INTERNAL_SERVER_ERROR.value(),
              "UNKNOWN_ERROR",
              "An unexpected error occurred. Please try again later.");
      return new ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
