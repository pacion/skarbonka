INSERT INTO savings_goals (id, account_id, name, target_amount, balance) VALUES
    (1, 1, 'Wakacje', 1000.00, 600.00),
    (2, 1, 'Nowy laptop', 3000.00, 1200.00),
    (3, 2, 'Rower', 2500.00, 1500.00),
    (4, 2, 'Poduszka finansowa', 500.00, 500.00);

INSERT INTO savings_operations (id, goal_id, type, amount, occurred_at) VALUES
    (1, 1, 'DEPOSIT', 300.00, TIMESTAMP '2025-12-10 10:00:00'),
    (2, 1, 'DEPOSIT', 500.00, TIMESTAMP '2026-01-10 10:00:00'),
    (3, 1, 'WITHDRAWAL', 200.00, TIMESTAMP '2026-02-10 10:00:00'),
    (4, 2, 'DEPOSIT', 600.00, TIMESTAMP '2026-01-15 10:00:00'),
    (5, 2, 'DEPOSIT', 600.00, TIMESTAMP '2026-10-10 10:00:00'),
    (6, 3, 'DEPOSIT', 500.00, TIMESTAMP '2025-12-12 10:00:00'),
    (7, 3, 'DEPOSIT', 800.00, TIMESTAMP '2026-03-12 10:00:00'),
    (8, 3, 'WITHDRAWAL', 100.00, TIMESTAMP '2026-04-12 10:00:00'),
    (9, 3, 'DEPOSIT', 300.00, TIMESTAMP '2026-10-12 10:00:00'),
    (10, 4, 'DEPOSIT', 500.00, TIMESTAMP '2026-07-12 10:00:00');

UPDATE accounts
SET balance = 1200.00
WHERE customer_id = (SELECT id FROM customers WHERE username = 'anna');

UPDATE accounts
SET balance = 1000.00
WHERE customer_id = (SELECT id FROM customers WHERE username = 'patryk');

ALTER TABLE savings_goals ALTER COLUMN id RESTART WITH 5;
ALTER TABLE savings_operations ALTER COLUMN id RESTART WITH 11;
