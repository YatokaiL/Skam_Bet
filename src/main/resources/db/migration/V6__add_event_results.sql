-- feature 6: result of a finished event, null until the event is settled
ALTER TABLE events
    ADD COLUMN result     VARCHAR(10),
    ADD COLUMN settled_at TIMESTAMP;
