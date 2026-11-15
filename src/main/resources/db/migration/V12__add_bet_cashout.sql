-- feature 18: the bookmaker bought the bet back before the start of the event
ALTER TABLE bets
    ADD COLUMN cashout_amount NUMERIC(12, 2) CHECK (cashout_amount > 0),
    ADD COLUMN cashed_out_at  TIMESTAMP;
