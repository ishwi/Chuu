CREATE TABLE every_noise_genre
(
    id       bigint PRIMARY KEY AUTO_INCREMENT       NOT NULL,
    genre    varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
    playlist varchar(40) COLLATE ascii_general_ci    NOT NULL,
    UNIQUE (genre),
    FULLTEXT idx_ft (genre)
) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE every_noise_release
(
    id        bigint PRIMARY KEY AUTO_INCREMENT       NOT NULL,
    week      date                                    NOT NULL,
    artist    varchar(400) COLLATE utf8mb4_unicode_ci NOT NULL,
    `release` varchar(400) COLLATE utf8mb4_unicode_ci NOT NULL,
    uri       varchar(40) COLLATE ascii_general_ci,
    UNIQUE (artist, release)
) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


CREATE TABLE every_noise_release_genre
(
    genre_id   bigint REFERENCES every_noise_genre (id),
    release_id BIGINT REFERENCES every_noise_release (id),
    PRIMARY KEY (genre_id, release_id)
);
ALTER TABLE every_noise_genre
    ADD FULLTEXT idx_ft (genre);

ALTER TABLE every_noise_genre
    ADD COLUMN meta varchar(40) AS (soundex(genre)) PERSISTENT;



CREATE FUNCTION `soundex_match`(
    needle varchar(128), haystack text, splitchar varchar(1))
    RETURNS tinyint(4)
    DETERMINISTIC
BEGIN
    DECLARE spacepos int;
    DECLARE searchlen int DEFAULT 0;
    DECLARE curword varchar(128) DEFAULT '';
    DECLARE tempstr text DEFAULT haystack;
    DECLARE tmp text DEFAULT '';
    DECLARE soundx1 varchar(64) DEFAULT '';
    DECLARE soundx2 varchar(64) DEFAULT '';

    SET searchlen = length(haystack);
    SET spacepos = locate(splitchar, tempstr);
    SET soundx1 = soundex(needle);

    WHILE searchlen > 0
        DO
            IF spacepos = 0 THEN
                SET tmp = tempstr;
                SELECT soundex(tmp) INTO soundx2;
                IF soundx1 = soundx2 THEN
                    RETURN 1;
                ELSE
                    RETURN 0;
                END IF;
            ELSE
                SET tmp = substr(tempstr, 1, spacepos - 1);
                SET soundx2 = soundex(tmp);
                IF soundx1 = soundx2 THEN
                    RETURN 1;
                END IF;

                SET tempstr = substr(tempstr, spacepos + 1);
                SET searchlen = length(tempstr);
            END IF;

            SET spacepos = locate(splitchar, tempstr);
        END WHILE;

    RETURN 0;
END;
CREATE FUNCTION `soundex_match_all`(needle varchar(128), haystack text, splitchar varchar(1)) RETURNS tinyint(4)

BEGIN
    /* find the first instance of the splitting character */
    DECLARE comma INT DEFAULT 0;
    DECLARE word TEXT;

    SET comma = LOCATE(splitchar, needle);
    SET word = TRIM(needle);

    IF LENGTH(haystack) = 0 THEN
        RETURN 0;
    ELSEIF comma = 0 THEN
        /* one word search term */
        RETURN soundex_match(word, haystack, splitchar);
    END IF;

    SET word = trim(substr(needle, 1, comma));

    /* Insert each split variable into the word variable */
    REPEAT
        IF soundex_match(word, haystack, splitchar) = 0 THEN
            RETURN 0;
        END IF;

        /* get the next word */
        SET needle = trim(substr(needle, comma));
        SET comma = LOCATE(splitchar, needle);
        IF comma = 0 THEN
            /* last word */
            RETURN soundex_match(needle, haystack, splitchar);
        END IF;

        SET word = trim(substr(needle, 1, comma));
    UNTIL length(word) = 0
        END REPEAT;

    RETURN 0;
END;



