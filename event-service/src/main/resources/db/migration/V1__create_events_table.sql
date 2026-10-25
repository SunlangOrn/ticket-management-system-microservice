CREATE TABLE events (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title       VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    venue_name  VARCHAR(150) NOT NULL,
    city        VARCHAR(100) NOT NULL,
    starts_at   TIMESTAMPTZ  NOT NULL,
    ends_at     TIMESTAMPTZ  NOT NULL,
    status      VARCHAR(20)  NOT NULL CHECK (status IN ('DRAFT', 'PUBLISHED', 'CANCELLED')),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_events_time CHECK (ends_at > starts_at)
);

CREATE INDEX idx_events_status_city_starts ON events (status, city, starts_at);