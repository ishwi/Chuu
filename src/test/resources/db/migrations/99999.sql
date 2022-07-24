-- liquibase formatted sql
-- changeset ish:data-inital
insert into `user`(discord_id, lastfm_id)
values (-1, 'ishwaracoello');

insert into guild(guild_id)
values (-1);

insert into user_guild(discord_id, guild_id)
values (-1, -1);
