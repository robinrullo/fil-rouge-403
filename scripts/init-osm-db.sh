## Download osm.pbf file from geofabrik or https://wiki.openstreetmap.org/wiki/Downloading_data
# https://download.geofabrik.de/europe/france.html
wget https://download.geofabrik.de/europe/france/alsace-latest.osm.pbf

## PGSQL using TCP
osm2pgsql \
  --host=localhost \
  --port=5432 \
  --user=postgres \
  --database=osm \
  --password \
  --create \
  --proj=4326 \
  alsace-latest.osm.pbf

## Remove osm.pbf file
rm alsace-latest.osm.pbf
