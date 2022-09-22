/*
 Create GraphHopper View after import of osm data
 */
\c osm
CREATE OR REPLACE VIEW public.gh_roads
AS
SELECT planet_osm_roads.osm_id,
       0                        AS maxspeed,
       CASE
           WHEN planet_osm_roads.oneway = 'no' THEN 'b'::character varying(5)
           WHEN planet_osm_roads.oneway = 'yes' THEN 'f'::character varying(5)
           ELSE planet_osm_roads.oneway::character varying(5)
           END
                                AS oneway,
       planet_osm_roads.highway AS fclass,
       planet_osm_roads.name,
       planet_osm_roads.way     AS geom
FROM planet_osm_roads;
