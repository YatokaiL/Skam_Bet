-- feature 20: blacklist. blocked_at is set - the user is blocked until blocked_until, null there means forever
ALTER TABLE users
    ADD COLUMN blocked_at    TIMESTAMP,
    ADD COLUMN blocked_until TIMESTAMP,
    ADD COLUMN block_reason  VARCHAR(200),
    ADD CONSTRAINT users_block_reason_check CHECK (blocked_at IS NULL OR block_reason IS NOT NULL);
