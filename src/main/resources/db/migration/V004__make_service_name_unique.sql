ALTER TABLE subscriptions
    ADD CONSTRAINT unique_service_name UNIQUE (service_name);
