DROP DATABASE IF EXISTS osm;
DROP DATABASE IF EXISTS gis;

/*
    Create databases and enable postgis and hstore extensions
*/

-- Create gis database
CREATE DATABASE gis;
\c gis
CREATE EXTENSION postgis;

-- Create osm database
CREATE DATABASE osm;
\c osm;
CREATE EXTENSION postgis;
CREATE EXTENSION hstore;
