CREATE TABLE payments (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID           NOT NULL,   -- ID only: orders live in order-service
    user_id         UUID           NOT NULL,   -- the JWT subject
    payment_method  VARCHAR(30)    NOT NULL
        CHECK (payment_method IN ('ABA_PAYWAY_KHQR')),
    status          VARCHAR(20)    NOT NULL
        CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'EXPIRED', 'REFUND_REQUIRED')),
    amount          NUMERIC(12, 2) NOT NULL
        CHECK (amount > 0),
    currency        VARCHAR(3)     NOT NULL,
    tran_id         VARCHAR(20)    NOT NULL,   -- the id we give ABA
    qr_string       TEXT,
    deeplink        TEXT,
    expires_at      TIMESTAMPTZ    NOT NULL,
    last_checked_at TIMESTAMPTZ,
    paid_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT uq_payments_tran_id UNIQUE (tran_id)
);

-- At most ONE live payment per order: a repeated POST can never create a second charge
CREATE UNIQUE INDEX uq_payments_order_live ON payments (order_id)
    WHERE status IN ('PENDING', 'COMPLETED');

CREATE INDEX idx_payments_user_created ON payments (user_id, created_at DESC);
CREATE INDEX idx_payments_pending_expiry ON payments (expires_at) WHERE status = 'PENDING';