DELETE
FROM
    artist_tags
WHERE
        artist_id IN (SELECT artist_id FROM artist_tags GROUP BY artist_id HAVING COUNT(*) > 30)
  AND   tag IN (SELECT tag FROM album_tags WHERE artist_id = artist_tags.artist_id GROUP BY tag HAVING COUNT(*) < 6);


CREATE INDEX track_name ON track (track_name);

CREATE TABLE IF NOT EXISTS `track_tags`
(
    `id`        bigint(20)   NOT NULL AUTO_INCREMENT,
    `artist_id` bigint(20)   NOT NULL,
    `track_id`  bigint(20)   NOT NULL,
    `tag`       varchar(100) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `track_id` (`artist_id`, `track_id`, `tag`),
    KEY `album_tags_fk_album_id` (`track_id`),
    CONSTRAINT `track_tags_fk_album_id` FOREIGN KEY (`track_id`) REFERENCES `track` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `track_tags_fk_artist_id` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
