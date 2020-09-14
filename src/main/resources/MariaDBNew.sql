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
    declare current_score int;
    declare current_url varchar(400);

    set current_score = (SELECT max(a.score)
                         FROM alt_url a
                         WHERE a.artist_id = new.artist_id);
    set current_url = (SELECT a.url
                       FROM alt_url a
                       WHERE a.artist_id = new.artist_id);
    IF ((SELECT url FROM artist b WHERE b.id = new.artist_id) = new.url) AND (new.score < current_score) THEN
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
    `mbid`         varchar(36)                             DEFAULT NULL,
    `spotify_id`   varchar(40) COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    `release_year` smallint(6)                             DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `rym_id` (`rym_id`),
    -- UNIQUE KEY `mbid` (`mbid`),
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

CREATE TABLE scrobbled_album
(
    artist_id  BIGINT(20)                           NOT NULL,
    album_id   BIGINT(20)                           NOT NULL,
    lastfm_id  VARCHAR(45) COLLATE ascii_general_ci NOT NULL,
    playnumber INT(11)                              NOT NULL,
    PRIMARY KEY (album_id, lastfm_id),
    CONSTRAINT scrobbled_album_fk_artist FOREIGN KEY (artist_id) REFERENCES artist (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT scrobbled_album_fk_album FOREIGN KEY (album_id) REFERENCES album (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT scrobbled_album_fk_user FOREIGN KEY (lastfm_id) REFERENCES user (lastfm_id) ON UPDATE CASCADE ON DELETE CASCADE
);

create table week
(
    id         int not null AUTO_INCREMENT,
    week_start date,
    PRIMARY KEY (id)
);
create table weekly_billboard_listeners
(
    id         bigint(20) not null AUTO_INCREMENT,
    guild_id   bigint(20),
    week_id    int,
    artist_id  bigint(20),
    track_name varchar(400),
    position   smallint,
    listeners  int,
    PRIMARY KEY (id),
    -- KEY (guild_id, week_id, artist_id, track_name),
    CONSTRAINT weekly_billboard_artist_id FOREIGN KEY (artist_id) REFERENCES artist (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT weekly_billboard_guild_id FOREIGN KEY (guild_id) REFERENCES guild (guild_id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT weekly_billboard_week_id FOREIGN KEY (week_id) REFERENCES week (id) ON UPDATE CASCADE ON DELETE CASCADE

);

create table weekly_billboard_scrobbles
(
    id             bigint(20) not null AUTO_INCREMENT,
    guild_id       bigint(20),
    week_id        int,
    artist_id      bigint(20),
    track_name     varchar(400),
    position       smallint,
    scrobble_count int,
    PRIMARY KEY (id),
    -- KEY (guild_id, week_id, artist_id, track_name),
    CONSTRAINT weekly_billboard_scrobbles_artist_id FOREIGN KEY (artist_id) REFERENCES artist (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT weekly_billboard_scrobbles_guild_id FOREIGN KEY (guild_id) REFERENCES guild (guild_id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT weekly_billboard_scrobbles_week_id FOREIGN KEY (week_id) REFERENCES week (id) ON UPDATE CASCADE ON DELETE CASCADE
);


DELIMITER $$

create PROCEDURE insert_weeks()
BEGIN
    SET @t_current = date(curdate() - interval weekday(curdate()) day);
    SET @t_end = DATE_ADD(date(curdate() - interval weekday(curdate()) day), INTERVAL 5 YEAR);
    WHILE(@t_current < @t_end)
        DO
            INSERT INTO week(week_start) VALUES (@t_current);
            SET @t_current = DATE_ADD(@t_current, INTERVAL 7 day);
        END WHILE;
END;
$$
DELIMITER ;


create table user_billboard_data
(
    id             BIGINT(20)                           not null AUTO_INCREMENT,
    week_id        int,
    lastfm_id      VARCHAR(45) COLLATE ascii_general_ci NOT NULL,
    artist_id      bigint(20),
    track_name     varchar(400),
    album_name     varchar(400),
    scrobble_count smallint,
    PRIMARY KEY (id),
    CONSTRAINT user_billboard_data_artist_id FOREIGN KEY (artist_id) REFERENCES artist (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT user_billboard_data_guild_id FOREIGN KEY (lastfm_id) REFERENCES user (lastfm_id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT user_billboard_data_week_id FOREIGN KEY (week_id) REFERENCES week (id) ON UPDATE CASCADE ON DELETE CASCADE
);


create table weekly_billboard_artist_listeners
(
    id        bigint(20) not null AUTO_INCREMENT,
    guild_id  bigint(20),
    week_id   int,
    artist_id bigint(20),
    position  smallint,
    listeners int,
    PRIMARY KEY (id),
    -- KEY (guild_id, week_id, artist_id, track_name),
    CONSTRAINT weekly_billboard_artists_artist_id FOREIGN KEY (artist_id) REFERENCES artist (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT weekly_billboard_artist_guild_id FOREIGN KEY (guild_id) REFERENCES guild (guild_id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT weekly_billboard_artist_week_id FOREIGN KEY (week_id) REFERENCES week (id) ON UPDATE CASCADE ON DELETE CASCADE

);

create table weekly_billboard_artist_scrobbles
(
    id             bigint(20) not null AUTO_INCREMENT,
    guild_id       bigint(20),
    week_id        int,
    artist_id      bigint(20),
    position       smallint,
    scrobble_count int,
    PRIMARY KEY (id),
    -- KEY (guild_id, week_id, artist_id, track_name),
    CONSTRAINT weekly_billboard_artist_scrobbles_artist_id FOREIGN KEY (artist_id) REFERENCES artist (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT weekly_billboard_artist_scrobbles_guild_id FOREIGN KEY (guild_id) REFERENCES guild (guild_id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT weekly_billboard_artist_scrobbles_week_id FOREIGN KEY (week_id) REFERENCES week (id) ON UPDATE CASCADE ON DELETE CASCADE
);



create table weekly_billboard_album_listeners
(
    id         bigint(20) not null AUTO_INCREMENT,
    guild_id   bigint(20),
    week_id    int,
    artist_id  bigint(20),
    album_name varchar(400),

    position   smallint,
    listeners  int,
    PRIMARY KEY (id),
    -- KEY (guild_id, week_id, artist_id, track_name),
    CONSTRAINT weekly_billboard_album_artist_id FOREIGN KEY (artist_id) REFERENCES artist (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT weekly_billboard_album_guild_id FOREIGN KEY (guild_id) REFERENCES guild (guild_id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT weekly_billboard_album_week_id FOREIGN KEY (week_id) REFERENCES week (id) ON UPDATE CASCADE ON DELETE CASCADE

);

create table weekly_billboard_album_scrobbles
(
    id             bigint(20) not null AUTO_INCREMENT,
    guild_id       bigint(20),
    week_id        int,
    artist_id      bigint(20),
    album_name     varchar(400),
    position       smallint,
    scrobble_count int,
    PRIMARY KEY (id),
    -- KEY (guild_id, week_id, artist_id, track_name),
    CONSTRAINT weekly_billboard_album_scrobbles_artist_id FOREIGN KEY (artist_id) REFERENCES artist (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT weekly_billboard_album_scrobbles_guild_id FOREIGN KEY (guild_id) REFERENCES guild (guild_id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT weekly_billboard_album_scrobbles_week_id FOREIGN KEY (week_id) REFERENCES week (id) ON UPDATE CASCADE ON DELETE CASCADE
);


-- GLOBAL


create table weekly_billboard_global_listeners
(
    id         bigint(20) not null AUTO_INCREMENT,
    week_id    int,
    artist_id  bigint(20),
    track_name varchar(400),
    position   smallint,
    listeners  int,
    PRIMARY KEY (id),
    -- KEY (guild_id, week_id, artist_id, track_name),
    CONSTRAINT weekly_billboard_global_artist_id FOREIGN KEY (artist_id) REFERENCES artist (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT weekly_billboard_global_week_id FOREIGN KEY (week_id) REFERENCES week (id) ON UPDATE CASCADE ON DELETE CASCADE

);

create table weekly_billboard_global_scrobbles
(
    id             bigint(20) not null AUTO_INCREMENT,
    week_id        int,
    artist_id      bigint(20),
    track_name     varchar(400),
    position       smallint,
    scrobble_count int,
    PRIMARY KEY (id),
    -- KEY (guild_id, week_id, artist_id, track_name),
    CONSTRAINT weekly_billboard_global_scrobbles_artist_id FOREIGN KEY (artist_id) REFERENCES artist (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT weekly_billboard_global_scrobbles_week_id FOREIGN KEY (week_id) REFERENCES week (id) ON UPDATE CASCADE ON DELETE CASCADE
);



create table weekly_billboard_artist_global_scrobbles
(
    id             bigint(20) not null AUTO_INCREMENT,
    week_id        int,
    artist_id      bigint(20),
    position       smallint,
    scrobble_count int,
    PRIMARY KEY (id),
    -- KEY (guild_id, week_id, artist_id, track_name),
    CONSTRAINT weekly_billboard_artist_global_scrobbles_artist_id FOREIGN KEY (artist_id) REFERENCES artist (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT weekly_billboard_artist_global_scrobbles_week_id FOREIGN KEY (week_id) REFERENCES week (id) ON UPDATE CASCADE ON DELETE CASCADE
);


create table weekly_billboard_artist_global_listeners
(
    id        bigint(20) not null AUTO_INCREMENT,
    week_id   int,
    artist_id bigint(20),
    position  smallint,
    listeners int,
    PRIMARY KEY (id),
    -- KEY (guild_id, week_id, artist_id, track_name),
    CONSTRAINT weekly_billboard_artist_global_listeners_artist_id FOREIGN KEY (artist_id) REFERENCES artist (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT weekly_billboard_artist_global_listeners_week_id FOREIGN KEY (week_id) REFERENCES week (id) ON UPDATE CASCADE ON DELETE CASCADE
);


create table weekly_billboard_album_global_listeners
(
    id         bigint(20) not null AUTO_INCREMENT,
    week_id    int,
    artist_id  bigint(20),
    album_name varchar(400),
    position   smallint,
    listeners  int,
    PRIMARY KEY (id),
    -- KEY (guild_id, week_id, artist_id, track_name),
    CONSTRAINT weekly_billboard_album_global_artist_id FOREIGN KEY (artist_id) REFERENCES artist (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT weekly_billboard_album_global_week_id FOREIGN KEY (week_id) REFERENCES week (id) ON UPDATE CASCADE ON DELETE CASCADE

);

create table weekly_billboard_album_global_scrobbles
(
    id             bigint(20) not null AUTO_INCREMENT,
    week_id        int,
    artist_id      bigint(20),
    album_name     varchar(400),
    position       smallint,
    scrobble_count int,
    PRIMARY KEY (id),
    -- KEY (guild_id, week_id, artist_id, track_name),
    CONSTRAINT weekly_billboard_album_global_scrobbles_artist_id FOREIGN KEY (artist_id) REFERENCES artist (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT weekly_billboard_album_global_scrobbles_week_id FOREIGN KEY (week_id) REFERENCES week (id) ON UPDATE CASCADE ON DELETE CASCADE
);


alter table weekly_billboard_album_global_scrobbles
    add index (week_id);
alter table weekly_billboard_album_global_listeners
    add index (week_id);
alter table weekly_billboard_artist_global_scrobbles
    add index (week_id);
alter table weekly_billboard_album_global_scrobbles
    add index (week_id);
alter table weekly_billboard_global_listeners
    add index (week_id);
alter table weekly_billboard_global_scrobbles
    add index (week_id);
alter table weekly_billboard_album_global_scrobbles
    add index (week_id);
alter table weekly_billboard_album_global_listeners
    add index (week_id);
alter table weekly_billboard_artist_global_scrobbles
    add index (week_id);
alter table weekly_billboard_album_global_scrobbles
    add index (week_id);

alter table weekly_billboard_album_scrobbles
    add index (week_id, guild_id);
alter table weekly_billboard_album_listeners
    add index (week_id, guild_id);
alter table weekly_billboard_artist_scrobbles
    add index (week_id, guild_id);
alter table weekly_billboard_album_scrobbles
    add index (week_id, guild_id);
alter table weekly_billboard_listeners
    add index (week_id, guild_id);
alter table weekly_billboard_scrobbles
    add index (week_id, guild_id);
alter table weekly_billboard_album_scrobbles
    add index (week_id, guild_id);
alter table weekly_billboard_album_listeners
    add index (week_id, guild_id);
alter table weekly_billboard_artist_scrobbles
    add index (week_id, guild_id);
alter table weekly_billboard_album_scrobbles
    add index (week_id, guild_id);



create table top_combos
(
    id           bigint       not null AUTO_INCREMENT,
    discord_id   bigint       not null,
    artist_id    bigint       not null,
    album_id     bigint       null,
    track_name   varchar(400) null,
    artist_combo int,
    album_combo  int,
    track_combo  int,
    streak_start TIMESTAMP    not NULL,

    primary key (id),
    unique (discord_id, artist_id, streak_start)
);


CREATE TABLE random_links_ratings
(
    id         bigint(20) PRIMARY KEY                  NOT NULL AUTO_INCREMENT,
    url        VARCHAR(400) COLLATE utf8mb4_unicode_ci NOT NULL,
    discord_id BIGINT(20)                              NOT NULL,
    rating     int,
    unique (url, discord_id),
    CONSTRAINT random_links_ratings_url FOREIGN KEY (url) REFERENCES randomlinks (url) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT random_links_ratings_discord FOREIGN KEY (discord_id) REFERENCES user (discord_id) ON UPDATE CASCADE ON DELETE CASCADE
);


-- Functions for streak calculation


CREATE FUNCTION streak_billboard_track(bill_id bigint(20)) RETURNS int DETERMINISTIC
    RETURN
        (WITH RECURSIVE cte (week_id, artist_id, guild_id, track_name) AS
                            (SELECT week_id,
                                    artist_id,
                                    guild_id,
                                    track_name
                             FROM weekly_billboard_listeners
                             WHERE id = bill_id
                             UNION ALL
                             SELECT b.week_id,
                                    b.artist_id,
                                    b.guild_id,
                                    b.track_name
                             FROM cte t
                                      JOIN weekly_billboard_listeners b ON b.week_id = t.week_id - 1
                                 AND t.artist_id = b.artist_id
                                 AND t.guild_id = b.guild_id
                                 AND t.track_name = b.track_name)
         SELECT count(*)
         FROM cte);


CREATE FUNCTION streak_billboard_track_scrobbles(bill_id bigint(20)) RETURNS int DETERMINISTIC
    RETURN
        (WITH RECURSIVE cte (week_id, artist_id, guild_id, track_name) AS
                            (SELECT week_id,
                                    artist_id,
                                    guild_id,
                                    track_name
                             FROM weekly_billboard_scrobbles
                             WHERE id = bill_id
                             UNION ALL
                             SELECT b.week_id,
                                    b.artist_id,
                                    b.guild_id,
                                    b.track_name
                             FROM cte t
                                      JOIN weekly_billboard_scrobbles b ON b.week_id = t.week_id - 1
                                 AND t.artist_id = b.artist_id
                                 AND t.guild_id = b.guild_id
                                 AND t.track_name = b.track_name)
         SELECT count(*)
         FROM cte);


CREATE FUNCTION streak_billboard_artist(bill_id bigint(20)) RETURNS int DETERMINISTIC
    RETURN
        (WITH RECURSIVE cte (week_id, artist_id, guild_id) AS
                            (SELECT week_id,
                                    artist_id,
                                    guild_id
                             FROM weekly_billboard_artist_listeners
                             WHERE id = bill_id
                             UNION ALL
                             SELECT b.week_id,
                                    b.artist_id,
                                    b.guild_id
                             FROM cte t
                                      JOIN weekly_billboard_artist_listeners b ON b.week_id = t.week_id - 1
                                 AND t.artist_id = b.artist_id
                                 AND t.guild_id = b.guild_id)
         SELECT count(*)
         FROM cte);


CREATE FUNCTION streak_billboard_artist_scrobbles(bill_id bigint(20)) RETURNS int DETERMINISTIC
    RETURN
        (WITH RECURSIVE cte (week_id, artist_id, guild_id) AS
                            (SELECT week_id,
                                    artist_id,
                                    guild_id
                             FROM weekly_billboard_artist_scrobbles
                             WHERE id = bill_id
                             UNION ALL
                             SELECT b.week_id,
                                    b.artist_id,
                                    b.guild_id
                             FROM cte t
                                      JOIN weekly_billboard_artist_scrobbles b ON b.week_id = t.week_id - 1
                                 AND t.artist_id = b.artist_id
                                 AND t.guild_id = b.guild_id)
         SELECT count(*)
         FROM cte);


CREATE FUNCTION streak_billboard_album_scrobbles(bill_id bigint(20)) RETURNS int DETERMINISTIC
    RETURN
        (WITH RECURSIVE cte (week_id, artist_id, guild_id, album_name) AS
                            (SELECT week_id,
                                    artist_id,
                                    guild_id,
                                    album_name
                             FROM weekly_billboard_album_scrobbles
                             WHERE id = bill_id
                             UNION ALL
                             SELECT b.week_id,
                                    b.artist_id,
                                    b.guild_id,
                                    b.album_name
                             FROM cte t
                                      JOIN weekly_billboard_album_scrobbles b ON b.week_id = t.week_id - 1
                                 AND t.artist_id = b.artist_id
                                 AND t.guild_id = b.guild_id
                                 AND t.album_name = b.album_name)
         SELECT count(*)
         FROM cte);


CREATE FUNCTION streak_billboard_album_listeners(bill_id bigint(20)) RETURNS int DETERMINISTIC
    RETURN
        (WITH RECURSIVE cte (week_id, artist_id, guild_id, album_name) AS
                            (SELECT week_id,
                                    artist_id,
                                    guild_id,
                                    album_name
                             FROM weekly_billboard_album_listeners
                             WHERE id = bill_id
                             UNION ALL
                             SELECT b.week_id,
                                    b.artist_id,
                                    b.guild_id,
                                    b.album_name
                             FROM cte t
                                      JOIN weekly_billboard_album_listeners b ON b.week_id = t.week_id - 1
                                 AND t.artist_id = b.artist_id
                                 AND t.guild_id = b.guild_id
                                 AND t.album_name = b.album_name)
         SELECT count(*)
         FROM cte);

--

CREATE FUNCTION streak_global_billboard_track(bill_id bigint(20)) RETURNS int DETERMINISTIC
    RETURN
        (WITH RECURSIVE cte (week_id, artist_id, track_name) AS
                            (SELECT week_id,
                                    artist_id,
                                    track_name
                             FROM weekly_billboard_global_listeners
                             WHERE id = bill_id
                             UNION ALL
                             SELECT b.week_id,
                                    b.artist_id,
                                    b.track_name
                             FROM cte t
                                      JOIN weekly_billboard_global_listeners b ON b.week_id = t.week_id - 1
                                 AND t.artist_id = b.artist_id
                                 AND t.track_name = b.track_name)
         SELECT count(*)
         FROM cte);


CREATE FUNCTION streak_billboard_global_track_scrobbles(bill_id bigint(20)) RETURNS int DETERMINISTIC
    RETURN
        (WITH RECURSIVE cte (week_id, artist_id, track_name) AS
                            (SELECT week_id,
                                    artist_id,
                                    track_name
                             FROM weekly_billboard_global_scrobbles
                             WHERE id = bill_id
                             UNION ALL
                             SELECT b.week_id,
                                    b.artist_id,
                                    b.track_name
                             FROM cte t
                                      JOIN weekly_billboard_global_scrobbles b ON b.week_id = t.week_id - 1
                                 AND t.artist_id = b.artist_id
                                 AND t.track_name = b.track_name)
         SELECT count(*)
         FROM cte);


CREATE FUNCTION streak_global_billboard_artist(bill_id bigint(20)) RETURNS int DETERMINISTIC
    RETURN
        (WITH RECURSIVE cte (week_id, artist_id) AS
                            (SELECT week_id,
                                    artist_id
                             FROM weekly_billboard_artist_global_listeners
                             WHERE id = bill_id
                             UNION ALL
                             SELECT b.week_id,
                                    b.artist_id
                             FROM cte t
                                      JOIN weekly_billboard_artist_global_listeners b ON b.week_id = t.week_id - 1
                                 AND t.artist_id = b.artist_id)
         SELECT count(*)
         FROM cte);


CREATE FUNCTION streak_global_billboard_artist_scrobbles(bill_id bigint(20)) RETURNS int DETERMINISTIC
    RETURN
        (WITH RECURSIVE cte (week_id, artist_id) AS
                            (SELECT week_id,
                                    artist_id
                             FROM weekly_billboard_artist_global_scrobbles
                             WHERE id = bill_id
                             UNION ALL
                             SELECT b.week_id,
                                    b.artist_id
                             FROM cte t
                                      JOIN weekly_billboard_artist_global_scrobbles b ON b.week_id = t.week_id - 1
                                 AND t.artist_id = b.artist_id
                            )
         SELECT count(*)
         FROM cte);


CREATE FUNCTION streak_global_billboard_album_scrobbles(bill_id bigint(20)) RETURNS int DETERMINISTIC
    RETURN
        (WITH RECURSIVE cte (week_id, artist_id, album_name) AS
                            (SELECT week_id,
                                    artist_id,
                                    album_name
                             FROM weekly_billboard_album_global_scrobbles
                             WHERE id = bill_id
                             UNION ALL
                             SELECT b.week_id,
                                    b.artist_id,
                                    b.album_name
                             FROM cte t
                                      JOIN weekly_billboard_album_global_scrobbles b ON b.week_id = t.week_id - 1
                                 AND t.artist_id = b.artist_id
                                 AND t.album_name = b.album_name)
         SELECT count(*)
         FROM cte);


CREATE FUNCTION streak_global_billboard_album_listeners(bill_id bigint(20)) RETURNS int DETERMINISTIC
    RETURN
        (WITH RECURSIVE cte (week_id, artist_id, album_name) AS
                            (SELECT week_id,
                                    artist_id,
                                    album_name
                             FROM weekly_billboard_album_global_listeners
                             WHERE id = bill_id
                             UNION ALL
                             SELECT b.week_id,
                                    b.artist_id,
                                    b.album_name
                             FROM cte t
                                      JOIN weekly_billboard_album_global_listeners b ON b.week_id = t.week_id - 1
                                 AND t.artist_id = b.artist_id
                                 AND t.album_name = b.album_name)
         SELECT count(*)
         FROM cte);
-- End of streak functions calculation


create table user_billboard_data_scrobbles
(
    id         BIGINT(20)                           not null AUTO_INCREMENT,
    week_id    int,
    lastfm_id  VARCHAR(45) COLLATE ascii_general_ci NOT NULL,
    artist_id  bigint(20),
    track_name varchar(400),
    album_name varchar(400),
    timestamp  TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT user_billboard_data_scrobbles_artist_id FOREIGN KEY (artist_id) REFERENCES artist (id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT user_billboard_data_scrobbles_guild_id FOREIGN KEY (lastfm_id) REFERENCES user (lastfm_id) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT user_billboard_data_scrobbles_week_id FOREIGN KEY (week_id) REFERENCES week (id) ON UPDATE CASCADE ON DELETE CASCADE,
    index lookup_user_billboard_scrobble (`lastfm_id`, week_id)
);


CREATE TABLE server_blocked
(
    discord_id BIGINT(20) NOT NULL,
    guild_id   BIGINT(20) NOT NULL,
    PRIMARY KEY (discord_id, guild_id),
    CONSTRAINT server_blocked_fk_user FOREIGN KEY (discord_id) REFERENCES user (discord_id) ON DELETE CASCADE ON UPDATE CASCADE

);



CREATE FUNCTION calculate_country(area bigint) RETURNS int
    language sql
    RETURN

        (WITH RECURSIVE area_descendants AS (
    SELECT entity0 AS parent, entity1 AS descendant, 1 AS depth
      FROM l_area_area laa
      JOIN link ON laa.link = link.id
     WHERE link.link_type = 356
       AND entity1 IN (area)
     UNION
    SELECT entity0 AS parent, descendant, (depth + 1) AS depth
      FROM l_area_area laa
      JOIN link ON laa.link = link.id
      JOIN area_descendants ON area_descendants.parent = laa.entity1
     WHERE link.link_type = 356
       AND entity0 != descendant
)
SELECT iso.code
FROM area_descendants ad
         JOIN iso_3166_1 iso ON iso.area = ad.parent);
        (WITH RECURSIVE cte (week_id, artist_id, album_name) AS
                            (SELECT week_id,
                                    artist_id,
                                    album_name
                             FROM weekly_billboard_album_global_listeners
                             WHERE id = bill_id
                             UNION ALL
                             SELECT b.week_id,
                                    b.artist_id,
                                    b.album_name
                             FROM cte t
                                      JOIN weekly_billboard_album_global_listeners b ON b.week_id = t.week_id - 1
                                 AND t.artist_id = b.artist_id
                                 AND t.album_name = b.album_name)
         SELECT count(*)
         FROM cte);
-- End of streak functions calculation