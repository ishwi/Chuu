-- liquibase formatted sql
-- changeset ish:noise
CREATE TABLE obscurity
(
    id        bigint PRIMARY KEY AUTO_INCREMENT      NOT NULL,
    lastfm_id varchar(45) CHARACTER SET ascii UNIQUE NOT NULL,
    score     double                                 NOT NULL,
    CONSTRAINT `obscurity_user_lastfm_id` FOREIGN KEY (`lastfm_id`) REFERENCES `user` (`lastfm_id`) ON DELETE CASCADE ON UPDATE CASCADE

);

ALTER TABLE guild
    ADD COLUMN set_on_join boolean DEFAULT TRUE;

ALTER TABLE user
    ALTER show_botted SET DEFAULT FALSE;
-- rollback drop table obscurity;
-- rollback alter table guild drop column set_on_join;
-- rollback alter table user alter show_botted set default false;
