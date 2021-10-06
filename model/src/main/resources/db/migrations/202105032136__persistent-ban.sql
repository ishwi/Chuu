CREATE TABLE botted
(
    id        bigint PRIMARY KEY AUTO_INCREMENT NOT NULL,
    lastfm_id varchar(45) CHARSET ascii         NOT NULL,
    UNIQUE (lastfm_id)
);
INSERT INTO
    botted(lastfm_id)
SELECT
    lastfm_id
FROM
    user
WHERE
    botted_account = TRUE;


CREATE TABLE image_blocked
(
    id         bigint PRIMARY KEY AUTO_INCREMENT NOT NULL,
    discord_id bigint                            NOT NULL,
    UNIQUE (discord_id)
);
INSERT INTO
    image_blocked(discord_id)
SELECT
    discord_id
FROM
    user
WHERE
    role = 'IMAGE_BLOCKED';

CREATE TRIGGER botter
    AFTER INSERT
    ON botted
    FOR EACH ROW
    UPDATE user
    SET
        botted_account = TRUE
    WHERE
        lastfm_id = new.lastfm_id;

CREATE TRIGGER imaged
    AFTER INSERT
    ON image_blocked
    FOR EACH ROW
    UPDATE user
    SET
        role = 'IMAGE_BLOCKED'
    WHERE
        discord_id = new.discord_id;


CREATE TABLE role_colour_server
(
    id               bigint PRIMARY KEY AUTO_INCREMENT NOT NULL,
    guild_id         bigint                            NOT NULL,
    colour           varchar(10)                       NOT NULL,
    start_breakpoint int                               NOT NULL,
    end_breakpoint   int                               NOT NULL,
    role_id          bigint UNIQUE                     NOT NULL,
    UNIQUE (guild_id, colour, start_breakpoint, end_breakpoint)
);
ALTER TABLE user
    ADD COLUMN chart_options bigint NOT NULL DEFAULT 0;
ALTER TABLE randomlinks
    DROP CONSTRAINT randomlinks_fk_guild,
    DROP COLUMN guild_id;
ALTER TABLE command_logs
    ADD COLUMN success boolean;
ALTER TABLE command_logs
    ADD COLUMN is_slash boolean;

ALTER TABLE guild
    ADD COLUMN announcement_id bigint NULL;
ALTER TABLE guild
    ADD COLUMN announcement_enabled boolean NOT NULL DEFAULT TRUE;
