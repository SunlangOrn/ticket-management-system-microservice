CREATE TABLE orders (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID           NOT NULL,   -- the JWT subject: users live in user-service
    event_id         UUID           NOT NULL,
    ticket_type_id   UUID           NOT NULL,
    ticket_type_name VARCHAR(100)   NOT NULL,   -- snapshot at purchase time
    unit_price       NUMERIC(12, 2) NOT NULL CHECK (unit_price >= 0),  -- snapshot
    quantity         INTEGER        NOT NULL CHECK (quantity BETWEEN 1 AND 10),
    total_amount     NUMERIC(12, 2) NOT NULL CHECK (total_amount >= 0),
    reservation_id   UUID           NOT NULL,
    status           VARCHAR(20)    NOT NULL
    CHECK (status IN ('PENDING_PAYMENT', 'PAID', 'CANCELLED', 'EXPIRED')),
    expires_at       TIMESTAMPTZ    NOT NULL,
    version          BIGINT         NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_orders_user_created ON orders (user_id, created_at DESC);
CREATE UNIQUE INDEX uq_orders_reservation ON orders (reservation_id);