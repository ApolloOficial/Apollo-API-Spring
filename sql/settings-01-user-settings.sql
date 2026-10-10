BEGIN;

CREATE TABLE IF NOT EXISTS employee_settings (
    employee_id         UUID PRIMARY KEY REFERENCES employee (id) ON DELETE CASCADE,
    language            VARCHAR(10) NOT NULL DEFAULT 'pt-BR',
    notify_predictive   BOOLEAN     NOT NULL DEFAULT TRUE,
    notify_panel_alert  BOOLEAN     NOT NULL DEFAULT TRUE,
    notify_email        BOOLEAN     NOT NULL DEFAULT FALSE,
    phone               VARCHAR(20),
    password_changed_at TIMESTAMP,
    updated_at          TIMESTAMP   NOT NULL DEFAULT now(),
    CONSTRAINT ck_employee_settings_language CHECK (language IN ('pt-BR', 'es', 'en')),
    CONSTRAINT ck_employee_settings_phone CHECK (phone IS NULL OR phone ~ '^[0-9]{10,11}$')
);

CREATE TABLE IF NOT EXISTS employee_photo (
    employee_id  UUID PRIMARY KEY REFERENCES employee (id) ON DELETE CASCADE,
    content_type VARCHAR(50) NOT NULL,
    data         BYTEA       NOT NULL,
    updated_at   TIMESTAMP   NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS phone_change_request (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id UUID        NOT NULL REFERENCES employee (id) ON DELETE CASCADE,
    new_phone   VARCHAR(20) NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMP   NOT NULL DEFAULT now(),
    reviewed_by UUID REFERENCES employee (id),
    reviewed_at TIMESTAMP,
    CONSTRAINT ck_phone_change_request_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT ck_phone_change_request_phone CHECK (new_phone ~ '^[0-9]{10,11}$')
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_phone_change_request_pending
    ON phone_change_request (employee_id) WHERE status = 'PENDING';

CREATE INDEX IF NOT EXISTS ix_phone_change_request_status ON phone_change_request (status, created_at DESC);

CREATE TABLE IF NOT EXISTS device_session (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    employee_id    UUID        NOT NULL REFERENCES employee (id) ON DELETE CASCADE,
    device_name    VARCHAR(80) NOT NULL,
    first_login_at TIMESTAMP   NOT NULL DEFAULT now(),
    last_login_at  TIMESTAMP   NOT NULL DEFAULT now(),
    last_seen_at   TIMESTAMP   NOT NULL DEFAULT now(),
    CONSTRAINT ux_device_session_employee_device UNIQUE (employee_id, device_name)
);

COMMIT;
