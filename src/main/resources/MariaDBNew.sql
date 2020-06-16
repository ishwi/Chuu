CREATE TABLE user
(
    lastfm_id         VARCHAR(45) COLLATE ascii_general_ci  NOT NULL,
    discord_id        BIGINT(20),
    last_update       TIMESTAMP                             NULL     DEFAULT current_timestamp(),
    control_timestamp TIMESTAMP                             NULL     DEFAULT current_timestamp(),
    role              ENUM ('USER','IMAGE_BLOCKED','ADMIN') NOT NULL DEFAULT 'USER',
    private_update    TINYINT(1)                            NOT NULL DEFAULT FALSE,
    PRIMARY KEY (discord_id),
    UNIQUE (lastfm_id)
);

CREATE TABLE guild
(
    guild_id        BIGINT(20)                                         NOT NULL,
    logo            BLOB                                                        DEFAULT NULL,
    prefix          CHAR(1) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT '!',
    crown_threshold INT                                                NOT NULL DEFAULT 0,
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
    url               VARCHAR(400) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
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
    discord_id BIGINT(20),
    guild_id   BIGINT(20),
    url        VARCHAR(400) COLLATE utf8mb4_unicode_ci NOT NULL,
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
    discord_id BIGINT(20),
    added_date DATETIME     NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id),
    CONSTRAINT queuedalias_fk_artsit FOREIGN KEY (artist_id) REFERENCES artist (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT queuedalias_fk_discordid FOREIGN KEY (discord_id) REFERENCES user (discord_id) ON UPDATE CASCADE ON DELETE CASCADE
);
CREATE TABLE alt_url
(
    id         BIGINT(20)                            NOT NULL AUTO_INCREMENT PRIMARY KEY,
    artist_id  BIGINT(20)                            NOT NULL,
    url        VARCHAR(400) COLLATE ascii_general_ci NOT NULL,
    discord_id BIGINT(20)                            NULL,
    added_date DATETIME                              NOT NULL DEFAULT NOW(),
    score      INT                                   NOT NULL DEFAULT 0,
    CONSTRAINT alt_urls_fk_artist FOREIGN KEY (artist_id) REFERENCES artist (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT alt_urls_fk_user FOREIGN KEY (discord_id) REFERENCES user (discord_id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT uc_url UNIQUE (artist_id, url)
);


-- Tigger on add/delete change score in alt_url

CREATE TABLE vote
(
    alt_id     BIGINT(20) NOT NULL,
    discord_id BIGINT(20) NOT NULL,
    ispositive BOOLEAN    NOT NULL,
    added_date DATETIME   NOT NULL DEFAULT NOW(),
    PRIMARY KEY (alt_id, discord_id),
    CONSTRAINT vote_fk_alt_url FOREIGN KEY (alt_id) REFERENCES alt_url (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT vote_fk_user FOREIGN KEY (discord_id) REFERENCES user (discord_id) ON UPDATE CASCADE ON DELETE CASCADE
);
CREATE TABLE reported
(
    id          BIGINT(20) NOT NULL PRIMARY KEY AUTO_INCREMENT,
    alt_id      BIGINT(20) NOT NULL,
    discord_id  BIGINT(20) NOT NULL,
    report_date DATETIME   NOT NULL DEFAULT NOW(),
    UNIQUE (alt_id, discord_id),
    CONSTRAINT reported_fk_alt_url FOREIGN KEY (alt_id) REFERENCES alt_url (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT reported_fk_user FOREIGN KEY (discord_id) REFERENCES user (discord_id) ON UPDATE CASCADE ON DELETE CASCADE
);
DELIMITER //
CREATE TRIGGER vote_add
    AFTER INSERT
    ON vote
    FOR EACH ROW
BEGIN
    UPDATE alt_url SET score = score + if(new.ispositive, 1, -1) WHERE id = new.alt_id;
END;
//
DELIMITER ;

DELIMITER //

CREATE TRIGGER vote_update
    AFTER UPDATE
    ON vote
    FOR EACH ROW
BEGIN
    SET @new_value = 0;
    IF (old.ispositive AND NOT new.ispositive) THEN
        SET @new_value = -2;
    ELSEIF (NOT old.ispositive AND new.ispositive) THEN
        SET @new_value = 2;
    END IF;
    IF (@new_value != 0) THEN
        UPDATE alt_url SET score = score + @new_value WHERE id = new.alt_id;
    END IF;
END;
//
DELIMITER ;

DELIMITER //

CREATE TRIGGER alt_url_insert
    AFTER INSERT
    ON alt_url
    FOR EACH ROW
BEGIN
    IF ((SELECT url FROM artist WHERE id = new.artist_id) IS NULL) OR
       (new.score > (SELECT max(alt_url.score) FROM alt_url WHERE artist_id = new.artist_id))
    THEN
        UPDATE artist SET url = new.url WHERE id = new.artist_id;
    END IF;
END;
//
DELIMITER ;

DELIMITER //
CREATE TRIGGER alt_url_update
    AFTER UPDATE
    ON alt_url
    FOR EACH ROW
BEGIN
    (SELECT max(a.score), a.url
     INTO @current_score,@current_url
     FROM alt_url a
     WHERE a.artist_id = new.artist_id);
    IF ((SELECT url FROM artist b WHERE b.id = new.artist_id) = new.url) AND (new.score < @current_score) THEN
        UPDATE artist SET url = @current_url WHERE id = new.artist_id;
    ELSEIF (new.score >= @current_score) THEN
        UPDATE artist SET url = new.url WHERE id = new.artist_id;
    END IF;
END;
//
DELIMITER ;
DELIMITER //
CREATE TRIGGER alt_url_delete
    AFTER DELETE
    ON alt_url
    FOR EACH ROW
BEGIN
    IF (old.url = (SELECT url FROM artist WHERE id = old.artist_id)) THEN
        UPDATE artist
        SET url = (SELECT url FROM alt_url WHERE artist_id = old.artist_id ORDER BY alt_url.score DESC LIMIT 1)
        WHERE id = old.artist_id;
    END IF;
END;
//
DELIMITER ;


DELIMITER //

CREATE TRIGGER vote_delete
    AFTER DELETE
    ON vote
    FOR EACH ROW
BEGIN
    SET @new_value = 0;
    IF (old.ispositive) THEN
        SET @new_value = -1;
    ELSE
        SET @new_value = 1;
    END IF;
    UPDATE alt_url SET score = score + @new_value WHERE id = old.alt_id;

END;
//
DELIMITER ;

CREATE TABLE log_reported
(
    id       BIGINT(20) NOT NULL PRIMARY KEY AUTO_INCREMENT,
    reported BIGINT(20) NOT NULL,
    modded   BIGINT(20) NOT NULL,
    CONSTRAINT log_reported_fk_user FOREIGN KEY (reported) REFERENCES user (discord_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT log_mod_fk_user FOREIGN KEY (modded) REFERENCES user (discord_id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE TABLE past_recommendations
(
    id          BIGINT(20) NOT NULL PRIMARY KEY AUTO_INCREMENT,
    artist_id   BIGINT(20) NOT NULL,
    receiver_id BIGINT(20) NOT NULL,
    giver_id    BIGINT(20) NOT NULL,
    rec_date    DATETIME   NOT NULL DEFAULT NOW(),
    rating      INTEGER,
    UNIQUE (artist_id, giver_id, receiver_id),
    CONSTRAINT past_recommendations_fk_rec FOREIGN KEY (receiver_id) REFERENCES user (discord_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT past_recommendations_fk_giv FOREIGN KEY (giver_id) REFERENCES user (discord_id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT past_recommendations_fk_artist FOREIGN KEY (artist_id) REFERENCES artist (id) ON DELETE CASCADE ON UPDATE CASCADE

);

CREATE TABLE rate_limited
(
    discord_id     BIGINT(20) NOT NULL PRIMARY KEY,
    queries_second float      NOT NULL,
    CONSTRAINT rate_limiteddiscord_id FOREIGN KEY (discord_id) REFERENCES user (discord_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE command_guild_disabled
(
    guild_id     BIGINT(20)  NOT NULL,
    command_name VARCHAR(40) NOT NULL,
    PRIMARY KEY (guild_id, command_name),
    CONSTRAINT command_guild_disabled_fk_guild FOREIGN KEY (guild_id) REFERENCES guild (guild_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE command_guild_channel_disabled
(
    guild_id     BIGINT(20)  NOT NULL,
    channel_id   BIGINT(20)  NOT NULL,
    command_name VARCHAR(40) NOT NULL,
    enabled      TINYINT(1)  NOT NULL,
    PRIMARY KEY (guild_id, channel_id, command_name),
    CONSTRAINT command_guild_channel_disabled_fk_guild FOREIGN KEY (guild_id) REFERENCES guild (guild_id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE queued_url
(
    id         INT(11)      NOT NULL AUTO_INCREMENT,
    url        VARCHAR(400) NOT NULL,
    artist_id  BIGINT(20)   NOT NULL,
    discord_id BIGINT(20),
    added_date DATETIME     NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id),
    UNIQUE (artist_id, url),
    CONSTRAINT queued_url_fk_artsit FOREIGN KEY (artist_id) REFERENCES artist (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT queued_url_fk_discordid FOREIGN KEY (discord_id) REFERENCES user (discord_id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE `album`
(
    `id`           bigint(20)                              NOT NULL AUTO_INCREMENT,
    `artist_id`    bigint(20)                              DEFAULT NULL,
    `album_name`   varchar(400) COLLATE utf8mb4_unicode_ci NOT NULL,
    `url`          varchar(400) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `rym_id`       bigint(20)                              DEFAULT NULL,
    `mbid`         binary(16)                              DEFAULT NULL,
    `spotify_id`   varchar(40) COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    `release_year` smallint(6)                             DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `rym_id` (`rym_id`),
    UNIQUE KEY `mbid` (`mbid`),
    UNIQUE KEY `spotify_id` (`spotify_id`),
    UNIQUE KEY `artist_id` (`artist_id`, `album_name`),
    CONSTRAINT `album_fk_artist` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE `album_rating`
(
    `id`         bigint(20)  NOT NULL AUTO_INCREMENT,
    `artist_id`  bigint(20) DEFAULT NULL,
    `album_id`   bigint(20) DEFAULT NULL,
    `discord_id` bigint(20) DEFAULT NULL,
    `rating`     tinyint(10) NOT NULL,
    `source`     tinyint(2) DEFAULT 0,
    `review`     text       DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `review_unique` (`artist_id`, `album_id`, `discord_id`),
    KEY `album_rating_url_fk_discordid` (`discord_id`),
    KEY `album_rating_fk_album` (`album_id`),
    CONSTRAINT `album_rating_fk_album` FOREIGN KEY (`album_id`) REFERENCES `album` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `album_rating_fk_artist` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `album_rating_url_fk_discordid` FOREIGN KEY (`discord_id`) REFERENCES `user` (`discord_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;