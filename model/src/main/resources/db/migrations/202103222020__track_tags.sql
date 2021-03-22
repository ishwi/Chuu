delete
from artist_tags
where artist_id in (select artist_id from artist_tags group by artist_id having count(*) > 30)
  and tag in (select tag from album_tags where artist_id = artist_tags.artist_id group by tag having count(*) < 6);


create index track_name on track (track_name);

CREATE TABLE if not exists `track_tags`
(
    `id`        bigint(20)   NOT NULL AUTO_INCREMENT,
    `artist_id` bigint(20)   NOT NULL,
    `track_id`  bigint(20)   NOT NULL,
    `tag`       varchar(100) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `track_id` (`artist_id`, `track_id`, `tag`),
    KEY `album_tags_fk_album_id` (`track_id`),
    CONSTRAINT `track_tags_fk_album_id` FOREIGN KEY (`album_id`) REFERENCES `album` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `track_tags_fk_artist_id` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
