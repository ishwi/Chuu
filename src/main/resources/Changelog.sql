alter table user
    add column private_update TINYINT(1) NOT NULL DEFAULT FALSE;
alter table user
    add column notify_image TINYINT(1) NOT NULL DEFAULT TRUE;
alter table user
    add column additional_embed TINYINT(1) NOT NULL DEFAULT false;
alter table guild
    add column additional_embed TINYINT(1) NOT NULL DEFAULT false;

--
alter table user
    add column whoknows_mode ENUM ('IMAGE','LIST','PIE') NOT NULL DEFAULT "IMAGE";
alter table guild
    add column whoknows_mode ENUM ('IMAGE','LIST','PIE');

alter table user
    add column chart_mode ENUM ('IMAGE','IMAGE_INFO','LIST','PIE') NULL DEFAULT "IMAGE";
alter table guild
    add column chart_mode ENUM ('IMAGE','IMAGE_INFO','LIST','PIE') NULL;

update user
set chart_mode = IF(additional_embed, "IMAGE_INFO", "IMAGE");
update guild
set chart_mode = IF(additional_embed, "IMAGE_INFO", NULL);

alter table user
    drop column additional_embed;
alter table guild
    drop column additional_embed;

alter table user
    add column remaining_mode ENUM ('IMAGE','IMAGE_INFO','LIST','PIE') NOT NULL DEFAULT "IMAGE";
alter table guild
    add column remaining_mode ENUM ('IMAGE','IMAGE_INFO','LIST','PIE') NULL;
--

alter table user
    MODIFY column chart_mode ENUM ('IMAGE','IMAGE_INFO','IMAGE_ASIDE','IMAGE_ASIDE_INFO','LIST','PIE') NULL DEFAULT "IMAGE";
alter table guild
    MODIFY column chart_mode ENUM ('IMAGE','IMAGE_INFO','IMAGE_ASIDE','IMAGE_ASIDE_INFO','LIST','PIE') NULL;


alter table user
    add column botted_account boolean default false;

alter table user
    add column default_x int default 5;
alter table user
    add column default_y int default 5;

alter table user
    add column privacy_mode ENUM ('STRICT', 'NORMAL', 'TAG', 'LAST_NAME', 'DISCORD_NAME') DEFAULT 'NORMAL';

ALTER TABLE album
    DROP COLUMN MBID;

ALTER TABLE album
    ADD COLUMN mbid VARCHAR(36) unique;


alter table weekly_billboard_artist_global_listeners
    drop column scrobble_count;
alter table weekly_billboard_artist_global_listeners
    add column listeners int;

alter table user
    add column notify_rating boolean NOT NULL DEFAULT true;

alter table user
    add column private_lastfm boolean NOT NULL DEFAULT false;

alter table artist
    add column `mbid` varchar(36) character set ascii DEFAULT NULL;

alter table artist
    add column `spotify_id` varchar(40) character set ascii DEFAULT NULL;


-- 2020/09/14
-- Might need these queries to delete duplicated rows;
-- create table temp_combos  as (select *  from top_combos a where (artist_combo,discord_id,artist_id) not in (select max(artist_combo),discord_id,artist_id from top_combos b  where b.discord_id = a.discord_id and b.artist_id = a.artist_id));
-- delete from top_combos where  exists (select 1 from temp_combos  where id = top_combos.id) ;


-- delete from top_combos
-- where id not in
--       (
--           select * from
--               (
--                   select max(id)
--                   from top_combos
--                   group by artist_id,discord_id
--               ) tmp
alter table top_combos
    add column
        streak_start TIMESTAMP not NULL;

alter table top_combos
    add unique index combo_uniqueness (discord_id, artist_id, streak_start);

