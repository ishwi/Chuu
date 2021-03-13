alter table guild
    add column override_color ENUM ('OVERRIDE', 'EMPTY') default 'OVERRIDE';

update guild
set color = null
where color = 'RANDOM';


update user
set color = null
where color = 'RANDOM';
