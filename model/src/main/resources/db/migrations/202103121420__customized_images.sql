CREATE TABLE artist_custom_images
(
    id        bigint(20) NOT NULL AUTO_INCREMENT,
    alt_id    bigint(20) not null references alt_url (id) on delete cascade on update cascade,
    guild_id  bigint(20) not null,
    artist_id bigint(20) not null,
    PRIMARY KEY (`id`),
    KEY `artist_custom_images` (`guild_id`, artist_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;



CREATE TABLE banned_cover
(
    id                bigint(20)   NOT NULL AUTO_INCREMENT,
    album_id          bigint(20) references album (id),
    replacement_cover varchar(400) not null,
    PRIMARY KEY (`id`),
    KEY `banned_covers_key` (`album_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
alter table guild
    add column allow_covers boolean default false;
alter table queued_url
    add column guild_id bigint(20) null;

