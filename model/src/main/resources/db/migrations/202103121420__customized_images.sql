CREATE TABLE artist_custom_images
(
    id        bigint(20) NOT NULL AUTO_INCREMENT,
    alt_id    bigint(20) NOT NULL REFERENCES alt_url (id) ON DELETE CASCADE ON UPDATE CASCADE,
    guild_id  bigint(20) NOT NULL,
    artist_id bigint(20) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `artist_custom_images` (`guild_id`, artist_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;



CREATE TABLE banned_cover
(
    id                bigint(20)   NOT NULL AUTO_INCREMENT,
    album_id          bigint(20) REFERENCES album (id),
    replacement_cover varchar(400) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `banned_covers_key` (`album_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
ALTER TABLE guild
    ADD COLUMN allow_covers boolean DEFAULT FALSE;
ALTER TABLE queued_url
    ADD COLUMN guild_id bigint(20) NULL;

