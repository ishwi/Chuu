ALTER TABLE guild
    ADD COLUMN override_color enum ('OVERRIDE', 'EMPTY') DEFAULT 'OVERRIDE';

UPDATE guild
SET
    color = NULL
WHERE
    color = 'RANDOM';


UPDATE user
SET
    color = NULL
WHERE
    color = 'RANDOM';
