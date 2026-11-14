-- feature 17: an event can be annulled (match cancelled or postponed), bets on it are returned.
-- settled_at is also set for an annulled event, so it is shown among the results
ALTER TABLE events
    ADD COLUMN annulled_at  TIMESTAMP,
    ADD COLUMN annul_reason VARCHAR(255);
