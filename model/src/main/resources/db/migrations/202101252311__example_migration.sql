-- do stuff
alter table guild
    modify column `override_reactions` enum ('OVERRIDE','ADD','ADD_END','EMPTY') COLLATE utf8mb4_unicode_ci DEFAULT 'EMPTY';
alter table guild
    add column `color` varchar(200) COLLATE ascii_bin null;
alter table user
    add column `color` varchar(200) COLLATE ascii_bin null;
create index guild on user_guild (guild_id);
