CREATE TABLE IF NOT EXISTS users
(
    id                UUID PRIMARY KEY,
    user_name         TEXT,
    email             TEXT,
    registration_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS subscriptions
(
    id           SERIAL PRIMARY KEY,
    user_id      UUID references users (id),
    service_name TEXT,
    start_time   TIMESTAMP,
    end_time     TIMESTAMP
);