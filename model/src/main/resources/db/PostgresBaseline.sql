-- FOr the musicbrainz instance you must run the following functor creator.

CREATE FUNCTION calculate_country(area bigint) RETURNS character as $$

(WITH RECURSIVE area_descendants AS (
    SELECT entity0 AS parent, entity1 AS descendant, 1 AS depth
    FROM l_area_area laa
             JOIN link ON laa.link = link.id
    WHERE link.link_type = 356
      AND entity1 IN (area)
    UNION
    SELECT entity0 AS parent, descendant, (depth + 1) AS depth
    FROM l_area_area laa
             JOIN link ON laa.link = link.id
             JOIN area_descendants ON area_descendants.parent = laa.entity1
    WHERE link.link_type = 356
      AND entity0 != descendant
)
 SELECT iso.code
 FROM area_descendants ad
          JOIN iso_3166_1 iso ON iso.area = ad.parent);
$$
language sql;

-- and add this table

create table country_lookup as
select id, calculate_country(id) as country
from area
where type != 1;

