--liquibase formatted sql
--changeset ish:search_indexes
alter table user
    add column wk_mode int default 2;

--rollback alter table user drop column wk_mode;
