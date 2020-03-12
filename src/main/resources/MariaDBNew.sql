CREATE TABLE user
(
    lastfm_id         VARCHAR(45) COLLATE ascii_general_ci  NOT NULL,
    discord_id        BIGINT(20),
    last_update       TIMESTAMP                             NULL     DEFAULT current_timestamp(),
    control_timestamp TIMESTAMP                             NULL     DEFAULT current_timestamp(),
    role              ENUM ('USER','IMAGE_BLOCKED','ADMIN') NOT NULL DEFAULT 'USER',
    PRIMARY KEY (discord_id),
    UNIQUE (lastfm_id)
);

CREATE TABLE guild
(
    guild_id BIGINT(20)                                         NOT NULL,
    logo     BLOB                                                        DEFAULT NULL,
    prefix   CHAR(1) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT '!',
    PRIMARY KEY (guild_id)
);

CREATE TABLE user_guild
(
    discord_id BIGINT(20) NOT NULL,
    guild_id   BIGINT(20) NOT NULL,
    PRIMARY KEY (discord_id, guild_id),
    CONSTRAINT user_guild_fk_user FOREIGN KEY (discord_id) REFERENCES user (discord_id) ON DELETE CASCADE ON UPDATE CASCADE

);


CREATE TABLE artist
(
    id                BIGINT(20)                              NOT NULL AUTO_INCREMENT,
    name              VARCHAR(400) COLLATE utf8mb4_unicode_ci NOT NULL,
    url               VARCHAR(180) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    url_status        TINYINT(1)                              DEFAULT 1,
    correction_status TINYINT(1)                              DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE (name)
) ROW_FORMAT = DYNAMIC;

CREATE TABLE scrobbled_artist
(
    artist_id  BIGINT(20)                           NOT NULL,
    lastfm_id  VARCHAR(45) COLLATE ascii_general_ci NOT NULL,
    playnumber INT(11)                              NOT NULL,
    PRIMARY KEY (artist_id, lastfm_id),
    CONSTRAINT scrobbled_artist_fk_artist FOREIGN KEY (artist_id) REFERENCES artist (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT scrobbled_artist_fk_user FOREIGN KEY (lastfm_id) REFERENCES user (lastfm_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE album_crowns
(
    artist_id BIGINT(20)                              NOT NULL,
    discordid BIGINT(20)                              NOT NULL,
    album     VARCHAR(250) COLLATE utf8mb4_unicode_ci NOT NULL,
    plays     INT(11)                                 NOT NULL,
    guildid   BIGINT(20)                              NOT NULL,
    PRIMARY KEY (artist_id, album, guildid),
    UNIQUE KEY artist_id_unique (artist_id, album, guildid),
    CONSTRAINT `album_crowns_fk_artist_id ` FOREIGN KEY (artist_id) REFERENCES artist (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT album_crown_fk_guildid FOREIGN KEY (discordid, guildid) REFERENCES user_guild (discord_id, guild_id) ON DELETE CASCADE
) ROW_FORMAT = DYNAMIC;

CREATE TABLE corrections
(
    id        BIGINT(20) AUTO_INCREMENT,
    alias     VARCHAR(250) COLLATE utf8mb4_unicode_ci NOT NULL,
    artist_id BIGINT(20)                              NOT NULL,
    CONSTRAINT corrections_fk_artist FOREIGN KEY (artist_id) REFERENCES artist (id) ON UPDATE CASCADE ON DELETE CASCADE,
    PRIMARY KEY (id)
) ROW_FORMAT = DYNAMIC;
CREATE TABLE metrics
(
    id    INT(11) NOT NULL AUTO_INCREMENT,
    name  VARCHAR(100) CHARACTER SET utf8mb4 DEFAULT NULL,
    value BIGINT(20)                         DEFAULT 0,
    PRIMARY KEY (id)
);

CREATE TABLE randomlinks
(
    discord_id BIGINT(20)                              ,
    guild_id   BIGINT(20)                              ,
    url        VARCHAR(200) COLLATE utf8mb4_unicode_ci NOT NULL,
    PRIMARY KEY (url),
    UNIQUE KEY unique_url_random (url),
    CONSTRAINT randomlinks_fk_user FOREIGN KEY (discord_id) REFERENCES user (discord_id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT randomlinks_fk_guild FOREIGN KEY (guild_id) REFERENCES guild (guild_id) ON UPDATE CASCADE ON DELETE CASCADE
) ROW_FORMAT = DYNAMIC;
CREATE TABLE queued_alias
(
    id         INT(11)      NOT NULL AUTO_INCREMENT,
    alias      VARCHAR(255) NOT NULL,
    artist_id  BIGINT(20)   NOT NULL,
    discord_id BIGINT(20)   NOT NULL,
    added_date DATETIME     NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id),
    CONSTRAINT queuedalias_fk_artsit FOREIGN KEY (artist_id) REFERENCES artist (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT queuedalias_fk_discordid FOREIGN KEY (discord_id) REFERENCES user (discord_id) ON UPDATE CASCADE ON DELETE CASCADE
) ROW_FORMAT = DYNAMIC;
