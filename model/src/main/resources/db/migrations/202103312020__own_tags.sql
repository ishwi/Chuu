--liquibase formatted sql
--changeset ish:own-tags
ALTER TABLE user
    ADD COLUMN `own_tags` boolean NOT NULL DEFAULT FALSE;
--rollback alter table user drop column own_tags;