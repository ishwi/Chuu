-- do stuff
ALTER TABLE guild
    MODIFY COLUMN `override_reactions` enum ('OVERRIDE','ADD','ADD_END','EMPTY') COLLATE utf8mb4_unicode_ci DEFAULT 'EMPTY';
ALTER TABLE guild
    ADD COLUMN `color` varchar(200) COLLATE ascii_bin NULL;
ALTER TABLE user
    ADD COLUMN `color` varchar(200) COLLATE ascii_bin NULL;
CREATE INDEX guild ON user_guild (guild_id);
