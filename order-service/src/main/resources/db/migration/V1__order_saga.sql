-- Chạy thủ công trên Supabase (SQL Editor) nếu không dùng ddl-auto.
-- Hoặc để JPA tạo bảng với ddl-auto: update.

CREATE TABLE IF NOT EXISTS order_saga (
    order_id            VARCHAR(64) PRIMARY KEY,
    process_instance_id VARCHAR(64),
    product_id          VARCHAR(128) NOT NULL,
    quantity            INTEGER      NOT NULL,
    status              VARCHAR(64)  NOT NULL,
    last_error          TEXT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_order_saga_process ON order_saga (process_instance_id);
CREATE INDEX IF NOT EXISTS idx_order_saga_status ON order_saga (status);
