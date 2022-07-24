-- liquibase formatted sql
-- changeset ish:friends
create table friends
(
    `id`         bigint(20) NOT NULL PRIMARY KEY AUTO_INCREMENT,
    first_user   bigint(20) not null REFERENCES `user` (discord_id) ON DELETE CASCADE ON UPDATE CASCADE,
    second_user  bigint(20) not null REFERENCES `user` (discord_id) ON DELETE CASCADE ON UPDATE CASCADE,
    created_date timestamp  not null default now(),
    status       enum ('PENDING_FIRST','PENDING_SECOND','ACCEPTED'),
    UNIQUE (first_user, second_user)
);
-- rollback drop table friends;
