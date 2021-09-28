CREATE TABLE obscurity
(
    id        bigint PRIMARY KEY AUTO_INCREMENT           NOT NULL,
    lastfm_id varchar(45) UNIQUE COLLATE ascii_general_ci NOT NULL REFERENCES lastfm.user (lastfm_id) ON UPDATE CASCADE ON DELETE CASCADE,
    score     double                                      NOT NULL

);

ALTER TABLE guild
    ADD COLUMN set_on_join boolean DEFAULT TRUE;

ALTER TABLE user
    ALTER show_botted SET DEFAULT FALSE;
