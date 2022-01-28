--liquibase formatted sql
--changeset ish:search_indexes
alter table artist
    add column play_ranking int default -1;

DROP PROCEDURE IF EXISTS update_artist_ranking;

DELIMITER $$
CREATE or replace PROCEDURE update_artist_ranking(IN batch_size int)
BEGIN


    declare start_id int default 0;
    declare end_id int default 0;


    DECLARE batch_end_id INT DEFAULT start_id + batch_size;

    DECLARE batch_start_id INT DEFAULT start_id;

    DECLARE loop_counter INT DEFAULT 0;


    select max(id) into end_id from artist;

    WHILE batch_end_id <= end_id
        DO

            UPDATE artist
            SET play_ranking = (select sum(playnumber) from scrobbled_artist where artist_id = artist.id)
            WHERE id BETWEEN batch_start_id and batch_end_id;
            SET batch_start_id = batch_start_id + batch_size;
            SET batch_end_id = batch_end_id + batch_size;
            SET loop_counter = loop_counter + 1;
        END WHILE;


END$$
DELIMITER ;


DELIMITER $$
CREATE or replace PROCEDURE update_artist_ranking_not_set(IN batch_size int)
BEGIN


    declare start_id int default 0;
    declare end_id int default 0;


    DECLARE batch_end_id INT DEFAULT start_id + batch_size;

    DECLARE batch_start_id INT DEFAULT start_id;

    DECLARE loop_counter INT DEFAULT 0;


    select max(id) into end_id from artist;

    WHILE batch_end_id <= end_id
        DO

            UPDATE artist
            SET play_ranking = (select sum(playnumber) from scrobbled_artist where artist_id = artist.id)
            WHERE id BETWEEN batch_start_id and batch_end_id
              and artist.play_ranking = -1;

            SET batch_start_id = batch_start_id + batch_size;
            SET batch_end_id = batch_end_id + batch_size;
            SET loop_counter = loop_counter + 1;
        END WHILE;


END$$
DELIMITER ;

--
rollback drop procedure update_artist_ranking_not_set;
--
rollback drop procedure update_artist_ranking;
--
rollback alter table artist drop column play_ranking;
