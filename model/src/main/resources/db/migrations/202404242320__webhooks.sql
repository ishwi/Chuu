--liquibase formatted sql
--changeset ish:webhooks
CREATE TABLE webhooks
(
    id             bigint(20)   NOT NULL AUTO_INCREMENT,
    webhook_id     bigint(20)   NOT NULL,
    guild_id       bigint(20)   NOT NULL,
    channel_id     bigint(20)   NOT NULL,
    url            varchar(200) NOT NULL unique,
    type           varchar(20)  not null,
    genre_releases json         null,
    check ( type = 'BANDCAMP_RELEASE' and genre_releases is not null ),
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
--rollback drop table webhooks;
