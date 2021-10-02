ALTER TABLE user
    ADD COLUMN discord_access_token varchar(100) NULL,
    ADD COLUMN discord_access_token_expires       timestamp    NULL;
