
echo 'host  all   all   0.0.0.0/0   trust' > /var/lib/postgresql/data/pg_hba.conf

psql  -U "$POSTGRES_USER" musicbrainz -c 'CREATE EXTENSION cube;'
psql -U "$POSTGRES_USER" musicbrainz -c 'CREATE EXTENSION earthdistance;'

echo 'CREATE SCHEMA musicbrainz;' | mbslave psql -S

echo 'CREATE SCHEMA statistics;' | mbslave psql -S

echo 'CREATE SCHEMA cover_art_archive;' | mbslave psql -S
echo 'CREATE SCHEMA wikidocs;' | mbslave psql -S
echo 'CREATE SCHEMA documentation;' | mbslave psql -S

mbslave psql -f CreateCollations.sql
mbslave psql -f CreateTables.sql
mbslave psql -f statistics/CreateTables.sql
mbslave psql -f caa/CreateTables.sql
mbslave psql -f wikidocs/CreateTables.sql
mbslave psql -f documentation/CreateTables.sql


# Fetches all dumps
fetch-dump.sh replica
# Inserts all dumps
mbslave import /media/dbdump/mbdump.tar.bz2 /media/dbdump/mbdump-cdstubs.tar.bz2 /media/dbdump/mbdump-cover-art-archive.tar.bz2 /media/dbdump/mbdump-derived.tar.bz2 /media/dbdump/mbdump-stats.tar.bz2 /media/dbdump/mbdump-wikidocs.tar.bz2

rm  /media/dbdump/*.tar.bz2
# Create pk and fk with all data loaded
mbslave psql -f CreatePrimaryKeys.sql
mbslave psql -f statistics/CreatePrimaryKeys.sql
mbslave psql -f caa/CreatePrimaryKeys.sql
mbslave psql -f wikidocs/CreatePrimaryKeys.sql
mbslave psql -f documentation/CreatePrimaryKeys.sql

mbslave psql -f CreateIndexes.sql
mbslave psql -f CreateSlaveIndexes.sql
mbslave psql -f statistics/CreateIndexes.sql
mbslave psql -f caa/CreateIndexes.sql

mbslave psql -f CreateFunctions.sql
mbslave psql -f CreateViews.sql
echo 'VACUUM ANALYZE;' | mbslave psql

