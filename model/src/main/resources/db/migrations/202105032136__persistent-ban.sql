create table botted
(
    id         bigint primary key auto_increment not null,
    discord_id bigint                            not null,
    unique (discord_id)
);
insert into botted(discord_id)
select discord_id
from user
where botted_account = true;


create table image_blocked
(
    id         bigint primary key auto_increment not null,
    discord_id bigint                            not null,
    unique (discord_id)
);
insert into persistent_image_blocked(discord_id)
select discord_id
from user
where role = 'IMAGE_BLOCKED';

create trigger botter
    after INSERT
    on botted
    for each row
    update user
    set botted_account = true
    where discord_id = NEW.discord_id;

create trigger imaged
    after INSERT
    on image_blocked
    for each row
    update user
    set role = 'IMAGE_BLOCKED'
    where discord_id = NEW.discord_id;


create table role_colour_server
(
    id               bigint primary key auto_increment not null,
    guild_id         bigint                            not null,
    colour           varchar(10)                       not null,
    start_breakpoint int                               not null,
    end_breakpoint   int                               not null,
    role_id          bigint unique                     not null,
    unique (guild_id, colour, start_breakpoint, end_breakpoint)
);
alter table user
    add column chart_options bigint not null default 0;
alter table randomlinks
    drop constraint randomlinks_fk_guild,
    drop column guild_id;
alter table command_logs
    add column success boolean;
alter table command_logs
    add column is_slash boolean;
