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
    UNIQUE (artist, `release`)
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


DELIMITER $$

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
END$$
DELIMITER ;

DELIMITER $$
CREATE FUNCTION `soundex_match_all`(needle varchar(128), haystack text, splitchar varchar(1)) RETURNS tinyint(4)

BEGIN
    DECLARE comma INT DEFAULT 0;
    DECLARE word TEXT;

    SET comma = LOCATE(splitchar, needle);
    SET word = TRIM(needle);

    IF LENGTH(haystack) = 0 THEN
        RETURN 0;
    ELSEIF comma = 0 THEN
        RETURN soundex_match(word, haystack, splitchar);
    END IF;

    SET word = trim(substr(needle, 1, comma));

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
END $$
DELIMITER ;


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
