-- feature 6: settlement of events is done by an administrator
ALTER TABLE users
    ADD COLUMN role VARCHAR(10) NOT NULL DEFAULT 'USER';
