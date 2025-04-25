package com.remo.transaction_scanner.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.remo.transaction_scanner.exception.RestErrorResponse;
import com.remo.transaction_scanner.model.TransactionRequest;
import com.remo.transaction_scanner.model.TransactionResponse;
import com.remo.transaction_scanner.service.TransactionScannerService;
import jakarta.persistence.PersistenceException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private TransactionScannerService service;

  @Autowired private ObjectMapper objectMapper;

  private TransactionRequest req;
  private TransactionResponse res;

  @BeforeEach
  void setUp() {
    req = new TransactionRequest();
    req.setUserId("user1");
    req.setAmount(new BigDecimal("42.00"));

    res =
        TransactionResponse.builder()
            .userId("user1")
            .amount(new BigDecimal("42.00"))
            .timestamp(Timestamp.valueOf(LocalDateTime.of(2025, 4, 24, 12, 0)))
            .suspicious(false)
            .suspiciousReason(null)
            .build();
  }

  @Test
  void postTransaction_success() throws Exception {
    Mockito.when(service.saveTransaction(any(TransactionRequest.class))).thenReturn(res);

    mockMvc
        .perform(
            post("/transactions/transaction/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(content().json(objectMapper.writeValueAsString(res)));
  }

  @Test
  void postTransaction_validationError() throws Exception {
    Mockito.when(service.saveTransaction(any()))
        .thenThrow(new DataIntegrityViolationException("bad data"));

    RestErrorResponse err =
        new RestErrorResponse(
            400,
            "INVALID_REQUEST",
            "The transaction data is invalid or violates integrity constraints.");

    mockMvc
        .perform(
            post("/transactions/transaction/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isBadRequest())
        .andExpect(content().json(objectMapper.writeValueAsString(err)));
  }

  @Test
  void postTransaction_persistenceError() throws Exception {
    Mockito.when(service.saveTransaction(any())).thenThrow(new PersistenceException("oops"));

    RestErrorResponse err =
        new RestErrorResponse(
            500, "SERVER_ERROR", "There was an error while processing the transaction.");

    mockMvc
        .perform(
            post("/transactions/transaction/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isInternalServerError())
        .andExpect(content().json(objectMapper.writeValueAsString(err)));
  }

  @Test
  void postTransaction_unknownError() throws Exception {
    Mockito.when(service.saveTransaction(any())).thenThrow(new RuntimeException("boom"));

    RestErrorResponse err =
        new RestErrorResponse(
            500, "UNKNOWN_ERROR", "An unexpected error occurred. Please try again later.");

    mockMvc
        .perform(
            post("/transactions/transaction/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isInternalServerError())
        .andExpect(content().json(objectMapper.writeValueAsString(err)));
  }

  @Test
  void getSuspiciousTransactions_noContent() throws Exception {
    Mockito.when(service.getSuspiciousTransactions("user1")).thenReturn(Collections.emptyList());

    mockMvc.perform(get("/transactions/suspicious/user1")).andExpect(status().isNoContent());
  }

  @Test
  void getSuspiciousTransactions_success() throws Exception {
    List<TransactionResponse> list = List.of(res);
    Mockito.when(service.getSuspiciousTransactions("user1")).thenReturn(list);

    mockMvc
        .perform(get("/transactions/suspicious/user1"))
        .andExpect(status().isOk())
        .andExpect(content().json(objectMapper.writeValueAsString(list)));
  }

  @Test
  void getSuspiciousTransactions_validationError() throws Exception {
    Mockito.when(service.getSuspiciousTransactions("user1"))
        .thenThrow(new DataIntegrityViolationException("bad"));

    RestErrorResponse err =
        new RestErrorResponse(
            400,
            "INVALID_REQUEST",
            "The transaction data is invalid or violates integrity constraints.");

    mockMvc
        .perform(get("/transactions/suspicious/user1"))
        .andExpect(status().isBadRequest())
        .andExpect(content().json(objectMapper.writeValueAsString(err)));
  }

  @Test
  void getSuspiciousTransactions_persistenceError() throws Exception {
    Mockito.when(service.getSuspiciousTransactions("user1"))
        .thenThrow(new PersistenceException("oops"));

    RestErrorResponse err =
        new RestErrorResponse(
            500, "SERVER_ERROR", "There was an error while processing the transaction.");

    mockMvc
        .perform(get("/transactions/suspicious/user1"))
        .andExpect(status().isInternalServerError())
        .andExpect(content().json(objectMapper.writeValueAsString(err)));
  }

  @Test
  void getSuspiciousTransactions_unknownError() throws Exception {
    Mockito.when(service.getSuspiciousTransactions("user1"))
        .thenThrow(new RuntimeException("boom"));

    RestErrorResponse err =
        new RestErrorResponse(
            500, "UNKNOWN_ERROR", "An unexpected error occurred. Please try again later.");

    mockMvc
        .perform(get("/transactions/suspicious/user1"))
        .andExpect(status().isInternalServerError())
        .andExpect(content().json(objectMapper.writeValueAsString(err)));
  }
}
