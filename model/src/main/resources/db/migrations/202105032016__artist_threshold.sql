--liquibase formatted sql
--changeset ish:artist-threshold
ALTER TABLE user
    ADD COLUMN `artist_threshold` int NOT NULL DEFAULT 0;
--rollback alter table user drop column artist_threshold;