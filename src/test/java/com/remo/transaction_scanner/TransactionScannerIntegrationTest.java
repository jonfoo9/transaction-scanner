package com.remo.transaction_scanner;

import static org.assertj.core.api.Assertions.assertThat;

import com.remo.transaction_scanner.model.TransactionRequest;
import com.remo.transaction_scanner.model.TransactionResponse;
import com.remo.transaction_scanner.model.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TransactionScannerIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:15")
          .withDatabaseName("testdb")
          .withUsername("test")
          .withPassword("test");

  static {
    postgres.start();
  }

  @DynamicPropertySource
  static void overrideProps(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
  }

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private JdbcTemplate jdbc;

  private String baseUrl;

  @BeforeAll
  void setUp() {
    this.baseUrl = "http://localhost:" + port + "/api/v1";
  }

  private TransactionRequest makeRequest(
      String userId, BigDecimal amount, TransactionType transactionType) {
    return TransactionRequest.builder()
        .userId(userId)
        .amount(amount)
        .transactionType(transactionType)
        .build();
  }

  private void postTxn(TransactionRequest req) {
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
    restTemplate.postForEntity(
        baseUrl + "/transactions", new HttpEntity<>(req, headers), TransactionResponse.class);
  }

  private List<TransactionResponse> getSuspicious(String userId) {
    ResponseEntity<TransactionResponse[]> resp =
        restTemplate.getForEntity(
            baseUrl + "/users/" + userId + "/transactions/suspicious", TransactionResponse[].class);
    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    return List.of(resp.getBody());
  }

  @Test
  void verifySchemaAndData() {
    Integer countTables =
        jdbc.queryForObject(
            "SELECT count(*) FROM information_schema.tables WHERE table_schema='transaction_scanner' AND table_name='transactions'",
            Integer.class);
    assertThat(countTables).isEqualTo(1);
  }

  @Test
  void testFrequentTransactions() {
    String user = "freqUser";
    // threshold = 5 per hour by default
    LocalDateTime now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0);
    // post 6 transactions in same hour
    IntStream.range(0, 6)
        .forEach(i -> postTxn(makeRequest(user, BigDecimal.valueOf(10), TransactionType.DEPOSIT)));

    List<TransactionResponse> suspicious = getSuspicious(user);
    assertThat(suspicious).extracting(TransactionResponse::getUserId).contains(user);
    assertThat(suspicious)
        .extracting(TransactionResponse::getSuspiciousReason)
        .anySatisfy(reasons -> assertThat(reasons).contains("Frequent transaction"));
  }

  @Test
  void testHighVolumeTransactions() {
    String user = "highVolUser";
    // threshold amount = 10000
    postTxn(makeRequest(user, new BigDecimal("15000"), TransactionType.DEPOSIT));
    postTxn(makeRequest(user, new BigDecimal("15000"), TransactionType.DEPOSIT));

    List<TransactionResponse> suspicious = getSuspicious(user);
    assertThat(suspicious).extracting(TransactionResponse::getUserId).contains(user);
    assertThat(suspicious)
        .extracting(TransactionResponse::getSuspiciousReason)
        .anySatisfy(reasons -> assertThat(reasons).contains("High volume transaction"));
  }

  @Test
  void testRapidTransactions() {
    String user = "rapidUser";
    LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
    // 3 in 5 minute window
    postTxn(makeRequest(user, BigDecimal.valueOf(1), TransactionType.DEPOSIT));
    postTxn(makeRequest(user, BigDecimal.valueOf(2), TransactionType.DEPOSIT));
    postTxn(makeRequest(user, BigDecimal.valueOf(3), TransactionType.DEPOSIT));
    postTxn(makeRequest(user, BigDecimal.valueOf(3), TransactionType.DEPOSIT));

    List<TransactionResponse> suspicious = getSuspicious(user);
    assertThat(suspicious).extracting(TransactionResponse::getUserId).contains(user);
    assertThat(suspicious)
        .extracting(TransactionResponse::getSuspiciousReason)
        .anySatisfy(reasons -> assertThat(reasons).contains("Rapid transaction"));
  }
}