-- This SQL implements the Double Metaphone algorythm (c) 1998, 1999 by Lawrence Philips
-- it was translated to Python, and then to SQL from the C source written by Kevin Atkinson (http://aspell.net/metaphone/)
-- By Andrew Collins (atomodo.com) - Feb, 2007 who claims no rights to this work
-- github.com/AtomBoy/double-metaphone
-- Tested with MySQL 5.1 on Ubuntu 6.01 and Ubuntu 10.4
-- Updated Nov 27, 2007 to fix a bug in the 'CC' section
-- Updated Jun 01, 2010 to fix a bug in the 'Z' section - thanks Nils Johnsson!
-- Updated Jun 25, 2010 to fix 16 signifigant bugs - thanks again Nils Johnsson for a spectacular
--   bug squashing effort. There were many cases where this function wouldn't give the same output
--   as the original C source that were fixed by his careful attention and excellent communication.
DELIMITER $$

DROP FUNCTION IF EXISTS `dm` $$

CREATE FUNCTION `dm`(st VARCHAR(55)) RETURNS varchar(128) CHARSET utf8
    DETERMINISTIC

BEGIN
    DECLARE length, first, last, pos, prevpos, is_slavo_germanic SMALLINT;
    DECLARE pri, sec VARCHAR(45) DEFAULT '';
    DECLARE ch CHAR(1);
    -- returns the double metaphone code OR codes for given string
    -- if there is a secondary dm it is separated with a semicolon
    -- there are no checks done on the input string, but it should be a single word OR name.
    --  st is short for string. I usually prefer descriptive over short, but this var is used a lot!
    SET first = 3;
    SET length = CHAR_LENGTH(st);
    SET last = first + length - 1;
    SET st = CONCAT(REPEAT('-', first - 1), UCASE(st), REPEAT(' ', 5)); --  pad st so we can index beyond the begining AND end of the input string
    SET is_slavo_germanic = (st LIKE '%W%' OR st LIKE '%K%' OR st LIKE '%CZ%'); -- the check for '%W%' will catch WITZ
    SET pos = first;
    --  pos is short for position
    -- skip these silent letters when at start of word
    IF SUBSTRING(st, first, 2) IN ('GN', 'KN', 'PN', 'WR', 'PS') THEN
        SET pos = pos + 1;
    END IF;
    --  Initial 'X' is pronounced 'Z' e.g. 'Xavier'
    IF SUBSTRING(st, first, 1) = 'X' THEN
        SET pri = 'S', sec = 'S', pos = pos + 1; -- 'Z' maps to 'S'
    END IF;
    --  main loop through chars IN st
    WHILE pos <= last
        DO
            -- print str(pos) + '\t' + SUBSTRING(st, pos)
            SET prevpos = pos;
            SET ch = SUBSTRING(st, pos, 1); --  ch is short for character
            CASE
                WHEN ch IN ('A', 'E', 'I', 'O', 'U', 'Y') THEN
                    IF pos = first THEN --  all init vowels now map to 'A'
                        SET pri = CONCAT(pri, 'A'), sec = CONCAT(sec, 'A'), pos = pos + 1; -- nxt = ('A', 1)
                    ELSE
                        SET pos = pos + 1;
                    END IF;
                WHEN ch = 'B' THEN
                    -- '-mb', e.g', 'dumb', already skipped over... see 'M' below
                    IF SUBSTRING(st, pos + 1, 1) = 'B' THEN
                        SET pri = CONCAT(pri, 'P'), sec = CONCAT(sec, 'P'), pos = pos + 2; -- nxt = ('P', 2)
                    ELSE
                        SET pri = CONCAT(pri, 'P'), sec = CONCAT(sec, 'P'), pos = pos + 1; -- nxt = ('P', 1)
                    END IF;
                WHEN ch = 'C' THEN
                    --  various germanic
                    IF (pos > (first + 1) AND SUBSTRING(st, pos - 2, 1) NOT IN ('A', 'E', 'I', 'O', 'U', 'Y') AND
                        SUBSTRING(st, pos - 1, 3) = 'ACH' AND
                        (SUBSTRING(st, pos + 2, 1) NOT IN ('I', 'E') OR
                         SUBSTRING(st, pos - 2, 6) IN ('BACHER', 'MACHER'))) THEN
                        SET pri = CONCAT(pri, 'K'), sec = CONCAT(sec, 'K'), pos = pos + 2; -- nxt = ('K', 2)
                    --  special case 'CAESAR'
                    ELSEIF pos = first AND SUBSTRING(st, first, 6) = 'CAESAR' THEN
                        SET pri = CONCAT(pri, 'S'), sec = CONCAT(sec, 'S'), pos = pos + 2; -- nxt = ('S', 2)
                    ELSEIF SUBSTRING(st, pos, 4) = 'CHIA' THEN -- italian 'chianti'
                        SET pri = CONCAT(pri, 'K'), sec = CONCAT(sec, 'K'), pos = pos + 2; -- nxt = ('K', 2)
                    ELSEIF SUBSTRING(st, pos, 2) = 'CH' THEN
                        --  find 'michael'
                        IF pos > first AND SUBSTRING(st, pos, 4) = 'CHAE' THEN
                            SET pri = CONCAT(pri, 'K'), sec = CONCAT(sec, 'X'), pos = pos + 2; -- nxt = ('K', 'X', 2)
                        ELSEIF pos = first AND (SUBSTRING(st, pos + 1, 5) IN ('HARAC', 'HARIS') OR
                                                SUBSTRING(st, pos + 1, 3) IN ('HOR', 'HYM', 'HIA', 'HEM')) AND
                               SUBSTRING(st, first, 5) != 'CHORE' THEN
                            SET pri = CONCAT(pri, 'K'), sec = CONCAT(sec, 'K'), pos = pos + 2; -- nxt = ('K', 2)
                        -- germanic, greek, OR otherwise 'ch' for 'kh' sound
                        ELSEIF SUBSTRING(st, first, 4) IN ('VAN ', 'VON ') OR SUBSTRING(st, first, 3) = 'SCH'
                            OR SUBSTRING(st, pos - 2, 6) IN ('ORCHES', 'ARCHIT', 'ORCHID')
                            OR SUBSTRING(st, pos + 2, 1) IN ('T', 'S')
                            OR ((SUBSTRING(st, pos - 1, 1) IN ('A', 'O', 'U', 'E') OR pos = first)
                                AND
                                SUBSTRING(st, pos + 2, 1) IN ('L', 'R', 'N', 'M', 'B', 'H', 'F', 'V', 'W', ' ')) THEN
                            SET pri = CONCAT(pri, 'K'), sec = CONCAT(sec, 'K'), pos = pos + 2; -- nxt = ('K', 2)
                        ELSE
                            IF pos > first THEN
                                IF SUBSTRING(st, first, 2) = 'MC' THEN
                                    SET pri = CONCAT(pri, 'K'), sec = CONCAT(sec, 'K'), pos = pos + 2; -- nxt = ('K', 2)
                                ELSE
                                    SET pri = CONCAT(pri, 'X'), sec = CONCAT(sec, 'K'), pos = pos + 2; -- nxt = ('X', 'K', 2)
                                END IF;
                            ELSE
                                SET pri = CONCAT(pri, 'X'), sec = CONCAT(sec, 'X'), pos = pos + 2; -- nxt = ('X', 2)
                            END IF;
                        END IF;
                        -- e.g, 'czerny'
                    ELSEIF SUBSTRING(st, pos, 2) = 'CZ' AND SUBSTRING(st, pos - 2, 4) != 'WICZ' THEN
                        SET pri = CONCAT(pri, 'S'), sec = CONCAT(sec, 'X'), pos = pos + 2; -- nxt = ('S', 'X', 2)
                    -- e.g., 'focaccia'
                    ELSEIF SUBSTRING(st, pos + 1, 3) = 'CIA' THEN
                        SET pri = CONCAT(pri, 'X'), sec = CONCAT(sec, 'X'), pos = pos + 3; -- nxt = ('X', 3)
                    -- double 'C', but not IF e.g. 'McClellan'
                    ELSEIF SUBSTRING(st, pos, 2) = 'CC' AND
                           NOT (pos = (first + 1) AND SUBSTRING(st, first, 1) = 'M') THEN
                        -- 'bellocchio' but not 'bacchus'
                        IF SUBSTRING(st, pos + 2, 1) IN ('I', 'E', 'H') AND SUBSTRING(st, pos + 2, 2) != 'HU' THEN
                            -- 'accident', 'accede' 'succeed'
                            IF (pos = first + 1 AND SUBSTRING(st, first) = 'A') OR
                               SUBSTRING(st, pos - 1, 5) IN ('UCCEE', 'UCCES') THEN
                                SET pri = CONCAT(pri, 'KS'), sec = CONCAT(sec, 'KS'), pos = pos + 3; -- nxt = ('KS', 3)
                            -- 'bacci', 'bertucci', other italian
                            ELSE
                                SET pri = CONCAT(pri, 'X'), sec = CONCAT(sec, 'X'), pos = pos + 3; -- nxt = ('X', 3)
                            END IF;
                        ELSE
                            SET pri = CONCAT(pri, 'K'), sec = CONCAT(sec, 'K'), pos = pos + 2; -- nxt = ('K', 2)
                        END IF;
                    ELSEIF SUBSTRING(st, pos, 2) IN ('CK', 'CG', 'CQ') THEN
                        SET pri = CONCAT(pri, 'K'), sec = CONCAT(sec, 'K'), pos = pos + 2; -- nxt = ('K', 'K', 2)
                    ELSEIF SUBSTRING(st, pos, 2) IN ('CI', 'CE', 'CY') THEN
                        -- italian vs. english
                        IF SUBSTRING(st, pos, 3) IN ('CIO', 'CIE', 'CIA') THEN
                            SET pri = CONCAT(pri, 'S'), sec = CONCAT(sec, 'X'), pos = pos + 2; -- nxt = ('S', 'X', 2)
                        ELSE
                            SET pri = CONCAT(pri, 'S'), sec = CONCAT(sec, 'S'), pos = pos + 2; -- nxt = ('S', 2)
                        END IF;
                    ELSE
                        -- name sent IN 'mac caffrey', 'mac gregor
                        IF SUBSTRING(st, pos + 1, 2) IN (' C', ' Q', ' G') THEN
                            SET pri = CONCAT(pri, 'K'), sec = CONCAT(sec, 'K'), pos = pos + 3; -- nxt = ('K', 3)
                        ELSE
                            IF SUBSTRING(st, pos + 1, 1) IN ('C', 'K', 'Q') AND
                               SUBSTRING(st, pos + 1, 2) NOT IN ('CE', 'CI') THEN
                                SET pri = CONCAT(pri, 'K'), sec = CONCAT(sec, 'K'), pos = pos + 2; -- nxt = ('K', 2)
                            ELSE --  default for 'C'
                                SET pri = CONCAT(pri, 'K'), sec = CONCAT(sec, 'K'), pos = pos + 1; -- nxt = ('K', 1)
                            END IF;
                        END IF;
                    END IF;
                -- ELSEIF ch = 'Ç' THEN --  will never get here with st.encode('ascii', 'replace') above
                -- SET pri = CONCAT(pri, '5'), sec = CONCAT(sec, '5'), pos = pos  + 1; -- nxt = ('S', 1)
                WHEN ch = 'D' THEN
                    IF SUBSTRING(st, pos, 2) = 'DG' THEN
                        IF SUBSTRING(st, pos + 2, 1) IN ('I', 'E', 'Y') THEN -- e.g. 'edge'
                            SET pri = CONCAT(pri, 'J'), sec = CONCAT(sec, 'J'), pos = pos + 3; -- nxt = ('J', 3)
                        ELSE
                            SET pri = CONCAT(pri, 'TK'), sec = CONCAT(sec, 'TK'), pos = pos + 2; -- nxt = ('TK', 2)
                        END IF;
                    ELSEIF SUBSTRING(st, pos, 2) IN ('DT', 'DD') THEN
                        SET pri = CONCAT(pri, 'T'), sec = CONCAT(sec, 'T'), pos = pos + 2; -- nxt = ('T', 2)
                    ELSE
                        SET pri = CONCAT(pri, 'T'), sec = CONCAT(sec, 'T'), pos = pos + 1; -- nxt = ('T', 1)
                    END IF;
                WHEN ch = 'F' THEN
                    IF SUBSTRING(st, pos + 1, 1) = 'F' THEN
                        SET pri = CONCAT(pri, 'F'), sec = CONCAT(sec, 'F'), pos = pos + 2; -- nxt = ('F', 2)
                    ELSE
                        SET pri = CONCAT(pri, 'F'), sec = CONCAT(sec, 'F'), pos = pos + 1; -- nxt = ('F', 1)
                    END IF;
                WHEN ch = 'G' THEN
                    IF SUBSTRING(st, pos + 1, 1) = 'H' THEN
                        IF (pos > first AND SUBSTRING(st, pos - 1, 1) NOT IN ('A', 'E', 'I', 'O', 'U', 'Y'))
                            OR (pos = first AND SUBSTRING(st, pos + 2, 1) != 'I') THEN
                            SET pri = CONCAT(pri, 'K'), sec = CONCAT(sec, 'K'), pos = pos + 2; -- nxt = ('K', 2)
                        ELSEIF pos = first AND SUBSTRING(st, pos + 2, 1) = 'I' THEN
                            SET pri = CONCAT(pri, 'J'), sec = CONCAT(sec, 'J'), pos = pos + 2; -- nxt = ('J', 2)
                        -- Parker's rule (with some further refinements) - e.g., 'hugh'
                        ELSEIF (pos > (first + 1) AND SUBSTRING(st, pos - 2, 1) IN ('B', 'H', 'D'))
                            OR (pos > (first + 2) AND SUBSTRING(st, pos - 3, 1) IN ('B', 'H', 'D'))
                            OR (pos > (first + 3) AND SUBSTRING(st, pos - 4, 1) IN ('B', 'H')) THEN
                            SET pos = pos + 2; -- nxt = (None, 2)
                        ELSE
                            --  e.g., 'laugh', 'McLaughlin', 'cough', 'gough', 'rough', 'tough'
                            IF pos > (first + 2) AND SUBSTRING(st, pos - 1, 1) = 'U'
                                AND SUBSTRING(st, pos - 3, 1) IN ('C', 'G', 'L', 'R', 'T') THEN
                                SET pri = CONCAT(pri, 'F'), sec = CONCAT(sec, 'F'), pos = pos + 2; -- nxt = ('F', 2)
                            ELSEIF pos > first AND SUBSTRING(st, pos - 1, 1) != 'I' THEN
                                SET pri = CONCAT(pri, 'K'), sec = CONCAT(sec, 'K'), pos = pos + 2; -- nxt = ('K', 2)
                            ELSE
                                SET pos = pos + 1;
                            END IF;
                        END IF;
                    ELSEIF SUBSTRING(st, pos + 1, 1) = 'N' THEN
                        IF pos = (first + 1) AND SUBSTRING(st, first, 1) IN ('A', 'E', 'I', 'O', 'U', 'Y') AND
                           NOT is_slavo_germanic THEN
                            SET pri = CONCAT(pri, 'KN'), sec = CONCAT(sec, 'N'), pos = pos + 2; -- nxt = ('KN', 'N', 2)
                        ELSE
                            --  not e.g. 'cagney'
                            IF SUBSTRING(st, pos + 2, 2) != 'EY' AND SUBSTRING(st, pos + 1, 1) != 'Y'
                                AND NOT is_slavo_germanic THEN
                                SET pri = CONCAT(pri, 'N'), sec = CONCAT(sec, 'KN'), pos = pos + 2; -- nxt = ('N', 'KN', 2)
                            ELSE
                                SET pri = CONCAT(pri, 'KN'), sec = CONCAT(sec, 'KN'), pos = pos + 2; -- nxt = ('KN', 2)
                            END IF;
                        END IF;
                        --  'tagliaro'
                    ELSEIF SUBSTRING(st, pos + 1, 2) = 'LI' AND NOT is_slavo_germanic THEN
                        SET pri = CONCAT(pri, 'KL'), sec = CONCAT(sec, 'L'), pos = pos + 2; -- nxt = ('KL', 'L', 2)
                    --  -ges-,-gep-,-gel-, -gie- at beginning
                    ELSEIF pos = first AND (SUBSTRING(st, pos + 1, 1) = 'Y'
                        OR SUBSTRING(st, pos + 1, 2) IN
                           ('ES', 'EP', 'EB', 'EL', 'EY', 'IB', 'IL', 'IN', 'IE', 'EI', 'ER')) THEN
                        SET pri = CONCAT(pri, 'K'), sec = CONCAT(sec, 'J'), pos = pos + 2; -- nxt = ('K', 'J', 2)
                    --  -ger-,  -gy-
                    ELSEIF (SUBSTRING(st, pos + 1, 2) = 'ER' OR SUBSTRING(st, pos + 1, 1) = 'Y')
                        AND SUBSTRING(st, first, 6) NOT IN ('DANGER', 'RANGER', 'MANGER')
                        AND SUBSTRING(st, pos - 1, 1) NOT IN ('E', 'I') AND
                           SUBSTRING(st, pos - 1, 3) NOT IN ('RGY', 'OGY') THEN
                        SET pri = CONCAT(pri, 'K'), sec = CONCAT(sec, 'J'), pos = pos + 2; -- nxt = ('K', 'J', 2)
                    --  italian e.g, 'biaggi'
                    ELSEIF SUBSTRING(st, pos + 1, 1) IN ('E', 'I', 'Y') OR
                           SUBSTRING(st, pos - 1, 4) IN ('AGGI', 'OGGI') THEN
                        --  obvious germanic
                        IF SUBSTRING(st, first, 4) IN ('VON ', 'VAN ') OR SUBSTRING(st, first, 3) = 'SCH'
                            OR SUBSTRING(st, pos + 1, 2) = 'ET' THEN
                            SET pri = CONCAT(pri, 'K'), sec = CONCAT(sec, 'K'), pos = pos + 2; -- nxt = ('K', 2)
                        ELSE
                            --  always soft IF french ending
                            IF SUBSTRING(st, pos + 1, 4) = 'IER ' THEN
                                SET pri = CONCAT(pri, 'J'), sec = CONCAT(sec, 'J'), pos = pos + 2; -- nxt = ('J', 2)
                            ELSE
                                SET pri = CONCAT(pri, 'J'), sec = CONCAT(sec, 'K'), pos = pos + 2; -- nxt = ('J', 'K', 2)
                            END IF;
                        END IF;
                    ELSEIF SUBSTRING(st, pos + 1, 1) = 'G' THEN
                        SET pri = CONCAT(pri, 'K'), sec = CONCAT(sec, 'K'), pos = pos + 2; -- nxt = ('K', 2)
                    ELSE
                        SET pri = CONCAT(pri, 'K'), sec = CONCAT(sec, 'K'), pos = pos + 1; -- nxt = ('K', 1)
                    END IF;
                WHEN ch = 'H' THEN
                    --  only keep IF first & before vowel OR btw. 2 ('A', 'E', 'I', 'O', 'U', 'Y')
                    IF (pos = first OR SUBSTRING(st, pos - 1, 1) IN ('A', 'E', 'I', 'O', 'U', 'Y'))
                        AND SUBSTRING(st, pos + 1, 1) IN ('A', 'E', 'I', 'O', 'U', 'Y') THEN
                        SET pri = CONCAT(pri, 'H'), sec = CONCAT(sec, 'H'), pos = pos + 2; -- nxt = ('H', 2)
                    ELSE --  (also takes care of 'HH')
                        SET pos = pos + 1; -- nxt = (None, 1)
                    END IF;
                WHEN ch = 'J' THEN
                    --  obvious spanish, 'jose', 'san jacinto'
                    IF SUBSTRING(st, pos, 4) = 'JOSE' OR SUBSTRING(st, first, 4) = 'SAN ' THEN
                        IF (pos = first AND SUBSTRING(st, pos + 4, 1) = ' ') OR SUBSTRING(st, first, 4) = 'SAN ' THEN
                            SET pri = CONCAT(pri, 'H'), sec = CONCAT(sec, 'H'); -- nxt = ('H',)
                        ELSE
                            SET pri = CONCAT(pri, 'J'), sec = CONCAT(sec, 'H'); -- nxt = ('J', 'H')
                        END IF;
                    ELSEIF pos = first AND SUBSTRING(st, pos, 4) != 'JOSE' THEN
                        SET pri = CONCAT(pri, 'J'), sec = CONCAT(sec, 'A'); -- nxt = ('J', 'A') --  Yankelovich/Jankelowicz
                    ELSE
                        --  spanish pron. of e.g. 'bajador'
                        IF SUBSTRING(st, pos - 1, 1) IN ('A', 'E', 'I', 'O', 'U', 'Y') AND NOT is_slavo_germanic
                            AND SUBSTRING(st, pos + 1, 1) IN ('A', 'O') THEN
                            SET pri = CONCAT(pri, 'J'), sec = CONCAT(sec, 'H'); -- nxt = ('J', 'H')
                        ELSE
                            IF pos = last THEN
                                SET pri = CONCAT(pri, 'J'); -- nxt = ('J', ' ')
                            ELSE
                                IF SUBSTRING(st, pos + 1, 1) NOT IN ('L', 'T', 'K', 'S', 'N', 'M', 'B', 'Z')
                                    AND SUBSTRING(st, pos - 1, 1) NOT IN ('S', 'K', 'L') THEN
                                    SET pri = CONCAT(pri, 'J'), sec = CONCAT(sec, 'J'); -- nxt = ('J',)
                                END IF;
                            END IF;
                        END IF;
                    END IF;
                    IF SUBSTRING(st, pos + 1, 1) = 'J' THEN
                        SET pos = pos + 2;
                    ELSE
                        SET pos = pos + 1;
                    END IF;
                WHEN ch = 'K' THEN
                    IF SUBSTRING(st, pos + 1, 1) = 'K' THEN
                        SET pri = CONCAT(pri, 'K'), sec = CONCAT(sec, 'K'), pos = pos + 2; -- nxt = ('K', 2)
                    ELSE
                        SET pri = CONCAT(pri, 'K'), sec = CONCAT(sec, 'K'), pos = pos + 1; -- nxt = ('K', 1)
                    END IF;
                WHEN ch = 'L' THEN
                    IF SUBSTRING(st, pos + 1, 1) = 'L' THEN
                        --  spanish e.g. 'cabrillo', 'gallegos'
                        IF (pos = (last - 2) AND SUBSTRING(st, pos - 1, 4) IN ('ILLO', 'ILLA', 'ALLE'))
                            OR ((SUBSTRING(st, last - 1, 2) IN ('AS', 'OS') OR SUBSTRING(st, last) IN ('A', 'O'))
                                AND SUBSTRING(st, pos - 1, 4) = 'ALLE') THEN
                            SET pri = CONCAT(pri, 'L'), pos = pos + 2; -- nxt = ('L', ' ', 2)
                        ELSE
                            SET pri = CONCAT(pri, 'L'), sec = CONCAT(sec, 'L'), pos = pos + 2; -- nxt = ('L', 2)
                        END IF;
                    ELSE
                        SET pri = CONCAT(pri, 'L'), sec = CONCAT(sec, 'L'), pos = pos + 1; -- nxt = ('L', 1)
                    END IF;
                WHEN ch = 'M' THEN
                    IF SUBSTRING(st, pos - 1, 3) = 'UMB'
                           AND (pos + 1 = last OR SUBSTRING(st, pos + 2, 2) = 'ER')
                        OR SUBSTRING(st, pos + 1, 1) = 'M' THEN
                        SET pri = CONCAT(pri, 'M'), sec = CONCAT(sec, 'M'), pos = pos + 2; -- nxt = ('M', 2)
                    ELSE
                        SET pri = CONCAT(pri, 'M'), sec = CONCAT(sec, 'M'), pos = pos + 1; -- nxt = ('M', 1)
                    END IF;
                WHEN ch = 'N' THEN
                    IF SUBSTRING(st, pos + 1, 1) = 'N' THEN
                        SET pri = CONCAT(pri, 'N'), sec = CONCAT(sec, 'N'), pos = pos + 2; -- nxt = ('N', 2)
                    ELSE
                        SET pri = CONCAT(pri, 'N'), sec = CONCAT(sec, 'N'), pos = pos + 1; -- nxt = ('N', 1)
                    END IF;
                -- ELSEIF ch = u'Ñ' THEN
                -- SET pri = CONCAT(pri, '5'), sec = CONCAT(sec, '5'), pos = pos  + 1; -- nxt = ('N', 1)
                WHEN ch = 'P' THEN
                    IF SUBSTRING(st, pos + 1, 1) = 'H' THEN
                        SET pri = CONCAT(pri, 'F'), sec = CONCAT(sec, 'F'), pos = pos + 2; -- nxt = ('F', 2)
                    ELSEIF SUBSTRING(st, pos + 1, 1) IN ('P', 'B') THEN --  also account for 'campbell', 'raspberry'
                        SET pri = CONCAT(pri, 'P'), sec = CONCAT(sec, 'P'), pos = pos + 2; -- nxt = ('P', 2)
                    ELSE
                        SET pri = CONCAT(pri, 'P'), sec = CONCAT(sec, 'P'), pos = pos + 1; -- nxt = ('P', 1)
                    END IF;
                WHEN ch = 'Q' THEN
                    IF SUBSTRING(st, pos + 1, 1) = 'Q' THEN
                        SET pri = CONCAT(pri, 'K'), sec = CONCAT(sec, 'K'), pos = pos + 2; -- nxt = ('K', 2)
                    ELSE
                        SET pri = CONCAT(pri, 'K'), sec = CONCAT(sec, 'K'), pos = pos + 1; -- nxt = ('K', 1)
                    END IF;
                WHEN ch = 'R' THEN
                    --  french e.g. 'rogier', but exclude 'hochmeier'
                    IF pos = last AND NOT is_slavo_germanic
                        AND SUBSTRING(st, pos - 2, 2) = 'IE' AND SUBSTRING(st, pos - 4, 2) NOT IN ('ME', 'MA') THEN
                        SET sec = CONCAT(sec, 'R'); -- nxt = ('', 'R')
                    ELSE
                        SET pri = CONCAT(pri, 'R'), sec = CONCAT(sec, 'R'); -- nxt = ('R',)
                    END IF;
                    IF SUBSTRING(st, pos + 1, 1) = 'R' THEN
                        SET pos = pos + 2;
                    ELSE
                        SET pos = pos + 1;
                    END IF;
                WHEN ch = 'S' THEN
                    --  special cases 'island', 'isle', 'carlisle', 'carlysle'
                    IF SUBSTRING(st, pos - 1, 3) IN ('ISL', 'YSL') THEN
                        SET pos = pos + 1;
                        --  special case 'sugar-'
                    ELSEIF pos = first AND SUBSTRING(st, first, 5) = 'SUGAR' THEN
                        SET pri = CONCAT(pri, 'X'), sec = CONCAT(sec, 'S'), pos = pos + 1; --  nxt =('X', 'S', 1)
                    ELSEIF SUBSTRING(st, pos, 2) = 'SH' THEN
                        --  germanic
                        IF SUBSTRING(st, pos + 1, 4) IN ('HEIM', 'HOEK', 'HOLM', 'HOLZ') THEN
                            SET pri = CONCAT(pri, 'S'), sec = CONCAT(sec, 'S'), pos = pos + 2; -- nxt = ('S', 2)
                        ELSE
                            SET pri = CONCAT(pri, 'X'), sec = CONCAT(sec, 'X'), pos = pos + 2; -- nxt = ('X', 2)
                        END IF;
                        --  italian & armenian
                    ELSEIF SUBSTRING(st, pos, 3) IN ('SIO', 'SIA') OR SUBSTRING(st, pos, 4) = 'SIAN' THEN
                        IF NOT is_slavo_germanic THEN
                            SET pri = CONCAT(pri, 'S'), sec = CONCAT(sec, 'X'), pos = pos + 3; -- nxt = ('S', 'X', 3)
                        ELSE
                            SET pri = CONCAT(pri, 'S'), sec = CONCAT(sec, 'S'), pos = pos + 3; -- nxt = ('S', 3)
                        END IF;
                        --  german & anglicisations, e.g. 'smith' match 'schmidt', 'snider' match 'schneider'
                        --  also, -sz- IN slavic language altho IN hungarian it is pronounced 's'
                    ELSEIF (pos = first AND SUBSTRING(st, pos + 1, 1) IN ('M', 'N', 'L', 'W')) OR
                           SUBSTRING(st, pos + 1, 1) = 'Z' THEN
                        SET pri = CONCAT(pri, 'S'), sec = CONCAT(sec, 'X'); -- nxt = ('S', 'X')
                        IF SUBSTRING(st, pos + 1, 1) = 'Z' THEN
                            SET pos = pos + 2;
                        ELSE
                            SET pos = pos + 1;
                        END IF;
                    ELSEIF SUBSTRING(st, pos, 2) = 'SC' THEN
                        --  Schlesinger's rule
                        IF SUBSTRING(st, pos + 2, 1) = 'H' THEN
                            --  dutch origin, e.g. 'school', 'schooner'
                            IF SUBSTRING(st, pos + 3, 2) IN ('OO', 'ER', 'EN', 'UY', 'ED', 'EM') THEN
                                --  'schermerhorn', 'schenker'
                                IF SUBSTRING(st, pos + 3, 2) IN ('ER', 'EN') THEN
                                    SET pri = CONCAT(pri, 'X'), sec = CONCAT(sec, 'SK'), pos = pos + 3; -- nxt = ('X', 'SK', 3)
                                ELSE
                                    SET pri = CONCAT(pri, 'SK'), sec = CONCAT(sec, 'SK'), pos = pos + 3; -- nxt = ('SK', 3)
                                END IF;
                            ELSE
                                IF pos = first AND SUBSTRING(st, first + 3, 1) NOT IN ('A', 'E', 'I', 'O', 'U', 'Y') AND
                                   SUBSTRING(st, first + 3, 1) != 'W' THEN
                                    SET pri = CONCAT(pri, 'X'), sec = CONCAT(sec, 'S'), pos = pos + 3; -- nxt = ('X', 'S', 3)
                                ELSE
                                    SET pri = CONCAT(pri, 'X'), sec = CONCAT(sec, 'X'), pos = pos + 3; -- nxt = ('X', 3)
                                END IF;
                            END IF;
                        ELSEIF SUBSTRING(st, pos + 2, 1) IN ('I', 'E', 'Y') THEN
                            SET pri = CONCAT(pri, 'S'), sec = CONCAT(sec, 'S'), pos = pos + 3; -- nxt = ('S', 3)
                        ELSE
                            SET pri = CONCAT(pri, 'SK'), sec = CONCAT(sec, 'SK'), pos = pos + 3; -- nxt = ('SK', 3)
                        END IF;
                        --  french e.g. 'resnais', 'artois'
                    ELSEIF pos = last AND SUBSTRING(st, pos - 2, 2) IN ('AI', 'OI') THEN
                        SET sec = CONCAT(sec, 'S'), pos = pos + 1; -- nxt = ('', 'S')
                    ELSE
                        SET pri = CONCAT(pri, 'S'), sec = CONCAT(sec, 'S'); -- nxt = ('S',)
                        IF SUBSTRING(st, pos + 1, 1) IN ('S', 'Z') THEN
                            SET pos = pos + 2;
                        ELSE
                            SET pos = pos + 1;
                        END IF;
                    END IF;
                WHEN ch = 'T' THEN
                    IF SUBSTRING(st, pos, 4) = 'TION' THEN
                        SET pri = CONCAT(pri, 'X'), sec = CONCAT(sec, 'X'), pos = pos + 3; -- nxt = ('X', 3)
                    ELSEIF SUBSTRING(st, pos, 3) IN ('TIA', 'TCH') THEN
                        SET pri = CONCAT(pri, 'X'), sec = CONCAT(sec, 'X'), pos = pos + 3; -- nxt = ('X', 3)
                    ELSEIF SUBSTRING(st, pos, 2) = 'TH' OR SUBSTRING(st, pos, 3) = 'TTH' THEN
                        --  special case 'thomas', 'thames' OR germanic
                        IF SUBSTRING(st, pos + 2, 2) IN ('OM', 'AM') OR SUBSTRING(st, first, 4) IN ('VON ', 'VAN ')
                            OR SUBSTRING(st, first, 3) = 'SCH' THEN
                            SET pri = CONCAT(pri, 'T'), sec = CONCAT(sec, 'T'), pos = pos + 2; -- nxt = ('T', 2)
                        ELSE
                            SET pri = CONCAT(pri, '0'), sec = CONCAT(sec, 'T'), pos = pos + 2; -- nxt = ('0', 'T', 2)
                        END IF;
                    ELSEIF SUBSTRING(st, pos + 1, 1) IN ('T', 'D') THEN
                        SET pri = CONCAT(pri, 'T'), sec = CONCAT(sec, 'T'), pos = pos + 2; -- nxt = ('T', 2)
                    ELSE
                        SET pri = CONCAT(pri, 'T'), sec = CONCAT(sec, 'T'), pos = pos + 1; -- nxt = ('T', 1)
                    END IF;
                WHEN ch = 'V' THEN
                    IF SUBSTRING(st, pos + 1, 1) = 'V' THEN
                        SET pri = CONCAT(pri, 'F'), sec = CONCAT(sec, 'F'), pos = pos + 2; -- nxt = ('F', 2)
                    ELSE
                        SET pri = CONCAT(pri, 'F'), sec = CONCAT(sec, 'F'), pos = pos + 1; -- nxt = ('F', 1)
                    END IF;
                WHEN ch = 'W' THEN
                    --  can also be IN middle of word
                    IF SUBSTRING(st, pos, 2) = 'WR' THEN
                        SET pri = CONCAT(pri, 'R'), sec = CONCAT(sec, 'R'), pos = pos + 2; -- nxt = ('R', 2)
                    ELSEIF pos = first AND (SUBSTRING(st, pos + 1, 1) IN ('A', 'E', 'I', 'O', 'U', 'Y')
                        OR SUBSTRING(st, pos, 2) = 'WH') THEN
                        --  Wasserman should match Vasserman
                        IF SUBSTRING(st, pos + 1, 1) IN ('A', 'E', 'I', 'O', 'U', 'Y') THEN
                            SET pri = CONCAT(pri, 'A'), sec = CONCAT(sec, 'F'), pos = pos + 1; -- nxt = ('A', 'F', 1)
                        ELSE
                            SET pri = CONCAT(pri, 'A'), sec = CONCAT(sec, 'A'), pos = pos + 1; -- nxt = ('A', 1)
                        END IF;
                        --  Arnow should match Arnoff
                    ELSEIF (pos = last AND SUBSTRING(st, pos - 1, 1) IN ('A', 'E', 'I', 'O', 'U', 'Y'))
                        OR SUBSTRING(st, pos - 1, 5) IN ('EWSKI', 'EWSKY', 'OWSKI', 'OWSKY')
                        OR SUBSTRING(st, first, 3) = 'SCH' THEN
                        SET sec = CONCAT(sec, 'F'), pos = pos + 1; -- nxt = ('', 'F', 1)
                    -- END IF;
                    --  polish e.g. 'filipowicz'
                    ELSEIF SUBSTRING(st, pos, 4) IN ('WICZ', 'WITZ') THEN
                        SET pri = CONCAT(pri, 'TS'), sec = CONCAT(sec, 'FX'), pos = pos + 4; -- nxt = ('TS', 'FX', 4)
                    ELSE --  default is to skip it
                        SET pos = pos + 1;
                    END IF;
                WHEN ch = 'X' THEN
                    --  french e.g. breaux
                    IF NOT (pos = last AND (SUBSTRING(st, pos - 3, 3) IN ('IAU', 'EAU')
                        OR SUBSTRING(st, pos - 2, 2) IN ('AU', 'OU'))) THEN
                        SET pri = CONCAT(pri, 'KS'), sec = CONCAT(sec, 'KS'); -- nxt = ('KS',)
                    END IF;
                    IF SUBSTRING(st, pos + 1, 1) IN ('C', 'X') THEN
                        SET pos = pos + 2;
                    ELSE
                        SET pos = pos + 1;
                    END IF;
                WHEN ch = 'Z' THEN
                    --  chinese pinyin e.g. 'zhao'
                    IF SUBSTRING(st, pos + 1, 1) = 'H' THEN
                        SET pri = CONCAT(pri, 'J'), sec = CONCAT(sec, 'J'), pos = pos + 1; -- nxt = ('J', 2)
                    ELSEIF SUBSTRING(st, pos + 1, 3) IN ('ZO', 'ZI', 'ZA')
                        OR (is_slavo_germanic AND pos > first AND SUBSTRING(st, pos - 1, 1) != 'T') THEN
                        SET pri = CONCAT(pri, 'S'), sec = CONCAT(sec, 'TS'); -- nxt = ('S', 'TS')
                    ELSE
                        SET pri = CONCAT(pri, 'S'), sec = CONCAT(sec, 'S'); -- nxt = ('S',)
                    END IF;
                    IF SUBSTRING(st, pos + 1, 1) = 'Z' THEN
                        SET pos = pos + 2;
                    ELSE
                        SET pos = pos + 1;
                    END IF;
                ELSE
                    SET pos = pos + 1; -- DEFAULT is to move to next char
                END CASE;
            IF pos = prevpos THEN
                SET pos = pos + 1;
                SET pri = CONCAT(pri, '<didnt incr>'); -- it might be better to throw an error here if you really must be accurate
            END IF;
        END WHILE;
    IF pri != sec THEN
        SET pri = CONCAT(pri, ';', sec);
    END IF;
    RETURN (pri);
END $$


DELIMITER $$
DROP FUNCTION IF EXISTS levenshtein $$
CREATE FUNCTION levenshtein(s1 VARCHAR(255) CHARACTER SET utf8, s2 VARCHAR(255) CHARACTER SET utf8)
    RETURNS INT
    DETERMINISTIC
BEGIN
    DECLARE s1_len, s2_len, i, j, c, c_temp, cost INT;
    DECLARE s1_char CHAR CHARACTER SET utf8;
    -- max strlen=255 for this function
    DECLARE cv0, cv1 VARBINARY(256);

    SET s1_len = CHAR_LENGTH(s1),
        s2_len = CHAR_LENGTH(s2),
        cv1 = 0x00,
        j = 1,
        i = 1,
        c = 0;

    IF (s1 = s2) THEN
        RETURN (0);
    ELSEIF (s1_len = 0) THEN
        RETURN (s2_len);
    ELSEIF (s2_len = 0) THEN
        RETURN (s1_len);
    END IF;

    WHILE (j <= s2_len)
        DO
            SET cv1 = CONCAT(cv1, CHAR(j)),
                j = j + 1;
        END WHILE;

    WHILE (i <= s1_len)
        DO
            SET s1_char = SUBSTRING(s1, i, 1),
                c = i,
                cv0 = CHAR(i),
                j = 1;

            WHILE (j <= s2_len)
                DO
                    SET c = c + 1,
                        cost = IF(s1_char = SUBSTRING(s2, j, 1), 0, 1);

                    SET c_temp = ORD(SUBSTRING(cv1, j, 1)) + cost;
                    IF (c > c_temp) THEN
                        SET c = c_temp;
                    END IF;

                    SET c_temp = ORD(SUBSTRING(cv1, j + 1, 1)) + 1;
                    IF (c > c_temp) THEN
                        SET c = c_temp;
                    END IF;

                    SET cv0 = CONCAT(cv0, CHAR(c)),
                        j = j + 1;
                END WHILE;

            SET cv1 = cv0,
                i = i + 1;
        END WHILE;

    RETURN (c);
END $$

DELIMITER ;
