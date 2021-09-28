CREATE TABLE rejected
(
    `id`            bigint(20)   NOT NULL AUTO_INCREMENT,
    `url`           varchar(400) NOT NULL,
    `artist_id`     bigint(20)   NOT NULL,
    `discord_id`    bigint(20)            DEFAULT NULL,
    `rejected_date` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    PRIMARY KEY (`id`),
    KEY `rejected_url_fk_discordid` (`discord_id`),
    CONSTRAINT `rejected_url_fk_artsit` FOREIGN KEY (`artist_id`) REFERENCES `artist` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE strike
(
    `id`            bigint(20) NOT NULL AUTO_INCREMENT,
    `rejected_id`   bigint(20) NOT NULL,
    `discord_id`    bigint(20)          DEFAULT NULL,
    `rejected_date` datetime   NOT NULL DEFAULT CURRENT_TIMESTAMP(),
    PRIMARY KEY (`id`),
    KEY `strike_fk_discordid` (`discord_id`),
    CONSTRAINT `strike_fk_rejected` FOREIGN KEY (`rejected_id`) REFERENCES `rejected` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE INDEX alt_url_discord_id ON alt_url (discord_id);
