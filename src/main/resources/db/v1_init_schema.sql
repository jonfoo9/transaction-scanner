-- V1__init_schema.sql

CREATE SCHEMA IF NOT EXISTS transaction_scanner;

CREATE TABLE IF NOT EXISTS transaction_scanner.transactions (
    id SERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    type VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS transaction_scanner.suspicious_transaction_configuration (
    id SERIAL PRIMARY KEY,
    frequent_suspicious_transaction_threshold INTEGER NOT NULL
);

INSERT INTO transaction_scanner.suspicious_transaction_configuration (
    frequent_suspicious_transaction_threshold
) VALUES (
    5
);


CREATE VIEW transaction_scanner.suspicious_frequent_transactions AS
WITH cfg AS (
    SELECT suspicious_transaction_threshold AS n
    FROM transaction_scanner.suspicious_transaction_configuration
    LIMIT 1
),
txns_with_count AS (
    SELECT
        t.id,
        t.user_id,
        t.amount,
        t.timestamp,
        t.type,
        COUNT(*) OVER (PARTITION BY t.user_id, date_trunc('hour', t.timestamp)) AS cnt
    FROM transaction_scanner.transactions t
)
SELECT t.id, t.user_id, t.cnt, t.amount, t.timestamp, t.type
FROM txns_with_count t
CROSS JOIN cfg
WHERE t.cnt >= cfg.n
ORDER BY t.cnt DESC;

CREATE VIEW transaction_scanner.suspicious_high_volume_transactions AS
SELECT *
FROM transaction_scanner.transactions
WHERE amount >= 10000

CREATE VIEW transaction_scanner.suspicious_rapid_transfers AS
SELECT
    t.id,
    t.user_id,
    t.amount,
    t.timestamp,
    t.type,
    COUNT(*) OVER (PARTITION BY t.user_id, floor(date_part('minute', t.timestamp) / 5) * interval '5 minutes' ) as five_min_count
FROM transaction_scanner.transactions t
WHERE five_min_count >= 3


--CREATE OR REPLACE FUNCTION refresh_suspicious_frequent_transactions()
--RETURNS TRIGGER AS $$
--BEGIN
--    REFRESH MATERIALIZED VIEW CONCURRENTLY transaction_scanner.suspicious_frequent_transactions;
--    RETURN NULL;
--END;
--$$;
--
--CREATE TRIGGER trigger_refresh_suspicious_frequent_transactions
--AFTER UPDATE OF frequent_suspicious_transaction_threshold
--ON transaction_scanner.suspicious_transaction_configuration
--FOR EACH ROW
--WHEN (OLD.suspicious_transaction_threshold IS DISTINCT FROM NEW.suspicious_transaction_threshold)
--EXECUTE FUNCTION refresh_suspicious_frequent_transactions();