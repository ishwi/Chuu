/*!40014 SET FOREIGN_KEY_CHECKS = 0 */;

/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `user`
(
    `lastfm_id`         varchar(45) CHARACTER SET ascii                                     NOT NULL,
    `discord_id`        bigint(20)                                                          NOT NULL,
    `last_update`       timestamp                                                           NULL                             DEFAULT CURRENT_TIMESTAMP(),
    `control_timestamp` timestamp                                                           NULL                             DEFAULT CURRENT_TIMESTAMP(),
    `role`              enum ('USER','IMAGE_BLOCKED','ADMIN') COLLATE utf8mb4_unicode_ci    NOT NULL                         DEFAULT 'USER',
    `private_update`    tinyint(1)                                                          NOT NULL                         DEFAULT 0,
    `notify_image`      tinyint(1)                                                          NOT NULL                         DEFAULT 1,
    `additional_embed`  tinyint(1)                                                          NOT NULL                         DEFAULT 0,
    `whoknows_mode`     enum ('IMAGE','LIST','PIE') COLLATE utf8mb4_unicode_ci              NOT NULL                         DEFAULT 'IMAGE',
    `chart_mode`        enum ('IMAGE','IMAGE_INFO','IMAGE_ASIDE','IMAGE_ASIDE_INFO','LIST','PIE') COLLATE utf8mb4_unicode_ci DEFAULT 'IMAGE',
    `remaining_mode`    enum ('IMAGE','IMAGE_INFO','LIST','PIE') COLLATE utf8mb4_unicode_ci NOT NULL                         DEFAULT 'IMAGE',
    `botted_account`    tinyint(1)                                                                                           DEFAULT 0,
    `default_x`         int(11)                                                                                              DEFAULT 5,
    `default_y`         int(11)                                                                                              DEFAULT 5,
    `privacy_mode`      enum ('STRICT','NORMAL','TAG','LAST_NAME','DISCORD_NAME') COLLATE utf8mb4_unicode_ci                 DEFAULT 'NORMAL',
    `notify_rating`     tinyint(1)                                                          NOT NULL                         DEFAULT 1,
    `private_lastfm`    tinyint(1)                                                          NOT NULL                         DEFAULT 0,
    `np_mode`           bigint(20)                                                          NOT NULL                         DEFAULT 1,
    `timezone`          varchar(100) COLLATE utf8mb4_unicode_ci                                                              DEFAULT 'Europe/Brussels',
    `token`             varchar(50) COLLATE utf8mb4_unicode_ci                                                               DEFAULT NULL,
    `sess`              varchar(50) COLLATE utf8mb4_unicode_ci                                                               DEFAULT NULL,
    `show_botted`       tinyint(1)                                                                                           DEFAULT 1,
    `scrobbling`        tinyint(1)                                                          NOT NULL                         DEFAULT 1,
    PRIMARY KEY (`discord_id`),
    UNIQUE KEY `lastfm_id` (`lastfm_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `artist`
(
    `id`                bigint(20)                              NOT NULL AUTO_INCREMENT,
    `name`              varchar(400) COLLATE utf8mb4_unicode_ci NOT NULL,
    `url`               varchar(400) CHARACTER SET ascii DEFAULT NULL,
    `url_status`        tinyint(1)                       DEFAULT 1,
    `correction_status` tinyint(1)                       DEFAULT 0,
    `mbid`              varchar(36) CHARACTER SET ascii  DEFAULT NULL,
    `spotify_id`        varchar(40) CHARACTER SET ascii  DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = DYNAMIC;

/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `album`
(
    `id`           bigint(20)                              NOT NULL AUTO_INCREMENT,
    `artist_id`    bigint(20)                              DEFAULT NULL,
    `album_name`   varchar(400) COLLATE utf8mb4_unicode_ci NOT NULL,
    `url`          varchar(400) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `rym_id`       bigint(20)                              DEFAULT NULL,
    `spotify_id`   varchar(40) COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    `release_year` smallint(6)                             DEFAULT NULL,
    `mbid`         varchar(36) COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `rym_id` (`rym_id`),
    UNIQUE KEY `spotify_id` (`spotify_id`),
    UNIQUE KEY `artist_id` (`artist_id`, `album_name`),
    KEY `alb_mbid_idx` (`mbid`),
    KEY `album_name_alb` (`album_name`),
    KEY `release_year` (`release_year`),
    CONSTRAINT `album_fk_artist` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `user_guild`
(
    `discord_id` bigint(20) NOT NULL,
    `guild_id`   bigint(20) NOT NULL,
    PRIMARY KEY (`discord_id`, `guild_id`),
    CONSTRAINT `user_guild_fk_user` FOREIGN KEY (`discord_id`) REFERENCES `user` (`discord_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `track`
(
    `id`           bigint(20)                              NOT NULL AUTO_INCREMENT,
    `artist_id`    bigint(20)                              DEFAULT NULL,
    `track_name`   varchar(400) COLLATE utf8mb4_unicode_ci NOT NULL,
    `url`          varchar(400) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `mbid`         varchar(36) COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    `spotify_id`   varchar(40) COLLATE utf8mb4_unicode_ci  DEFAULT NULL,
    `release_year` smallint(6)                             DEFAULT NULL,
    `duration`     int(11)                                 DEFAULT NULL,
    `album_id`     bigint(20)                              DEFAULT NULL,
    `popularity`   int(11)                                 DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `track_and_artist` (`artist_id`, `track_name`),
    KEY `spotify_id_track` (`spotify_id`),
    KEY `mbid` (`mbid`),
    KEY `track_fk_album` (`album_id`),
    CONSTRAINT `track_fk_album` FOREIGN KEY (`album_id`) REFERENCES `album` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `track_fk_artist` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `guild`
(
    `guild_id`           bigint(20)                                         NOT NULL,
    `logo`               blob                                                                                                 DEFAULT NULL,
    `prefix`             char(1) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL                                          DEFAULT '!',
    `crown_threshold`    int(11)                                            NOT NULL                                          DEFAULT 0,
    `additional_embed`   tinyint(1)                                         NOT NULL                                          DEFAULT 0,
    `whoknows_mode`      enum ('IMAGE','LIST','PIE') COLLATE utf8mb4_unicode_ci                                               DEFAULT NULL,
    `chart_mode`         enum ('IMAGE','IMAGE_INFO','IMAGE_ASIDE','IMAGE_ASIDE_INFO','LIST','PIE') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `remaining_mode`     enum ('IMAGE','IMAGE_INFO','LIST','PIE') COLLATE utf8mb4_unicode_ci                                  DEFAULT NULL,
    `np_mode`            bigint(20)                                         NOT NULL                                          DEFAULT -1,
    `disabled_response`  tinyint(1)                                         NOT NULL                                          DEFAULT 0,
    `delete_message`     tinyint(1)                                         NOT NULL                                          DEFAULT 0,
    `disabled_warning`   tinyint(1)                                         NOT NULL                                          DEFAULT 0,
    `allow_reactions`    tinyint(1)                                         NOT NULL                                          DEFAULT 1,
    `override_reactions` enum ('OVERRIDE','ADD','ADD_END','EMPTY') COLLATE utf8mb4_unicode_ci                                 DEFAULT 'EMPTY',
    PRIMARY KEY (`guild_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `randomlinks`
(
    `discord_id` bigint(20) DEFAULT NULL,
    `guild_id`   bigint(20)                              NOT NULL,
    `url`        varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
    PRIMARY KEY (`url`),
    UNIQUE KEY `unique_url_random` (`url`),
    KEY `randomlinks_fk_guild` (`guild_id`),
    KEY `randomlinks_fk_user` (`discord_id`),
    CONSTRAINT `randomlinks_fk_guild` FOREIGN KEY (`guild_id`) REFERENCES `guild` (`guild_id`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = DYNAMIC;

/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `week`
(
    `id`         int(11) NOT NULL AUTO_INCREMENT,
    `week_start` date DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

/* break*/

/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `album_crowns`
(
    `artist_id` bigint(20)                              NOT NULL,
    `discordid` bigint(20)                              NOT NULL,
    `album`     varchar(250) COLLATE utf8mb4_unicode_ci NOT NULL,
    `plays`     int(11)                                 NOT NULL,
    `guildid`   bigint(20)                              NOT NULL,
    PRIMARY KEY (`artist_id`, `album`, `guildid`),
    UNIQUE KEY `artist_id_unique` (`artist_id`, `album`, `guildid`),
    KEY `album_crown_fk_guildid` (`discordid`, `guildid`),
    CONSTRAINT `album_crown_fk_guildid` FOREIGN KEY (`discordid`, `guildid`) REFERENCES `user_guild` (`discord_id`, `guild_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `album_crowns_fk_artist_id ` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `album_rating`
(
    `id`         bigint(20)  NOT NULL AUTO_INCREMENT,
    `artist_id`  bigint(20)                      DEFAULT NULL,
    `album_id`   bigint(20)                      DEFAULT NULL,
    `discord_id` bigint(20)                      DEFAULT NULL,
    `rating`     tinyint(10) NOT NULL,
    `source`     tinyint(2)                      DEFAULT 0,
    `review`     text COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `review_unique` (`artist_id`, `album_id`, `discord_id`),
    KEY `album_rating_url_fk_discordid` (`discord_id`),
    KEY `album_rating_fk_album` (`album_id`),
    CONSTRAINT `album_rating_fk_album` FOREIGN KEY (`album_id`) REFERENCES `album` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `album_rating_fk_artist` FOREIGN KEY (`artist_id`) REFERENCES `album` (`artist_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `album_rating_url_fk_discordid` FOREIGN KEY (`discord_id`) REFERENCES `user` (`discord_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `album_tags`
(
    `id`        bigint(20)   NOT NULL AUTO_INCREMENT,
    `artist_id` bigint(20)   NOT NULL,
    `album_id`  bigint(20)   NOT NULL,
    `tag`       varchar(100) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `artist_id` (`artist_id`, `album_id`, `tag`),
    KEY `album_tags_fk_album_id` (`album_id`),
    CONSTRAINT `album_tags_fk_album_id` FOREIGN KEY (`album_id`) REFERENCES `album` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `album_tags_fk_artist_id` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `album_tracklist`
(
    `id`       bigint(20) NOT NULL AUTO_INCREMENT,
    `album_id` bigint(20) NOT NULL,
    `track_id` bigint(20) NOT NULL,
    `position` int(11)    NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `album_id` (`album_id`, `track_id`),
    KEY `album_tracklist_fk_track` (`track_id`),
    CONSTRAINT `album_tracklist_fk_album` FOREIGN KEY (`album_id`) REFERENCES `album` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `album_tracklist_fk_track` FOREIGN KEY (`track_id`) REFERENCES `track` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `alt_url`
(
    `id`         bigint(20)                       NOT NULL AUTO_INCREMENT,
    `artist_id`  bigint(20)                       NOT NULL,
    `url`        varchar(400) CHARACTER SET ascii NOT NULL,
    `discord_id` bigint(20)                                DEFAULT NULL,
    `added_date` datetime                         NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    `score`      int(11)                          NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uc_url` (`artist_id`, `url`),
    CONSTRAINT `alt_urls_fk_artist` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client = @@character_set_client */;
/*!50003 SET @saved_cs_results = @@character_set_results */;
/*!50003 SET @saved_col_connection = @@collation_connection */;
/*!50003 SET character_set_client = utf8mb4 */;
/*!50003 SET character_set_results = utf8mb4 */;
/*!50003 SET collation_connection = utf8mb4_unicode_ci */;
/*!50003 SET @saved_sql_mode = @@sql_mode */;
/*!50003 SET sql_mode = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */;
DELIMITER ;;
/*!50003 CREATE TRIGGER IF NOT EXISTS alt_url_insert
    AFTER INSERT
    ON alt_url
    FOR EACH ROW
BEGIN
    IF ((SELECT url FROM artist WHERE id = new.artist_id) IS NULL) OR
       (new.score > (SELECT MAX(alt_url.score) FROM alt_url WHERE artist_id = new.artist_id))
    THEN
        UPDATE artist SET url = new.url WHERE id = new.artist_id;
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode = @saved_sql_mode */;
/*!50003 SET character_set_client = @saved_cs_client */;
/*!50003 SET character_set_results = @saved_cs_results */;
/*!50003 SET collation_connection = @saved_col_connection */;
/*!50003 SET @saved_cs_client = @@character_set_client */;
/*!50003 SET @saved_cs_results = @@character_set_results */;
/*!50003 SET @saved_col_connection = @@collation_connection */;
/*!50003 SET character_set_client = utf8mb4 */;
/*!50003 SET character_set_results = utf8mb4 */;
/*!50003 SET collation_connection = utf8mb4_unicode_ci */;
/*!50003 SET @saved_sql_mode = @@sql_mode */;
/*!50003 SET sql_mode = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */;
DELIMITER ;;
/*!50003 CREATE TRIGGER IF NOT EXISTS alt_url_update
    AFTER UPDATE
    ON alt_url
    FOR EACH ROW
BEGIN
    DECLARE current_score int;
    DECLARE current_url varchar(400);

    SET current_score = (SELECT
                             MAX(a.score)
                         FROM
                             alt_url a
                         WHERE
                             a.artist_id = new.artist_id);
    SET current_url = (SELECT
                           a.url
                       FROM
                           alt_url a
                       WHERE
                           a.artist_id = new.artist_id
                       ORDER BY score DESC
                       LIMIT 1);
    IF ((SELECT url FROM artist b WHERE b.id = new.artist_id) = new.url) AND (new.score < current_score) THEN
        UPDATE artist SET url = current_url WHERE id = new.artist_id;
    ELSEIF (new.score >= current_score) THEN
        UPDATE artist SET url = new.url WHERE id = new.artist_id;
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode = @saved_sql_mode */;
/*!50003 SET character_set_client = @saved_cs_client */;
/*!50003 SET character_set_results = @saved_cs_results */;
/*!50003 SET collation_connection = @saved_col_connection */;
/*!50003 SET @saved_cs_client = @@character_set_client */;
/*!50003 SET @saved_cs_results = @@character_set_results */;
/*!50003 SET @saved_col_connection = @@collation_connection */;
/*!50003 SET character_set_client = utf8mb4 */;
/*!50003 SET character_set_results = utf8mb4 */;
/*!50003 SET collation_connection = utf8mb4_unicode_ci */;
/*!50003 SET @saved_sql_mode = @@sql_mode */;
/*!50003 SET sql_mode = '' */;
DELIMITER ;;
/*!50003 CREATE TRIGGER IF NOT EXISTS alt_url_delete
    AFTER DELETE
    ON alt_url
    FOR EACH ROW
BEGIN
    IF (old.url = (SELECT url FROM artist WHERE id = old.artist_id)) THEN
        UPDATE artist
        SET
            url = (SELECT url FROM alt_url WHERE alt_url.artist_id = old.artist_id ORDER BY alt_url.score DESC LIMIT 1)
        WHERE
            id = old.artist_id;
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode = @saved_sql_mode */;
/*!50003 SET character_set_client = @saved_cs_client */;
/*!50003 SET character_set_results = @saved_cs_results */;
/*!50003 SET collation_connection = @saved_col_connection */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `artist_tags`
(
    `id`        bigint(20)   NOT NULL AUTO_INCREMENT,
    `artist_id` bigint(20)   NOT NULL,
    `tag`       varchar(100) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `artist_id` (`artist_id`, `tag`),
    KEY `tag` (`tag`),
    KEY `artist_tags_artist_id` (`artist_id`),
    CONSTRAINT `artist_tags_fk_artist_id` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `audio_features`
(
    `id`               bigint(20)                                                   NOT NULL AUTO_INCREMENT,
    `spotify_id`       varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `acousticness`     float   DEFAULT NULL,
    `danceability`     float   DEFAULT NULL,
    `energy`           float   DEFAULT NULL,
    `instrumentalness` float   DEFAULT NULL,
    `key`              int(11) DEFAULT NULL,
    `liveness`         float   DEFAULT NULL,
    `loudness`         int(11) DEFAULT NULL,
    `speechiness`      float   DEFAULT NULL,
    `tempo`            int(11) DEFAULT NULL,
    `valence`          float   DEFAULT NULL,
    `time_signature`   int(11) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `spotify_id` (`spotify_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `backup`
(
    `id`           bigint(20) NOT NULL DEFAULT 0,
    `discord_id`   bigint(20) NOT NULL,
    `artist_id`    bigint(20) NOT NULL,
    `album_id`     bigint(20)          DEFAULT NULL,
    `track_name`   varchar(400)        DEFAULT NULL,
    `artist_combo` int(11)             DEFAULT NULL,
    `album_combo`  int(11)             DEFAULT NULL,
    `track_combo`  int(11)             DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `banned_artist_tags`
(
    `id`         bigint(20)   NOT NULL AUTO_INCREMENT,
    `tag`        varchar(100) NOT NULL,
    `discord_id` bigint(20) DEFAULT NULL,
    `artist_id`  bigint(20) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `banned_tags`
(
    `id`  bigint(20)   NOT NULL AUTO_INCREMENT,
    `tag` varchar(100) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `tag` (`tag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `command_guild_channel_disabled`
(
    `guild_id`     bigint(20)  NOT NULL,
    `channel_id`   bigint(20)  NOT NULL,
    `command_name` varchar(40) NOT NULL,
    `enabled`      tinyint(1)  NOT NULL,
    PRIMARY KEY (`guild_id`, `channel_id`, `command_name`),
    CONSTRAINT `command_guild_channel_disabled_fk_guild` FOREIGN KEY (`guild_id`) REFERENCES `guild` (`guild_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `command_guild_disabled`
(
    `guild_id`     bigint(20)  NOT NULL,
    `command_name` varchar(40) NOT NULL,
    PRIMARY KEY (`guild_id`, `command_name`),
    CONSTRAINT `command_guild_disabled_fk_guild` FOREIGN KEY (`guild_id`) REFERENCES `guild` (`guild_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `command_logs`
(
    `id`         bigint(20)  NOT NULL AUTO_INCREMENT,
    `discord_id` bigint(20)  NOT NULL,
    `guild_id`   bigint(20)           DEFAULT NULL,
    `command`    varchar(30) NOT NULL,
    `nanos`      bigint(20)  NOT NULL,
    `moment`     datetime    NOT NULL DEFAULT UTC_TIMESTAMP(),
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `corrected_tags`
(
    `id`         bigint(20)   NOT NULL AUTO_INCREMENT,
    `invalid`    varchar(100) NOT NULL,
    `correction` varchar(100) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `invalid` (`invalid`, `correction`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `corrections`
(
    `id`        bigint(20)                              NOT NULL AUTO_INCREMENT,
    `alias`     varchar(250) COLLATE utf8mb4_unicode_ci NOT NULL,
    `artist_id` bigint(20)                              NOT NULL,
    PRIMARY KEY (`id`),
    KEY `corrections_fk_artist` (`artist_id`),
    CONSTRAINT `corrections_fk_artist` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = DYNAMIC;

/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `log_reported`
(
    `id`       bigint(20) NOT NULL AUTO_INCREMENT,
    `reported` bigint(20) NOT NULL,
    `modded`   bigint(20) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `log_reported_fk_user` (`reported`),
    KEY `log_mod_fk_user` (`modded`),
    CONSTRAINT `log_mod_fk_user` FOREIGN KEY (`modded`) REFERENCES `user` (`discord_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `log_tags`
(
    `id`         bigint(20)   NOT NULL AUTO_INCREMENT,
    `tag`        varchar(100) NOT NULL,
    `discord_id` bigint(20) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `metadata`
(
    `url`    varchar(400) CHARACTER SET ascii NOT NULL,
    `artist` varchar(400)                     DEFAULT NULL,
    `album`  varchar(400)                     DEFAULT NULL,
    `song`   varchar(400)                     DEFAULT NULL,
    `image`  varchar(400) CHARACTER SET ascii DEFAULT NULL,
    PRIMARY KEY (`url`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `metrics`
(
    `id`    int(11) NOT NULL AUTO_INCREMENT,
    `name`  varchar(100) CHARACTER SET utf8mb4 DEFAULT NULL,
    `value` bigint(20)                         DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `past_recommendations`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT,
    `artist_id`   bigint(20) NOT NULL,
    `receiver_id` bigint(20) NOT NULL,
    `giver_id`    bigint(20) NOT NULL,
    `rec_date`    datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    `rating`      int(11)             DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `artist_id` (`artist_id`, `giver_id`, `receiver_id`),
    KEY `past_recommendations_fk_rec` (`receiver_id`),
    KEY `past_recommendations_fk_giv` (`giver_id`),
    CONSTRAINT `past_recommendations_fk_artist` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `past_recommendations_fk_giv` FOREIGN KEY (`giver_id`) REFERENCES `user` (`discord_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `past_recommendations_fk_rec` FOREIGN KEY (`receiver_id`) REFERENCES `user` (`discord_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `queued_alias`
(
    `id`         int(11)                                 NOT NULL AUTO_INCREMENT,
    `alias`      varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
    `artist_id`  bigint(20)                              NOT NULL,
    `discord_id` bigint(20)                              NOT NULL,
    `added_date` datetime                                NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    PRIMARY KEY (`id`),
    KEY `queuedalias_fk_artsit` (`artist_id`),
    KEY `queuedalias_fk_discordid` (`discord_id`),
    CONSTRAINT `queuedalias_fk_artsit` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `queuedalias_fk_discordid` FOREIGN KEY (`discord_id`) REFERENCES `user` (`discord_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  ROW_FORMAT = DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `queued_url`
(
    `id`         int(11)      NOT NULL AUTO_INCREMENT,
    `url`        varchar(400) NOT NULL,
    `artist_id`  bigint(20)   NOT NULL,
    `discord_id` bigint(20)            DEFAULT NULL,
    `added_date` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    PRIMARY KEY (`id`),
    UNIQUE KEY `artist_id` (`artist_id`, `url`),
    KEY `queued_url_fk_discordid` (`discord_id`),
    CONSTRAINT `queued_url_fk_artsit` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `queued_url_fk_discordid` FOREIGN KEY (`discord_id`) REFERENCES `user` (`discord_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `random_links_ratings`
(
    `id`         bigint(20)                                                    NOT NULL AUTO_INCREMENT,
    `url`        varchar(400) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `discord_id` bigint(20)                                                    NOT NULL,
    `rating`     int(11) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `url` (`url`, `discord_id`),
    KEY `random_links_ratings_discord` (`discord_id`),
    CONSTRAINT `random_links_ratings_discord` FOREIGN KEY (`discord_id`) REFERENCES `user` (`discord_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `random_links_ratings_url` FOREIGN KEY (`url`) REFERENCES `randomlinks` (`url`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `rate_limited`
(
    `discord_id`     bigint(20) NOT NULL,
    `queries_second` float      NOT NULL,
    PRIMARY KEY (`discord_id`),
    CONSTRAINT `rate_limiteddiscord_id` FOREIGN KEY (`discord_id`) REFERENCES `user` (`discord_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `reported`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT,
    `alt_id`      bigint(20) NOT NULL,
    `discord_id`  bigint(20) NOT NULL,
    `report_date` datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    PRIMARY KEY (`id`),
    UNIQUE KEY `alt_id` (`alt_id`, `discord_id`),
    KEY `reported_fk_user` (`discord_id`),
    CONSTRAINT `report_fk_alt_url` FOREIGN KEY (`alt_id`) REFERENCES `alt_url` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `reported_fk_user` FOREIGN KEY (`discord_id`) REFERENCES `user` (`discord_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `scrobbled_album`
(
    `artist_id`  bigint(20)                      NOT NULL,
    `album_id`   bigint(20)                      NOT NULL,
    `lastfm_id`  varchar(45) CHARACTER SET ascii NOT NULL,
    `playnumber` int(11)                         NOT NULL,
    PRIMARY KEY (`album_id`, `lastfm_id`),
    KEY `scrobbled_album_fk_artist` (`artist_id`),
    KEY `scrobbled_album_fk_user` (`lastfm_id`),
    CONSTRAINT `scrobbled_album_fk_album` FOREIGN KEY (`album_id`) REFERENCES `album` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `scrobbled_album_fk_artist` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `scrobbled_album_fk_user` FOREIGN KEY (`lastfm_id`) REFERENCES `user` (`lastfm_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `scrobbled_artist`
(
    `artist_id`  bigint(20)                      NOT NULL,
    `lastfm_id`  varchar(45) CHARACTER SET ascii NOT NULL,
    `playnumber` int(11)                         NOT NULL,
    PRIMARY KEY (`artist_id`, `lastfm_id`),
    KEY `scrobbled_artist_fk_user` (`lastfm_id`),
    KEY `scrobbled_artist_plays` (`playnumber`),
    KEY `artist_id` (`artist_id`),
    CONSTRAINT `scrobbled_artist_fk_artist` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `scrobbled_artist_fk_user` FOREIGN KEY (`lastfm_id`) REFERENCES `user` (`lastfm_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `scrobbled_track`
(
    `artist_id`  bigint(20)                      NOT NULL,
    `track_id`   bigint(20)                      NOT NULL,
    `lastfm_id`  varchar(45) CHARACTER SET ascii NOT NULL,
    `playnumber` int(11)                         NOT NULL,
    `loved`      tinyint(1) DEFAULT 0,
    PRIMARY KEY (`track_id`, `lastfm_id`),
    KEY `artist_id` (`artist_id`),
    KEY `track_id` (`track_id`),
    KEY `scrobbled_track_fk_user` (`lastfm_id`),
    CONSTRAINT `scrobbled_track_fk_track` FOREIGN KEY (`track_id`) REFERENCES `track` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `scrobbled_track_fk_user` FOREIGN KEY (`lastfm_id`) REFERENCES `user` (`lastfm_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `server_blocked`
(
    `discord_id` bigint(20) NOT NULL,
    `guild_id`   bigint(20) NOT NULL,
    PRIMARY KEY (`discord_id`, `guild_id`),
    CONSTRAINT `server_blocked_fk_user` FOREIGN KEY (`discord_id`) REFERENCES `user` (`discord_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `server_reactions`
(
    `id`       bigint(20)                                                    NOT NULL AUTO_INCREMENT,
    `guild_id` bigint(20)                                                    NOT NULL,
    `reaction` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `position` int(11)                                                       NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `temp_combos`
(
    `id`           bigint(20) NOT NULL DEFAULT 0,
    `discord_id`   bigint(20) NOT NULL,
    `artist_id`    bigint(20) NOT NULL,
    `album_id`     bigint(20)          DEFAULT NULL,
    `track_name`   varchar(400)        DEFAULT NULL,
    `artist_combo` int(11)             DEFAULT NULL,
    `album_combo`  int(11)             DEFAULT NULL,
    `track_combo`  int(11)             DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `temp_corrections`
(
    `id`                bigint(20)                                                    NOT NULL DEFAULT 0,
    `name`              varchar(400) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `url`               varchar(400) CHARACTER SET ascii                                       DEFAULT NULL,
    `url_status`        tinyint(1)                                                             DEFAULT 1,
    `correction_status` tinyint(1)                                                             DEFAULT 0,
    KEY `id` (`id`, `url`),
    KEY `url` (`url`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `temp_idss`
(
    `id` bigint(20) NOT NULL DEFAULT 0
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `temp_test`
(
    `id` bigint(20) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `top_combos`
(
    `id`           bigint(20) NOT NULL AUTO_INCREMENT,
    `discord_id`   bigint(20) NOT NULL,
    `artist_id`    bigint(20) NOT NULL,
    `album_id`     bigint(20)          DEFAULT NULL,
    `track_name`   varchar(400)        DEFAULT NULL,
    `artist_combo` int(11)             DEFAULT NULL,
    `album_combo`  int(11)             DEFAULT NULL,
    `track_combo`  int(11)             DEFAULT NULL,
    `streak_start` timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
    PRIMARY KEY (`id`),
    UNIQUE KEY `combo_uniqueness` (`discord_id`, `artist_id`, `streak_start`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_billboard_data`
(
    `id`             bigint(20)                      NOT NULL AUTO_INCREMENT,
    `week_id`        int(11)      DEFAULT NULL,
    `lastfm_id`      varchar(45) CHARACTER SET ascii NOT NULL,
    `artist_id`      bigint(20)   DEFAULT NULL,
    `track_name`     varchar(400) DEFAULT NULL,
    `scrobble_count` smallint(6)  DEFAULT NULL,
    `album_name`     varchar(400) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `user_billboard_data_artist_id` (`artist_id`),
    KEY `user_billboard_data_guild_id` (`lastfm_id`),
    KEY `track_name` (`track_name`, `artist_id`, `lastfm_id`),
    KEY `week_id` (`week_id`, `lastfm_id`),
    CONSTRAINT `user_billboard_data_guild_id` FOREIGN KEY (`lastfm_id`) REFERENCES `user` (`lastfm_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `user_billboard_data_week_id` FOREIGN KEY (`week_id`) REFERENCES `week` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_billboard_data_scrobbles`
(
    `id`         bigint(20)                      NOT NULL AUTO_INCREMENT,
    `week_id`    int(11)                                  DEFAULT NULL,
    `lastfm_id`  varchar(45) CHARACTER SET ascii NOT NULL,
    `artist_id`  bigint(20)                               DEFAULT NULL,
    `track_name` varchar(400)                             DEFAULT NULL,
    `album_name` varchar(400)                             DEFAULT NULL,
    `timestamp`  timestamp                       NOT NULL DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
    PRIMARY KEY (`id`),
    KEY `user_billboard_data_scrobbles_artist_id` (`artist_id`),
    KEY `user_billboard_data_scrobbles_week_id` (`week_id`),
    KEY `lookup_user_billboard_scrobble` (`lastfm_id`, `week_id`),
    CONSTRAINT `user_billboard_data_scrobbles_guild_id` FOREIGN KEY (`lastfm_id`) REFERENCES `user` (`lastfm_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `user_billboard_data_scrobbles_week_id` FOREIGN KEY (`week_id`) REFERENCES `week` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_info`
(
    `id`           bigint(20)                      NOT NULL AUTO_INCREMENT,
    `lastfm_id`    varchar(45) CHARACTER SET ascii NOT NULL,
    `profile_pic`  varchar(400) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `login_moment` timestamp                       NOT NULL                      DEFAULT CURRENT_TIMESTAMP() ON UPDATE CURRENT_TIMESTAMP(),
    PRIMARY KEY (`id`),
    UNIQUE KEY `lastfm_id` (`lastfm_id`),
    CONSTRAINT `user_info_fk_user` FOREIGN KEY (`lastfm_id`) REFERENCES `user` (`lastfm_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_reactions`
(
    `id`         bigint(20)                                                    NOT NULL AUTO_INCREMENT,
    `discord_id` bigint(20)                                                    NOT NULL,
    `reaction`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `position`   int(11)                                                       NOT NULL,
    PRIMARY KEY (`id`),
    KEY `user_reactions_user` (`discord_id`),
    CONSTRAINT `user_reactions_user` FOREIGN KEY (`discord_id`) REFERENCES `user` (`discord_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `vote`
(
    `alt_id`     bigint(20) NOT NULL,
    `discord_id` bigint(20) NOT NULL,
    `ispositive` tinyint(1) NOT NULL,
    `added_date` datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    PRIMARY KEY (`alt_id`, `discord_id`),
    KEY `vote_fk_user` (`discord_id`),
    CONSTRAINT `vote_fk_alt_url` FOREIGN KEY (`alt_id`) REFERENCES `alt_url` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `vote_fk_user` FOREIGN KEY (`discord_id`) REFERENCES `user` (`discord_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client = @@character_set_client */;
/*!50003 SET @saved_cs_results = @@character_set_results */;
/*!50003 SET @saved_col_connection = @@collation_connection */;
/*!50003 SET character_set_client = utf8mb4 */;
/*!50003 SET character_set_results = utf8mb4 */;
/*!50003 SET collation_connection = utf8mb4_unicode_ci */;
/*!50003 SET @saved_sql_mode = @@sql_mode */;
/*!50003 SET sql_mode = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */;
DELIMITER ;;
/*!50003 CREATE TRIGGER vote_add
    AFTER INSERT
    ON vote
    FOR EACH ROW
BEGIN
    UPDATE alt_url SET score = score + IF(new.ispositive, 1, -1) WHERE id = new.alt_id;
END */;;
DELIMITER ;
/*!50003 SET sql_mode = @saved_sql_mode */;
/*!50003 SET character_set_client = @saved_cs_client */;
/*!50003 SET character_set_results = @saved_cs_results */;
/*!50003 SET collation_connection = @saved_col_connection */;
/*!50003 SET @saved_cs_client = @@character_set_client */;
/*!50003 SET @saved_cs_results = @@character_set_results */;
/*!50003 SET @saved_col_connection = @@collation_connection */;
/*!50003 SET character_set_client = utf8mb4 */;
/*!50003 SET character_set_results = utf8mb4 */;
/*!50003 SET collation_connection = utf8mb4_unicode_ci */;
/*!50003 SET @saved_sql_mode = @@sql_mode */;
/*!50003 SET sql_mode = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */;
DELIMITER ;;
/*!50003 CREATE TRIGGER vote_update
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
END */;;
DELIMITER ;
/*!50003 SET sql_mode = @saved_sql_mode */;
/*!50003 SET character_set_client = @saved_cs_client */;
/*!50003 SET character_set_results = @saved_cs_results */;
/*!50003 SET collation_connection = @saved_col_connection */;
/*!50003 SET @saved_cs_client = @@character_set_client */;
/*!50003 SET @saved_cs_results = @@character_set_results */;
/*!50003 SET @saved_col_connection = @@collation_connection */;
/*!50003 SET character_set_client = utf8mb4 */;
/*!50003 SET character_set_results = utf8mb4 */;
/*!50003 SET collation_connection = utf8mb4_unicode_ci */;
/*!50003 SET @saved_sql_mode = @@sql_mode */;
/*!50003 SET sql_mode = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */;
DELIMITER ;;
/*!50003 CREATE TRIGGER vote_delete
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

END */;;
DELIMITER ;
/*!50003 SET sql_mode = @saved_sql_mode */;
/*!50003 SET character_set_client = @saved_cs_client */;
/*!50003 SET character_set_results = @saved_cs_results */;
/*!50003 SET collation_connection = @saved_col_connection */;

/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `weekly_billboard_album_global_listeners`
(
    `id`         bigint(20) NOT NULL AUTO_INCREMENT,
    `week_id`    int(11)                                 DEFAULT NULL,
    `artist_id`  bigint(20)                              DEFAULT NULL,
    `album_name` varchar(400) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `position`   smallint(6)                             DEFAULT NULL,
    `listeners`  int(11)                                 DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `weekly_billboard_album_global_artist_id` (`artist_id`),
    KEY `week_id` (`week_id`),
    CONSTRAINT `weekly_billboard_album_global_artist_id` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `weekly_billboard_album_global_week_id` FOREIGN KEY (`week_id`) REFERENCES `week` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `weekly_billboard_album_global_scrobbles`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT,
    `week_id`        int(11)                                 DEFAULT NULL,
    `artist_id`      bigint(20)                              DEFAULT NULL,
    `album_name`     varchar(400) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `position`       smallint(6)                             DEFAULT NULL,
    `scrobble_count` int(11)                                 DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `weekly_billboard_album_global_scrobbles_artist_id` (`artist_id`),
    KEY `week_id` (`week_id`),
    CONSTRAINT `weekly_billboard_album_global_scrobbles_artist_id` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `weekly_billboard_album_global_scrobbles_week_id` FOREIGN KEY (`week_id`) REFERENCES `week` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `weekly_billboard_album_listeners`
(
    `id`         bigint(20) NOT NULL AUTO_INCREMENT,
    `guild_id`   bigint(20)                              DEFAULT NULL,
    `week_id`    int(11)                                 DEFAULT NULL,
    `artist_id`  bigint(20)                              DEFAULT NULL,
    `album_name` varchar(400) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `position`   smallint(6)                             DEFAULT NULL,
    `listeners`  int(11)                                 DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `weekly_billboard_album_artist_id` (`artist_id`),
    KEY `weekly_billboard_album_guild_id` (`guild_id`),
    KEY `week_id` (`week_id`, `guild_id`),
    CONSTRAINT `weekly_billboard_album_artist_id` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `weekly_billboard_album_guild_id` FOREIGN KEY (`guild_id`) REFERENCES `guild` (`guild_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `weekly_billboard_album_week_id` FOREIGN KEY (`week_id`) REFERENCES `week` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `weekly_billboard_album_scrobbles`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT,
    `guild_id`       bigint(20)                              DEFAULT NULL,
    `week_id`        int(11)                                 DEFAULT NULL,
    `artist_id`      bigint(20)                              DEFAULT NULL,
    `album_name`     varchar(400) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `position`       smallint(6)                             DEFAULT NULL,
    `scrobble_count` int(11)                                 DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `weekly_billboard_album_scrobbles_artist_id` (`artist_id`),
    KEY `weekly_billboard_album_scrobbles_guild_id` (`guild_id`),
    KEY `week_id` (`week_id`, `guild_id`),
    CONSTRAINT `weekly_billboard_album_scrobbles_artist_id` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `weekly_billboard_album_scrobbles_guild_id` FOREIGN KEY (`guild_id`) REFERENCES `guild` (`guild_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `weekly_billboard_album_scrobbles_week_id` FOREIGN KEY (`week_id`) REFERENCES `week` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `weekly_billboard_artist_global_listeners`
(
    `id`        bigint(20) NOT NULL AUTO_INCREMENT,
    `week_id`   int(11)     DEFAULT NULL,
    `artist_id` bigint(20)  DEFAULT NULL,
    `position`  smallint(6) DEFAULT NULL,
    `listeners` int(11)     DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `weekly_billboard_artist_global_listeners_artist_id` (`artist_id`),
    KEY `weekly_billboard_artist_global_listeners_week_id` (`week_id`),
    CONSTRAINT `weekly_billboard_artist_global_listeners_artist_id` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `weekly_billboard_artist_global_listeners_week_id` FOREIGN KEY (`week_id`) REFERENCES `week` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `weekly_billboard_artist_global_scrobbles`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT,
    `week_id`        int(11)     DEFAULT NULL,
    `artist_id`      bigint(20)  DEFAULT NULL,
    `position`       smallint(6) DEFAULT NULL,
    `scrobble_count` int(11)     DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `weekly_billboard_artist_global_scrobbles_artist_id` (`artist_id`),
    KEY `week_id` (`week_id`),
    CONSTRAINT `weekly_billboard_artist_global_scrobbles_artist_id` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `weekly_billboard_artist_global_scrobbles_week_id` FOREIGN KEY (`week_id`) REFERENCES `week` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `weekly_billboard_artist_listeners`
(
    `id`        bigint(20) NOT NULL AUTO_INCREMENT,
    `guild_id`  bigint(20)  DEFAULT NULL,
    `week_id`   int(11)     DEFAULT NULL,
    `artist_id` bigint(20)  DEFAULT NULL,
    `position`  smallint(6) DEFAULT NULL,
    `listeners` int(11)     DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `weekly_billboard_artists_artist_id` (`artist_id`),
    KEY `weekly_billboard_artist_guild_id` (`guild_id`),
    KEY `weekly_billboard_artist_week_id` (`week_id`),
    CONSTRAINT `weekly_billboard_artist_guild_id` FOREIGN KEY (`guild_id`) REFERENCES `guild` (`guild_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `weekly_billboard_artist_week_id` FOREIGN KEY (`week_id`) REFERENCES `week` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `weekly_billboard_artists_artist_id` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `weekly_billboard_artist_scrobbles`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT,
    `guild_id`       bigint(20)  DEFAULT NULL,
    `week_id`        int(11)     DEFAULT NULL,
    `artist_id`      bigint(20)  DEFAULT NULL,
    `position`       smallint(6) DEFAULT NULL,
    `scrobble_count` int(11)     DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `weekly_billboard_artist_scrobbles_artist_id` (`artist_id`),
    KEY `weekly_billboard_artist_scrobbles_guild_id` (`guild_id`),
    KEY `week_id` (`week_id`, `guild_id`),
    CONSTRAINT `weekly_billboard_artist_scrobbles_artist_id` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `weekly_billboard_artist_scrobbles_guild_id` FOREIGN KEY (`guild_id`) REFERENCES `guild` (`guild_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `weekly_billboard_artist_scrobbles_week_id` FOREIGN KEY (`week_id`) REFERENCES `week` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `weekly_billboard_global_listeners`
(
    `id`         bigint(20) NOT NULL AUTO_INCREMENT,
    `week_id`    int(11)                                 DEFAULT NULL,
    `artist_id`  bigint(20)                              DEFAULT NULL,
    `track_name` varchar(400) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `position`   smallint(6)                             DEFAULT NULL,
    `listeners`  int(11)                                 DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `weekly_billboard_global_artist_id` (`artist_id`),
    KEY `week_id` (`week_id`),
    CONSTRAINT `weekly_billboard_global_artist_id` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `weekly_billboard_global_week_id` FOREIGN KEY (`week_id`) REFERENCES `week` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `weekly_billboard_global_scrobbles`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT,
    `week_id`        int(11)                                 DEFAULT NULL,
    `artist_id`      bigint(20)                              DEFAULT NULL,
    `track_name`     varchar(400) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `position`       smallint(6)                             DEFAULT NULL,
    `scrobble_count` int(11)                                 DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `weekly_billboard_global_scrobbles_artist_id` (`artist_id`),
    KEY `week_id` (`week_id`),
    CONSTRAINT `weekly_billboard_global_scrobbles_artist_id` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `weekly_billboard_global_scrobbles_week_id` FOREIGN KEY (`week_id`) REFERENCES `week` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `weekly_billboard_listeners`
(
    `id`         bigint(20) NOT NULL AUTO_INCREMENT,
    `guild_id`   bigint(20)                              DEFAULT NULL,
    `week_id`    int(11)                                 DEFAULT NULL,
    `artist_id`  bigint(20)                              DEFAULT NULL,
    `track_name` varchar(400) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `position`   smallint(6)                             DEFAULT NULL,
    `listeners`  int(11)                                 DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `weekly_billboard_artist_id` (`artist_id`),
    KEY `weekly_billboard_guild_id` (`guild_id`),
    KEY `week_id` (`week_id`, `guild_id`),
    CONSTRAINT `weekly_billboard_artist_id` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `weekly_billboard_guild_id` FOREIGN KEY (`guild_id`) REFERENCES `guild` (`guild_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `weekly_billboard_week_id` FOREIGN KEY (`week_id`) REFERENCES `week` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `weekly_billboard_scrobbles`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT,
    `guild_id`       bigint(20)                              DEFAULT NULL,
    `week_id`        int(11)                                 DEFAULT NULL,
    `artist_id`      bigint(20)                              DEFAULT NULL,
    `track_name`     varchar(400) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `position`       smallint(6)                             DEFAULT NULL,
    `scrobble_count` int(11)                                 DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `weekly_billboard_scrobbles_artist_id` (`artist_id`),
    KEY `weekly_billboard_scrobbles_guild_id` (`guild_id`),
    KEY `week_id` (`week_id`, `guild_id`),
    CONSTRAINT `weekly_billboard_scrobbles_artist_id` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `weekly_billboard_scrobbles_guild_id` FOREIGN KEY (`guild_id`) REFERENCES `guild` (`guild_id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `weekly_billboard_scrobbles_week_id` FOREIGN KEY (`week_id`) REFERENCES `week` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
DELIMITER ;;
CREATE FUNCTION `streak_billboard_album_listeners`(bill_id bigint(20)) RETURNS int(11)
    DETERMINISTIC
    RETURN
        (WITH
             RECURSIVE
             cte (week_id, artist_id, guild_id, album_name) AS
                 (SELECT
                      week_id,
                      artist_id,
                      guild_id,
                      album_name
                  FROM
                      weekly_billboard_album_listeners
                  WHERE
                      id = bill_id
                  UNION ALL
                  SELECT
                      b.week_id,
                      b.artist_id,
                      b.guild_id,
                      b.album_name
                  FROM
                      cte t
                          JOIN weekly_billboard_album_listeners b ON b.week_id = t.week_id - 1
                          AND t.artist_id = b.artist_id
                          AND t.guild_id = b.guild_id
                          AND t.album_name = b.album_name)
         SELECT
             COUNT(*)
         FROM
             cte) ;;
DELIMITER ;
/*!50003 SET sql_mode = @saved_sql_mode */;
/*!50003 SET character_set_client = @saved_cs_client */;
/*!50003 SET character_set_results = @saved_cs_results */;
/*!50003 SET collation_connection = @saved_col_connection */;
/*!50003 DROP FUNCTION IF EXISTS `streak_billboard_album_scrobbles` */;
/*!50003 SET @saved_cs_client = @@character_set_client */;
/*!50003 SET @saved_cs_results = @@character_set_results */;
/*!50003 SET @saved_col_connection = @@collation_connection */;
/*!50003 SET character_set_client = utf8mb4 */;
/*!50003 SET character_set_results = utf8mb4 */;
/*!50003 SET collation_connection = utf8mb4_unicode_ci */;
/*!50003 SET @saved_sql_mode = @@sql_mode */;
/*!50003 SET sql_mode = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */;
DELIMITER ;;
CREATE FUNCTION `streak_billboard_album_scrobbles`(bill_id bigint(20)) RETURNS int(11)
    DETERMINISTIC
    RETURN
        (WITH
             RECURSIVE
             cte (week_id, artist_id, guild_id, album_name) AS
                 (SELECT
                      week_id,
                      artist_id,
                      guild_id,
                      album_name
                  FROM
                      weekly_billboard_album_scrobbles
                  WHERE
                      id = bill_id
                  UNION ALL
                  SELECT
                      b.week_id,
                      b.artist_id,
                      b.guild_id,
                      b.album_name
                  FROM
                      cte t
                          JOIN weekly_billboard_album_scrobbles b ON b.week_id = t.week_id - 1
                          AND t.artist_id = b.artist_id
                          AND t.guild_id = b.guild_id
                          AND t.album_name = b.album_name)
         SELECT
             COUNT(*)
         FROM
             cte) ;;
DELIMITER ;
/*!50003 SET sql_mode = @saved_sql_mode */;
/*!50003 SET character_set_client = @saved_cs_client */;
/*!50003 SET character_set_results = @saved_cs_results */;
/*!50003 SET collation_connection = @saved_col_connection */;
/*!50003 DROP FUNCTION IF EXISTS `streak_billboard_artist` */;
/*!50003 SET @saved_cs_client = @@character_set_client */;
/*!50003 SET @saved_cs_results = @@character_set_results */;
/*!50003 SET @saved_col_connection = @@collation_connection */;
/*!50003 SET character_set_client = utf8mb4 */;
/*!50003 SET character_set_results = utf8mb4 */;
/*!50003 SET collation_connection = utf8mb4_unicode_ci */;
/*!50003 SET @saved_sql_mode = @@sql_mode */;
/*!50003 SET sql_mode = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */;
DELIMITER ;;
CREATE FUNCTION `streak_billboard_artist`(bill_id bigint(20)) RETURNS int(11)
    DETERMINISTIC
    RETURN
        (WITH
             RECURSIVE
             cte (week_id, artist_id, guild_id) AS
                 (SELECT
                      week_id,
                      artist_id,
                      guild_id
                  FROM
                      weekly_billboard_artist_listeners
                  WHERE
                      id = bill_id
                  UNION ALL
                  SELECT
                      b.week_id,
                      b.artist_id,
                      b.guild_id
                  FROM
                      cte t
                          JOIN weekly_billboard_artist_listeners b ON b.week_id = t.week_id - 1
                          AND t.artist_id = b.artist_id
                          AND t.guild_id = b.guild_id)
         SELECT
             COUNT(*)
         FROM
             cte) ;;
DELIMITER ;
/*!50003 SET sql_mode = @saved_sql_mode */;
/*!50003 SET character_set_client = @saved_cs_client */;
/*!50003 SET character_set_results = @saved_cs_results */;
/*!50003 SET collation_connection = @saved_col_connection */;
/*!50003 DROP FUNCTION IF EXISTS `streak_billboard_artist_scrobbles` */;
/*!50003 SET @saved_cs_client = @@character_set_client */;
/*!50003 SET @saved_cs_results = @@character_set_results */;
/*!50003 SET @saved_col_connection = @@collation_connection */;
/*!50003 SET character_set_client = utf8mb4 */;
/*!50003 SET character_set_results = utf8mb4 */;
/*!50003 SET collation_connection = utf8mb4_unicode_ci */;
/*!50003 SET @saved_sql_mode = @@sql_mode */;
/*!50003 SET sql_mode = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */;
DELIMITER ;;
CREATE FUNCTION `streak_billboard_artist_scrobbles`(bill_id bigint(20)) RETURNS int(11)
    DETERMINISTIC
    RETURN
        (WITH
             RECURSIVE
             cte (week_id, artist_id, guild_id) AS
                 (SELECT
                      week_id,
                      artist_id,
                      guild_id
                  FROM
                      weekly_billboard_artist_scrobbles
                  WHERE
                      id = bill_id
                  UNION ALL
                  SELECT
                      b.week_id,
                      b.artist_id,
                      b.guild_id
                  FROM
                      cte t
                          JOIN weekly_billboard_artist_scrobbles b ON b.week_id = t.week_id - 1
                          AND t.artist_id = b.artist_id
                          AND t.guild_id = b.guild_id)
         SELECT
             COUNT(*)
         FROM
             cte) ;;
DELIMITER ;
/*!50003 SET sql_mode = @saved_sql_mode */;
/*!50003 SET character_set_client = @saved_cs_client */;
/*!50003 SET character_set_results = @saved_cs_results */;
/*!50003 SET collation_connection = @saved_col_connection */;
/*!50003 DROP FUNCTION IF EXISTS `streak_billboard_global_track_scrobbles` */;
/*!50003 SET @saved_cs_client = @@character_set_client */;
/*!50003 SET @saved_cs_results = @@character_set_results */;
/*!50003 SET @saved_col_connection = @@collation_connection */;
/*!50003 SET character_set_client = utf8mb4 */;
/*!50003 SET character_set_results = utf8mb4 */;
/*!50003 SET collation_connection = utf8mb4_unicode_ci */;
/*!50003 SET @saved_sql_mode = @@sql_mode */;
/*!50003 SET sql_mode = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */;
DELIMITER ;;
CREATE FUNCTION `streak_billboard_global_track_scrobbles`(bill_id bigint(20)) RETURNS int(11)
    DETERMINISTIC
    RETURN
        (WITH
             RECURSIVE
             cte (week_id, artist_id, track_name) AS
                 (SELECT
                      week_id,
                      artist_id,
                      track_name
                  FROM
                      weekly_billboard_global_scrobbles
                  WHERE
                      id = bill_id
                  UNION ALL
                  SELECT
                      b.week_id,
                      b.artist_id,
                      b.track_name
                  FROM
                      cte t
                          JOIN weekly_billboard_global_scrobbles b ON b.week_id = t.week_id - 1
                          AND t.artist_id = b.artist_id
                          AND t.track_name = b.track_name)
         SELECT
             COUNT(*)
         FROM
             cte) ;;
DELIMITER ;
/*!50003 SET sql_mode = @saved_sql_mode */;
/*!50003 SET character_set_client = @saved_cs_client */;
/*!50003 SET character_set_results = @saved_cs_results */;
/*!50003 SET collation_connection = @saved_col_connection */;
/*!50003 DROP FUNCTION IF EXISTS `streak_billboard_track` */;
/*!50003 SET @saved_cs_client = @@character_set_client */;
/*!50003 SET @saved_cs_results = @@character_set_results */;
/*!50003 SET @saved_col_connection = @@collation_connection */;
/*!50003 SET character_set_client = utf8mb4 */;
/*!50003 SET character_set_results = utf8mb4 */;
/*!50003 SET collation_connection = utf8mb4_unicode_ci */;
/*!50003 SET @saved_sql_mode = @@sql_mode */;
/*!50003 SET sql_mode = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */;
DELIMITER ;;
CREATE FUNCTION `streak_billboard_track`(bill_id bigint(20)) RETURNS int(11)
    DETERMINISTIC
    RETURN
        (WITH
             RECURSIVE
             cte (week_id, artist_id, guild_id, track_name) AS
                 (SELECT
                      week_id,
                      artist_id,
                      guild_id,
                      track_name
                  FROM
                      weekly_billboard_listeners
                  WHERE
                      id = bill_id
                  UNION ALL
                  SELECT
                      b.week_id,
                      b.artist_id,
                      b.guild_id,
                      b.track_name
                  FROM
                      cte t
                          JOIN weekly_billboard_listeners b ON b.week_id = t.week_id - 1
                          AND t.artist_id = b.artist_id
                          AND t.guild_id = b.guild_id
                          AND t.track_name = b.track_name)
         SELECT
             COUNT(*)
         FROM
             cte) ;;
DELIMITER ;
/*!50003 SET sql_mode = @saved_sql_mode */;
/*!50003 SET character_set_client = @saved_cs_client */;
/*!50003 SET character_set_results = @saved_cs_results */;
/*!50003 SET collation_connection = @saved_col_connection */;
/*!50003 DROP FUNCTION IF EXISTS `streak_billboard_track_scrobbles` */;
/*!50003 SET @saved_cs_client = @@character_set_client */;
/*!50003 SET @saved_cs_results = @@character_set_results */;
/*!50003 SET @saved_col_connection = @@collation_connection */;
/*!50003 SET character_set_client = utf8mb4 */;
/*!50003 SET character_set_results = utf8mb4 */;
/*!50003 SET collation_connection = utf8mb4_unicode_ci */;
/*!50003 SET @saved_sql_mode = @@sql_mode */;
/*!50003 SET sql_mode = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */;
DELIMITER ;;
CREATE FUNCTION `streak_billboard_track_scrobbles`(bill_id bigint(20)) RETURNS int(11)
    DETERMINISTIC
    RETURN
        (WITH
             RECURSIVE
             cte (week_id, artist_id, guild_id, track_name) AS
                 (SELECT
                      week_id,
                      artist_id,
                      guild_id,
                      track_name
                  FROM
                      weekly_billboard_scrobbles
                  WHERE
                      id = bill_id
                  UNION ALL
                  SELECT
                      b.week_id,
                      b.artist_id,
                      b.guild_id,
                      b.track_name
                  FROM
                      cte t
                          JOIN weekly_billboard_scrobbles b ON b.week_id = t.week_id - 1
                          AND t.artist_id = b.artist_id
                          AND t.guild_id = b.guild_id
                          AND t.track_name = b.track_name)
         SELECT
             COUNT(*)
         FROM
             cte) ;;
DELIMITER ;
/*!50003 SET sql_mode = @saved_sql_mode */;
/*!50003 SET character_set_client = @saved_cs_client */;
/*!50003 SET character_set_results = @saved_cs_results */;
/*!50003 SET collation_connection = @saved_col_connection */;
/*!50003 DROP FUNCTION IF EXISTS `streak_global_billboard_album_listeners` */;
/*!50003 SET @saved_cs_client = @@character_set_client */;
/*!50003 SET @saved_cs_results = @@character_set_results */;
/*!50003 SET @saved_col_connection = @@collation_connection */;
/*!50003 SET character_set_client = utf8mb4 */;
/*!50003 SET character_set_results = utf8mb4 */;
/*!50003 SET collation_connection = utf8mb4_unicode_ci */;
/*!50003 SET @saved_sql_mode = @@sql_mode */;
/*!50003 SET sql_mode = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */;
DELIMITER ;;
CREATE FUNCTION `streak_global_billboard_album_listeners`(bill_id bigint(20)) RETURNS int(11)
    DETERMINISTIC
    RETURN
        (WITH
             RECURSIVE
             cte (week_id, artist_id, album_name) AS
                 (SELECT
                      week_id,
                      artist_id,
                      album_name
                  FROM
                      weekly_billboard_album_global_listeners
                  WHERE
                      id = bill_id
                  UNION ALL
                  SELECT
                      b.week_id,
                      b.artist_id,
                      b.album_name
                  FROM
                      cte t
                          JOIN weekly_billboard_album_global_listeners b ON b.week_id = t.week_id - 1
                          AND t.artist_id = b.artist_id
                          AND t.album_name = b.album_name)
         SELECT
             COUNT(*)
         FROM
             cte) ;;
DELIMITER ;
/*!50003 SET sql_mode = @saved_sql_mode */;
/*!50003 SET character_set_client = @saved_cs_client */;
/*!50003 SET character_set_results = @saved_cs_results */;
/*!50003 SET collation_connection = @saved_col_connection */;
/*!50003 DROP FUNCTION IF EXISTS `streak_global_billboard_album_scrobbles` */;
/*!50003 SET @saved_cs_client = @@character_set_client */;
/*!50003 SET @saved_cs_results = @@character_set_results */;
/*!50003 SET @saved_col_connection = @@collation_connection */;
/*!50003 SET character_set_client = utf8mb4 */;
/*!50003 SET character_set_results = utf8mb4 */;
/*!50003 SET collation_connection = utf8mb4_unicode_ci */;
/*!50003 SET @saved_sql_mode = @@sql_mode */;
/*!50003 SET sql_mode = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */;
DELIMITER ;;
CREATE FUNCTION `streak_global_billboard_album_scrobbles`(bill_id bigint(20)) RETURNS int(11)
    DETERMINISTIC
    RETURN
        (WITH
             RECURSIVE
             cte (week_id, artist_id, album_name) AS
                 (SELECT
                      week_id,
                      artist_id,
                      album_name
                  FROM
                      weekly_billboard_album_global_scrobbles
                  WHERE
                      id = bill_id
                  UNION ALL
                  SELECT
                      b.week_id,
                      b.artist_id,
                      b.album_name
                  FROM
                      cte t
                          JOIN weekly_billboard_album_global_scrobbles b ON b.week_id = t.week_id - 1
                          AND t.artist_id = b.artist_id
                          AND t.album_name = b.album_name)
         SELECT
             COUNT(*)
         FROM
             cte) ;;
DELIMITER ;
/*!50003 SET sql_mode = @saved_sql_mode */;
/*!50003 SET character_set_client = @saved_cs_client */;
/*!50003 SET character_set_results = @saved_cs_results */;
/*!50003 SET collation_connection = @saved_col_connection */;
/*!50003 DROP FUNCTION IF EXISTS `streak_global_billboard_artist` */;
/*!50003 SET @saved_cs_client = @@character_set_client */;
/*!50003 SET @saved_cs_results = @@character_set_results */;
/*!50003 SET @saved_col_connection = @@collation_connection */;
/*!50003 SET character_set_client = utf8mb4 */;
/*!50003 SET character_set_results = utf8mb4 */;
/*!50003 SET collation_connection = utf8mb4_unicode_ci */;
/*!50003 SET @saved_sql_mode = @@sql_mode */;
/*!50003 SET sql_mode = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */;
DELIMITER ;;
CREATE FUNCTION `streak_global_billboard_artist`(bill_id bigint(20)) RETURNS int(11)
    DETERMINISTIC
    RETURN
        (WITH
             RECURSIVE
             cte (week_id, artist_id) AS
                 (SELECT
                      week_id,
                      artist_id
                  FROM
                      weekly_billboard_artist_global_listeners
                  WHERE
                      id = bill_id
                  UNION ALL
                  SELECT
                      b.week_id,
                      b.artist_id
                  FROM
                      cte t
                          JOIN weekly_billboard_artist_global_listeners b ON b.week_id = t.week_id - 1
                          AND t.artist_id = b.artist_id)
         SELECT
             COUNT(*)
         FROM
             cte) ;;
DELIMITER ;
/*!50003 SET sql_mode = @saved_sql_mode */;
/*!50003 SET character_set_client = @saved_cs_client */;
/*!50003 SET character_set_results = @saved_cs_results */;
/*!50003 SET collation_connection = @saved_col_connection */;
/*!50003 DROP FUNCTION IF EXISTS `streak_global_billboard_artist_scrobbles` */;
/*!50003 SET @saved_cs_client = @@character_set_client */;
/*!50003 SET @saved_cs_results = @@character_set_results */;
/*!50003 SET @saved_col_connection = @@collation_connection */;
/*!50003 SET character_set_client = utf8mb4 */;
/*!50003 SET character_set_results = utf8mb4 */;
/*!50003 SET collation_connection = utf8mb4_unicode_ci */;
/*!50003 SET @saved_sql_mode = @@sql_mode */;
/*!50003 SET sql_mode = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */;
DELIMITER ;;
CREATE FUNCTION `streak_global_billboard_artist_scrobbles`(bill_id bigint(20)) RETURNS int(11)
    DETERMINISTIC
    RETURN
        (WITH
             RECURSIVE
             cte (week_id, artist_id) AS
                 (SELECT
                      week_id,
                      artist_id
                  FROM
                      weekly_billboard_artist_global_scrobbles
                  WHERE
                      id = bill_id
                  UNION ALL
                  SELECT
                      b.week_id,
                      b.artist_id
                  FROM
                      cte t
                          JOIN weekly_billboard_artist_global_scrobbles b ON b.week_id = t.week_id - 1
                          AND t.artist_id = b.artist_id
                 )
         SELECT
             COUNT(*)
         FROM
             cte) ;;
DELIMITER ;
/*!50003 SET sql_mode = @saved_sql_mode */;
/*!50003 SET character_set_client = @saved_cs_client */;
/*!50003 SET character_set_results = @saved_cs_results */;
/*!50003 SET collation_connection = @saved_col_connection */;
/*!50003 DROP FUNCTION IF EXISTS `streak_global_billboard_track` */;
/*!50003 SET @saved_cs_client = @@character_set_client */;
/*!50003 SET @saved_cs_results = @@character_set_results */;
/*!50003 SET @saved_col_connection = @@collation_connection */;
/*!50003 SET character_set_client = utf8mb4 */;
/*!50003 SET character_set_results = utf8mb4 */;
/*!50003 SET collation_connection = utf8mb4_unicode_ci */;
/*!50003 SET @saved_sql_mode = @@sql_mode */;
/*!50003 SET sql_mode = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */;
DELIMITER ;;
CREATE FUNCTION `streak_global_billboard_track`(bill_id bigint(20)) RETURNS int(11)
    DETERMINISTIC
    RETURN
        (WITH
             RECURSIVE
             cte (week_id, artist_id, track_name) AS
                 (SELECT
                      week_id,
                      artist_id,
                      track_name
                  FROM
                      weekly_billboard_global_listeners
                  WHERE
                      id = bill_id
                  UNION ALL
                  SELECT
                      b.week_id,
                      b.artist_id,
                      b.track_name
                  FROM
                      cte t
                          JOIN weekly_billboard_global_listeners b ON b.week_id = t.week_id - 1
                          AND t.artist_id = b.artist_id
                          AND t.track_name = b.track_name)
         SELECT
             COUNT(*)
         FROM
             cte) ;;
DELIMITER ;
/*!50003 SET sql_mode = @saved_sql_mode */;
/*!50003 SET character_set_client = @saved_cs_client */;
/*!50003 SET character_set_results = @saved_cs_results */;
/*!50003 SET collation_connection = @saved_col_connection */;
/*!40103 SET TIME_ZONE = "+00:00" */;

-- /*!40101 SET CHARACTER_SET_CLIENT = @OLD_CHARACTER_SET_CLIENT */;
-- /*!40101 SET CHARACTER_SET_RESULTS = @OLD_CHARACTER_SET_RESULTS */;
-- /*!40101 SET COLLATION_CONNECTION = @OLD_COLLATION_CONNECTION */;
-- /*!40111 SET SQL_NOTES = @OLD_SQL_NOTES */;

-- Dump completed on 2021-01-25 22:40:27
