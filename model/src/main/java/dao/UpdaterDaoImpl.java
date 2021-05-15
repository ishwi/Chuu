package dao;

import dao.entities.*;
import dao.exceptions.ChuuServiceException;
import dao.exceptions.DuplicateInstanceException;
import dao.exceptions.InstanceNotFoundException;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.sql.*;
import java.text.Normalizer;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UpdaterDaoImpl extends BaseDAO implements UpdaterDao {


    public static String preparePlaceHolders(int length) {
        return String.join(",", Collections.nCopies(length, "?"));
    }

    @Override
    public void addSrobbledArtists(Connection con, List<ScrobbledArtist> scrobbledArtists) {

        StringBuilder mySql =
                new StringBuilder("INSERT INTO  scrobbled_artist" +
                                  "                  (artist_id,lastfm_id,playnumber) VALUES (?, ?, ?) ");

        mySql.append(", (?,?,?)".repeat(Math.max(0, scrobbledArtists.size() - 1)));
        mySql.append(" ON DUPLICATE KEY UPDATE playnumber =  VALUES(playnumber) + playnumber");

        try {
            PreparedStatement preparedStatement = con.prepareStatement(mySql.toString());
            for (int i = 0; i < scrobbledArtists.size(); i++) {
                preparedStatement.setLong(3 * i + 1, scrobbledArtists.get(i).getArtistId());
                preparedStatement.setString(3 * i + 2, scrobbledArtists.get(i).getDiscordID());
                preparedStatement.setInt(3 * i + 3, scrobbledArtists.get(i).getCount());

            }
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public UpdaterUserWrapper getLessUpdated(Connection connection) {
        String queryString =
                "SELECT a.discord_id,a.role, a.lastfm_id,(if(last_update = '0000-00-00 00:00:00', '1971-01-01 00:00:01', last_update)) updating,(if(control_timestamp = '0000-00-00 00:00:00', '1971-01-01 00:00:01', control_timestamp)) controling,timezone " +
                "FROM user a   " +
                " WHERE NOT private_update " +
                "ORDER BY  control_timestamp LIMIT 1";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */

            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {

                String name = resultSet.getString("a.lastFm_Id");
                long discordID = resultSet.getLong("a.discord_ID");
                Timestamp timestamp = resultSet.getTimestamp("updating");
                Timestamp controlTimestamp = resultSet.getTimestamp("controling");
                Role role = Role.valueOf(resultSet.getString("a.role"));
                TimeZone tz = TimeZone.getTimeZone(Objects.requireNonNullElse(resultSet.getString("timezone"), "GMT"));


                return new UpdaterUserWrapper(discordID, name, ((int) timestamp.toInstant()
                        .getEpochSecond()), ((int) controlTimestamp.toInstant().getEpochSecond()), role, tz);
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return null;
    }

    @Override
    public void setUpdatedTime(Connection connection, String id, Integer timestamp, Integer timestampControl) {
        String queryString = "UPDATE user  "
                             + " SET last_update= ?, control_timestamp = ? WHERE user.lastfm_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            Timestamp timestamp1;
            if (timestamp == null) {
                timestamp1 = Timestamp.from(Instant.now());
            } else {
                timestamp1 = Timestamp.from(Instant.ofEpochSecond(timestamp));
            }
            Timestamp timestamp2;
            if (timestampControl == null) {
                timestamp2 = Timestamp.from(Instant.now());
            } else {
                timestamp2 = Timestamp.from(Instant.ofEpochSecond(timestampControl));
            }
            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setTimestamp(i++, timestamp1);
            preparedStatement.setTimestamp(i++, timestamp2);
            preparedStatement.setString(i, id);



            /* Execute query. */
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void upsertArtist(Connection con, List<ScrobbledArtist> scrobbledArtists) {
        StringBuilder mySql =
                new StringBuilder("INSERT INTO  scrobbled_artist" +
                                  "                (artist_id,lastfm_id,playnumber) VALUES (?, ?, ?) ");

        mySql.append(", (?,?,?)".repeat(Math.max(0, scrobbledArtists.size() - 1)));
        mySql.append(" ON DUPLICATE KEY UPDATE playnumber =  playnumber + VALUES(playnumber)");

        try {
            PreparedStatement preparedStatement = con.prepareStatement(mySql.toString());
            for (int i = 0; i < scrobbledArtists.size(); i++) {
                preparedStatement.setLong(3 * i + 1, scrobbledArtists.get(i).getArtistId());
                preparedStatement.setString(3 * i + 2, scrobbledArtists.get(i).getDiscordID());
                preparedStatement.setInt(3 * i + 3, scrobbledArtists.get(i).getCount());

            }
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public long upsertUrl(Connection con, String url, long artistId, long discordId) {
        String queryString = "INSERT INTO alt_url ( artist_id,url,discord_id)   VALUES (?, ?,?)  on duplicate key update id = id returning id";
        return insertArtistInfo(con, url, artistId, discordId, queryString);
    }

    private long insertArtistInfo(Connection con, String url, long artistId, long discordId, String queryString) {
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(i++, artistId);
            preparedStatement.setString(i++, url);
            preparedStatement.setLong(i, discordId);


            /* Execute query. */
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getResultSet();
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            /* Get generated identifier. */
            throw new ChuuServiceException();

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }/**/

    @Override
    public void upsertArtistsDetails(Connection con, List<ScrobbledArtist> scrobbledArtists) {
        StringBuilder queryString = new StringBuilder("INSERT  INTO  artist "
                                                      + " (name,url,correction_status)  VALUES (?, ?,?) ");


        queryString.append(", (?,?,?)".repeat(Math.max(0, scrobbledArtists.size() - 1)));
        queryString.append(" ON DUPLICATE KEY UPDATE url= values(url) ,correction_status = values(correction_status)");

        try {
            PreparedStatement preparedStatement = con.prepareStatement(queryString.toString());
            for (int i = 0; i < scrobbledArtists.size(); i++) {
                preparedStatement.setString(3 * i + 1, scrobbledArtists.get(i).getArtist());
                preparedStatement.setString(3 * i + 2, scrobbledArtists.get(i).getUrl());
                preparedStatement.setBoolean(3 * i + 3, scrobbledArtists.get(i).isUpdateBit());
            }
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public String getArtistUrl(Connection connection, String artist) {
        String queryString = "SELECT url FROM artist WHERE name= ? ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setString(1, artist);
            ResultSet resultSet = preparedStatement.executeQuery();


            /* Get generated identifier. */

            if (resultSet.next()) {
                return (resultSet.getString("url"));
            }

        } catch (SQLException e) {
            logger.warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return null;
    }

    @Override
    public Set<ScrobbledArtist> selectNullUrls(Connection connection, boolean doSpotifySearch) {
        Set<ScrobbledArtist> returnList = new HashSet<>();

        String queryString = doSpotifySearch ?
                "SELECT name,id FROM artist where url = '' and  url_status = 1 or url is null limit 20" :
                "SELECT name,id FROM artist where url is null limit 20";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            ResultSet resultSet = preparedStatement.executeQuery();


            /* Get generated identifier. */

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                long id = resultSet.getLong("id");
                ScrobbledArtist scrobbledArtist = new ScrobbledArtist(name, 0, null);
                scrobbledArtist.setArtistId(id);
                returnList.add(scrobbledArtist);
            }

        } catch (SQLException e) {
            logger.warn(e.getMessage(), e);
        }
        return returnList;
    }

    @Override
    public void upsertSpotify(Connection con, String url, long artist_id, long discordId) {
        String queryString = "INSERT INTO alt_url ( artist_id,url,discord_id)   VALUES (?, ?,?) on duplicate key update id = id returning id";

        insertArtistInfo(con, url, artist_id, discordId, queryString);
    }

    @Override
    public UpdaterStatus getUpdaterStatus(Connection connection, String artist) throws InstanceNotFoundException {
        String queryString = "SELECT a.id,url,correction_status,name FROM artist a " +
                             " WHERE a.name = ? ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setString(1, artist);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String url = resultSet.getString("url");
                boolean status = resultSet.getBoolean("correction_status");
                long artistId = resultSet.getLong("a.id");
                String artistName = resultSet.getString("a.name");

                return new UpdaterStatus(url, status, artistId, artistName);
            }
            throw new InstanceNotFoundException(artist);
        } catch (SQLException e) {
            logger.warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void insertCorrection(Connection connection, long artistId, String correction) {
        String queryString = "INSERT INTO corrections"
                             + " (alias,artist_id) VALUES (?, ?) ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setString(i++, correction);
            preparedStatement.setLong(i, artistId);


            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void updateStatusBit(Connection connection, long artistId, boolean statusBit, String url) {
        String queryString = "UPDATE artist SET correction_status = ?, url = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */

            int i = 1;
            preparedStatement.setBoolean(i++, statusBit);
            preparedStatement.setString(i++, url);
            preparedStatement.setLong(i, artistId);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public String findCorrection(Connection connection, String artist) {
        String queryString = "SELECT  name  FROM corrections JOIN artist a ON corrections.artist_id = a.id" +
                             " WHERE alias = ? ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setString(1, artist);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return (resultSet.getString(1));
            }

        } catch (SQLException e) {
            logger.warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return null;
    }

    @Override
    public void updateMetric(Connection connection, int metricId, long value) {
        String queryString = "UPDATE   metrics SET value = value + ?  WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(i++, value);
            preparedStatement.setInt(i, metricId);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void deleteAllArtists(Connection con, String id) {
        String queryString = "DELETE   FROM scrobbled_artist  WHERE lastfm_id = ? ";
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setString(i, id);

            preparedStatement.executeUpdate();


        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public boolean insertRandomUrl(Connection con, String url, long discordId, Long guildId) {
        String queryString = "INSERT INTO  randomlinks"
                             + " ( discord_id,url) " + " VALUES (?,  ?)";
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

            int i = 1;
            preparedStatement.setLong(i++, discordId);
            preparedStatement.setString(i, url);

            /* Execute query. */
            int rows = preparedStatement.executeUpdate();
            return rows != 0;

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public RandomUrlEntity getRandomUrl(Connection con) {
        String queryString = """
                SELECT * FROM randomlinks WHERE discord_id IN\s
                (SELECT discord_id FROM (SELECT discord_id,count(*)   , -log(1-rand()) / log(count(*) + 1)    AS ra FROM randomlinks GROUP BY discord_id having COUNT(*) > 0 ORDER BY ra LIMIT 1) t) or discord_id is null \s
                 ORDER BY rand() LIMIT 1;""";
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next())
                return null;

            String url = resultSet.getString("url");
            Long discordID = resultSet.getLong("discord_Id");
            return new RandomUrlEntity(url, discordID);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    @Override
    public RandomUrlEntity getRandomUrlFromServer(Connection con, long discordId) {
        String queryString = """
                SELECT * FROM randomlinks WHERE discord_id IN\s
                (SELECT discord_id FROM (SELECT a.discord_id,count(*)   , -log(1-rand()) / log(count(*) + 1)  AS ra FROM randomlinks a join user_guild b on  a.discord_id = b.discord_id  where b.guild_id = ? GROUP BY discord_id having COUNT(*) > 0 ORDER BY ra LIMIT 1) t)  \s
                 ORDER BY rand() LIMIT 1""";
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {
            preparedStatement.setLong(1, discordId);
            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next())
                return null;

            String url = resultSet.getString("url");
            Long discordID = resultSet.getLong("discord_Id");
            return new RandomUrlEntity(url, discordID);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public RandomUrlEntity getRandomUrlFromUser(Connection connection, long userId) {
        String queryString = """
                SELECT * FROM randomlinks WHERE discord_id = ? ORDER BY rand() LIMIT 1
                """;
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setLong(1, userId);
            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next())
                return null;

            String url = resultSet.getString("url");
            Long discordID = resultSet.getLong("discord_Id");
            return new RandomUrlEntity(url, discordID);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }


    }

    @Override
    public RandomUrlEntity findRandomUrlById(Connection con, String urlQ) {
        String queryString = "SELECT * " +
                             "FROM randomlinks  " +
                             "WHERE url = ?";
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {
            /* Fill "preparedStatement". */
            preparedStatement.setString(1, urlQ);
            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next())
                return null;

            String url = resultSet.getString("url");
            long discordID = resultSet.getLong("discord_Id");
            return new RandomUrlEntity(url, discordID);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public @Nullable
    RandomUrlDetails randomUrlDetails(Connection con, String urlQ) {
        String queryString = "SELECT (select avg(rating) from random_links_ratings where url = main.url), (select count(*) from random_links_ratings where url = main.url), main.discord_id,b.discord_id," +
                             "b.rating,coalesce(privacy_mode,'NORMAL'),lastfm_id " +
                             "FROM randomlinks main left join random_links_ratings b on main.url = b.url left join user c on b.discord_id = c.discord_id " +
                             "WHERE main.url = ?";
        RandomUrlDetails randomUrlDetails = null;
        List<RandomRating> a = new ArrayList<>();
        double average = 0;
        long count = 0;
        long discordID = 0;
        boolean init = false;
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {
            /* Fill "preparedStatement". */
            preparedStatement.setString(1, urlQ);
            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                init = true;

                average = resultSet.getDouble(1);
                count = resultSet.getLong(2);
                discordID = resultSet.getLong(3);
                long rater = resultSet.getLong(4);
                byte rating = resultSet.getByte(5);
                PrivacyMode privacyMode = PrivacyMode.valueOf(resultSet.getString(6));
                String lastfm_id = resultSet.getString(7);
                if (lastfm_id != null) {
                    a.add(new RandomRating(rater, rating, privacyMode, lastfm_id));
                }
            }
            if (init) {
                return new RandomUrlDetails(urlQ, discordID, average, count, a);
            }
            return null;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void insertAlbumCrown(Connection connection, long artistId, String album, long discordID,
                                 long guildId,
                                 int plays) {
        String queryString = """
                INSERT INTO album_crowns
                (artist_id,
                discordid,
                album,
                plays,
                guildid)
                VALUES
                (?,
                ?,
                ?,
                ?,
                ?)

                ON DUPLICATE KEY UPDATE
                  plays = ?,
                  discordid =  ?;""";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(i++, artistId);
            preparedStatement.setLong(i++, discordID);
            preparedStatement.setString(i++, album);
            preparedStatement.setInt(i++, plays);
            preparedStatement.setLong(i++, guildId);
            preparedStatement.setInt(i++, plays);
            preparedStatement.setLong(i, discordID);



            /* Execute query. */
            preparedStatement.executeUpdate();

            /* Get generated identifier. */

            /* Return booking. */

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }


    }

    @Override
    public Map<Long, Character> getGuildPrefixes(Connection connection, char defaultPrefix) {
        Map<Long, Character> returnedMap = new HashMap<>();
        String queryString = "SELECT guild_id, prefix FROM guild where prefix != ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            /* Fill "preparedStatement". */
            preparedStatement.setString(1, String.valueOf(defaultPrefix));



            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String prefix = resultSet.getString("prefix");
                long guildId = resultSet.getLong("guild_id");
                returnedMap.put(guildId, prefix.charAt(0));
            }

            return returnedMap;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void upsertGuildPrefix(Connection connection, long guildID, Character prefix) {
        String queryString = "UPDATE guild SET prefix =  ? WHERE guild_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setString(i++, String.valueOf(prefix));
            preparedStatement.setLong(i, guildID);
            /* Execute query. */
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void deleteAlbumCrown(Connection connection, String artist, String album, long discordID,
                                 long guildId) {
        String queryString = "DELETE   FROM album_crowns WHERE  artist_id = ? AND discordid = ? AND album = ?  AND guildid = ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setString(i++, artist);
            preparedStatement.setLong(i++, discordID);
            preparedStatement.setString(i++, album);
            preparedStatement.setLong(i, guildId);

            preparedStatement.executeUpdate();


        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void truncateRandomPool(Connection connection) {
        String queryString = "TRUNCATE randomlinks; ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            preparedStatement.executeUpdate();


        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void fillIds(Connection connection, List<ScrobbledArtist> list) {
        String queryString = "SELECT id, name FROM  artist WHERE name IN (%s)  ";
        String sql = String.format(queryString, list.isEmpty() ? null : preparePlaceHolders(list.size()));

        sql += " ORDER BY  name";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {


            for (int i = 0; i < list.size(); i++) {
                preparedStatement.setString(i + 1, list.get(i).getArtist());
            }
            /* Fill "preparedStatement". */
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<String, ScrobbledArtist> artistToScrobbled = list.stream().collect(Collectors.toMap(ScrobbledArtist::getArtist, Function.identity(), (scrobbledArtist, scrobbledArtist2) -> {
                scrobbledArtist.setCount(scrobbledArtist.getCount() + scrobbledArtist2.getCount());
                return scrobbledArtist;
            }));
            Pattern compile = Pattern.compile("\\p{M}");

            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                String name = resultSet.getString("name");
                ScrobbledArtist scrobbledArtist = artistToScrobbled.get(name);
                if (scrobbledArtist != null) {
                    scrobbledArtist.setArtistId(id);
                } else {
                    // name can be stripped or maybe the element is collect is the stripped one
                    String normalizeArtistName = compile.matcher(
                            Normalizer.normalize(name, Normalizer.Form.NFKD)
                    ).replaceAll("");
                    ScrobbledArtist normalizedArtist = artistToScrobbled.get(normalizeArtistName);
                    if (normalizedArtist != null) {
                        normalizedArtist.setArtistId(id);
                    }
                }
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void insertArtistSad(Connection connection, ScrobbledArtist nonExistingId) {

        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO artist (name,url,url_status,mbid) VALUES (?,?,?,?)" + " on duplicate key update correction_status = correction_status returning id")) {
            String artist = nonExistingId.getArtist();
            preparedStatement.setString(+1, artist);
            preparedStatement.setString(2, nonExistingId.getUrl());
            preparedStatement.setBoolean(3, nonExistingId.isUpdateBit());
            preparedStatement.setString(4, nonExistingId.getArtistMbid());

            preparedStatement.execute();

            ResultSet ids = preparedStatement.getResultSet();
            if (ids.next()) {
                nonExistingId.setArtistId(ids.getLong(1));
            } else {
                try {
                    if (artist.length() > 400) {
                        artist = artist.substring(0, 400);
                    }
                    long artistId = getArtistId(connection, artist);
                    nonExistingId.setArtistId(artistId);
                } catch (InstanceNotFoundException e) {
                    logger.warn("{} couldnt be inserted", artist);

                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public void insertArtists(Connection connection, List<ScrobbledArtist> nonExistingId) {

        String mySql = "INSERT INTO artist (name,url,url_status) VALUES (?,?,?)" + ",(?,?,?)".repeat(Math.max(0, nonExistingId.size() - 1)) +
                       " on duplicate key update correction_status = correction_status  returning id,name ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(mySql)) {
            for (int i = 0; i < nonExistingId.size(); i++) {
                preparedStatement.setString(3 * i + 1, nonExistingId.get(i).getArtist());
                preparedStatement.setString(3 * i + 2, nonExistingId.get(i).getUrl());
                preparedStatement.setBoolean(3 * i + 3, nonExistingId.get(i).isUpdateBit());
            }
            preparedStatement.execute();

            ResultSet ids = preparedStatement.getResultSet();
            int counter = 0;
            while (ids.next()) {
                long aLong = ids.getLong(1);
                nonExistingId.get(counter++).setArtistId(aLong);
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public long getArtistId(Connection connection, String artistName) throws InstanceNotFoundException {
        String queryString = "SELECT id, name FROM  artist WHERE name = ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setString(1, artistName);

            ResultSet execute = preparedStatement.executeQuery();
            if (execute.next()) {
                return execute.getLong(1);
            }
            throw new InstanceNotFoundException(artistName);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public long getAlbumIdByRYMId(Connection connection, Long rymId) throws InstanceNotFoundException {
        String queryString = "SELECT artist_id FROM  album WHERE rym_id = ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setLong(1, rymId);

            ResultSet execute = preparedStatement.executeQuery();
            if (execute.next()) {
                return execute.getLong(1);
            }
            throw new InstanceNotFoundException(rymId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public long getAlbumByName(Connection connection, String album, long artist_id) throws InstanceNotFoundException {
        String queryString = "SELECT id FROM  album WHERE album_name = ? and artist_id = ?  ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setLong(2, artist_id);
            preparedStatement.setString(1, album);


            ResultSet execute = preparedStatement.executeQuery();
            if (execute.next()) {
                return execute.getLong(1);
            }
            throw new InstanceNotFoundException(album);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    @Override
    public UpdaterUserWrapper getUserUpdateStatus(Connection connection, long discordId) throws
            InstanceNotFoundException {
        String queryString =
                "SELECT a.discord_id, a.role, a.lastfm_id, (if(last_update = '0000-00-00 00:00:00', '1971-01-01 00:00:01', last_update)) updating ,(if(control_timestamp = '0000-00-00 00:00:00', '1971-01-01 00:00:01', control_timestamp)) control, timezone " +
                "FROM user a   " +
                " WHERE a.discord_id = ?  ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            preparedStatement.setLong(1, discordId);

            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {

                String name = resultSet.getString("a.lastFm_Id");
                long discordID = resultSet.getLong("a.discord_ID");
                Timestamp timestamp = resultSet.getTimestamp("updating");
                Timestamp controlTimestamp = resultSet.getTimestamp("control");
                Role role = Role.valueOf(resultSet.getString("a.role"));
                TimeZone tz = TimeZone.getTimeZone(Objects.requireNonNullElse(resultSet.getString("timezone"), "GMT"));

                return new UpdaterUserWrapper(discordID, name, ((int) timestamp.toInstant()
                        .getEpochSecond()), ((int) controlTimestamp.toInstant().getEpochSecond()), role, tz);
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        throw new InstanceNotFoundException(discordId);
    }

    @Override
    public void addAlias(Connection connection, String alias, long toArtistId) throws DuplicateInstanceException {
        String queryString = "INSERT corrections(alias, artist_id) VALUES (?,?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setString(1, alias);
            preparedStatement.setLong(2, toArtistId);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new DuplicateInstanceException(alias);
        }
    }

    @Override
    public void queueAlias(Connection connection, String alias, long toArtistId, long whom) {
        String queryString = "INSERT queued_alias(alias, artist_id,discord_id) VALUES (?,?,?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setString(1, alias);
            preparedStatement.setLong(2, toArtistId);
            preparedStatement.setLong(3, whom);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public AliasEntity getNextInAliasQueue(Connection connection) {
        String queryString = "SELECT a.id,alias,a.artist_id,discord_id,added_date,b.name FROM   queued_alias a JOIN artist b ON a.artist_id = b.id ORDER BY added_date LIMIT 1;";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                long aliasId = resultSet.getLong(1);
                String alias = resultSet.getString(2);
                long artistId = resultSet.getLong(3);
                long discordID = resultSet.getLong(4);
                Timestamp date = resultSet.getTimestamp(5);
                String artistName = resultSet.getString(6);
                return new AliasEntity(aliasId, alias, artistId, discordID, date.toLocalDateTime(), artistName);
            }
            return null;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    @Override
    public ReportEntity getReportEntity(Connection connection, long maxIdAllowed, Set<Long> skippedIds) {

        String queryString = """
                SELECT  b.id,count(*) AS reportcount,b.score,
                min(a.report_date) AS l,b.added_date,b.discord_id,c.name,b.url,(SELECT count(*) FROM log_reported WHERE reported = b.discord_id),b.artist_id,a.id
                FROM reported a JOIN\s
                alt_url b ON a.alt_id = b.id JOIN
                artist c ON b.artist_id = c.id
                WHERE a.id <= ?\s""";
        if (!skippedIds.isEmpty()) {
            queryString += " and a.alt_id not in (" + "?,".repeat(skippedIds.size() - 1) + "?)";
        }
        queryString += "GROUP BY a.alt_id ORDER BY l DESC LIMIT 1";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setLong(1, maxIdAllowed);
            int i = 2;
            for (Long skippedId : skippedIds) {
                preparedStatement.setLong(i++, skippedId);
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                long altUrlId = resultSet.getLong(1);
                int reportCount = resultSet.getInt(2);
                int score = resultSet.getInt(3);
                Timestamp firstReport = resultSet.getTimestamp(4);
                Timestamp imageDate = resultSet.getTimestamp(5);
                long imageOwner = resultSet.getLong(6);
                String artistName = resultSet.getString(7);
                String url = resultSet.getString(8);
                int userReportCount = resultSet.getInt(9);
                long artistId = resultSet.getInt(10);
                long reportId = resultSet.getInt(11);

                return new ReportEntity(url, imageOwner, artistId,
                        altUrlId, firstReport.toLocalDateTime(), artistName,
                        imageDate.toLocalDateTime(), score, reportCount, userReportCount, reportId);
            }
            return null;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void deleteAliasById(Connection con, long aliasId) {
        String queryString = "DELETE FROM queued_alias WHERE id = ? ";

        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {
            preparedStatement.setLong(1, aliasId);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void updateUrlStatus(Connection con, long artistId, String spotifyId) {
        String queryString = "UPDATE artist SET url_status = 0, spotify_id = ?  WHERE id = ?";
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {
            preparedStatement.setString(1, spotifyId);
            preparedStatement.setLong(2, artistId);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public OptionalLong checkArtistUrlExists(Connection con, long artistId, String urlParsed) {
        String queryString = "SELECT id FROM alt_url WHERE artist_id = ? AND url = ? ";
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

            preparedStatement.setLong(1, artistId);
            preparedStatement.setString(2, urlParsed);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return OptionalLong.of(resultSet.getLong(1));
            }
            return OptionalLong.empty();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void removeVote(Connection con, long urlId, long discordId) {
        String queryString = "DELETE FROM vote WHERE discord_id = ? AND alt_id = ? ";
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {
            preparedStatement.setLong(1, discordId);
            preparedStatement.setLong(2, urlId);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public boolean castVote(Connection con, long urlId, long discordId, boolean isPositive) {
        String queryString = "INSERT IGNORE INTO vote(alt_id,discord_id,ispositive) VALUES (?,?,?) ON DUPLICATE KEY UPDATE  ispositive = ?";
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setLong(i++, urlId);
            preparedStatement.setLong(i++, discordId);
            preparedStatement.setBoolean(i++, isPositive);
            preparedStatement.setBoolean(i, isPositive);
            return preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void reportImage(Connection connection, long urlId, long userIdLong) {

        String queryString = "INSERT IGNORE INTO reported(alt_id,discord_id) VALUES (?,?) ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setLong(i++, urlId);
            preparedStatement.setLong(i, userIdLong);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public void removeImage(Connection connection, long altId) {
        String queryString = "DELETE FROM alt_url WHERE id = ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setLong(1, altId);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void logRemovedImage(Connection connection, long imageOwner, long modId) {
        String queryString = "INSERT  INTO log_reported(reported,modded) VALUES (?,?) ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setLong(i++, imageOwner);
            preparedStatement.setLong(i, modId);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public int getReportCount(Connection connection) {
        String queryString = "SELECT count(*) FROM (SELECT count(*) FROM reported GROUP BY reported.alt_id) a ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void removeReport(Connection connection, long altId) {
        String queryString = "DELETE FROM reported WHERE alt_id = ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setLong(1, altId);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void removeQueuedImage(Connection connection, long altId) {
        String queryString = "DELETE FROM queued_url WHERE id = ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setLong(1, altId);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    @Override
    public void insertPastRecommendation(Connection connection, long secondDiscordID, long firstDiscordID, long artistId) {
        String queryString = "INSERT IGNORE INTO past_recommendations ( artist_id,receiver_id,giver_id)   VALUES (?, ?,?)  ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setLong(i++, artistId);
            preparedStatement.setLong(i++, firstDiscordID);
            preparedStatement.setLong(i, secondDiscordID);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public void updateGuildCrownThreshold(Connection connection, long guildId, int newThreshold) {
        String queryString = "UPDATE guild SET crown_threshold = ? WHERE guild_id = ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setInt(i++, newThreshold);
            preparedStatement.setLong(i, guildId);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }


    @Override
    public ImageQueue getUrlQueue(Connection connection, long maxIdAllowed, Set<Long> skippedIds) {

        String queryString = """
                SELECT a.id,a.url,a.artist_id,a.discord_id,a.added_date, c.name,
                (SELECT count(*) FROM alt_url d WHERE d.discord_id = a.discord_id) as submitted,
                (SELECT count(*) FROM rejected WHERE rejected.discord_id = a.discord_id) as reportedCount,
                (SELECT count(*) FROM strike WHERE strike.discord_id = a.discord_id) as reportedCount,
                guild_id
                FROM queued_url a JOIN
                artist c ON a.artist_id = c.id
                WHERE a.id <= ?\s""";
        if (!skippedIds.isEmpty()) {
            queryString += " and a.id not in (" + "?,".repeat(skippedIds.size() - 1) + "?)";
        }
        queryString += "ORDER BY a.added_date DESC LIMIT 1";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setLong(1, maxIdAllowed);
            int i = 2;
            for (Long skippedId : skippedIds) {
                preparedStatement.setLong(i++, skippedId);
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                long queuedId = resultSet.getLong(1);
                String url = resultSet.getString(2);
                long artistId = resultSet.getInt(3);
                long uploader = resultSet.getLong(4);
                Timestamp addedDate = resultSet.getTimestamp(5);
                String artistName = resultSet.getString(6);
                int count = resultSet.getInt(7);
                int userReportCount = resultSet.getInt(8);
                int strikes = resultSet.getInt(9);
                long guildId = resultSet.getLong(10);
                return new ImageQueue(queuedId, url, artistId, uploader,
                        artistName, addedDate.toLocalDateTime(), userReportCount, count, strikes, guildId == 0 ? null : guildId);
            }
            return null;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void upsertQueueUrl(Connection connection, String url, long artistId, long discordId, Long guildId) {
        String queryString = "INSERT INTO queued_url(url,artist_id,discord_id,guild_id) values (?,?,?,?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setString(1, url);
            preparedStatement.setLong(2, artistId);
            preparedStatement.setLong(3, discordId);
            if (guildId == null) {
                preparedStatement.setNull(4, Types.BIGINT);
            } else {
                preparedStatement.setLong(4, guildId);

            }
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public OptionalLong checkQueuedUrlExists(Connection connection, long artistId, String urlParsed) {
        String queryString = "SELECT id FROM queued_url WHERE artist_id = ? AND url = ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            preparedStatement.setLong(1, artistId);
            preparedStatement.setString(2, urlParsed);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return OptionalLong.of(resultSet.getLong(1));
            }
            return OptionalLong.empty();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public int getQueueUrlCount(Connection connection) {

        String queryString = "SELECT count(*) FROM queued_url ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void deleteAllRatings(Connection con, long userId) {
        String queryString = "DELETE   FROM album_rating  WHERE discord_id = ? ";
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(i, userId);

            preparedStatement.executeUpdate();


        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void fillALbumsByRYMID(Connection connection, List<RYMImportRating> list) {
        String queryString = "SELECT id, album_name, artist_id,rym_id  FROM  album WHERE rym_id in (%s) ";
        String sql = String.format(queryString, list.isEmpty() ? null : preparePlaceHolders(list.size()));

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {


            for (int i = 0; i < list.size(); i++) {
                preparedStatement.setLong(i + 1, list.get(i).getRYMid());
            }
            /* Fill "preparedStatement". */
            ResultSet resultSet = preparedStatement.executeQuery();

            Map<Long, RYMImportRating> rymIdToRating = list.stream().collect(Collectors.toMap(RYMImportRating::getRYMid, Function.identity()));

            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                String name = resultSet.getString("album_name");
                long artist_id = resultSet.getLong("artist_id");
                long rym_id = resultSet.getLong("rym_id");
                RYMImportRating RYMImportRating = rymIdToRating.get(rym_id);
                RYMImportRating.setArtist_id(artist_id);
                RYMImportRating.setId(id);
                RYMImportRating.setRealAlbumName(name);
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void insertAlbumSad(Connection connection, RYMImportRating x) {


        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT IGNORE INTO album (artist_id,album_name,rym_id,release_year) VALUES (?,?,?,?)" + " ON DUPLICATE KEY UPDATE release_year =  LEAST(release_year,values(release_year)), rym_id = if(rym_id is null,values(rym_id),rym_id)", Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setLong(+1, x.getArtist_id());
            preparedStatement.setString(2, x.getTitle());
            preparedStatement.setLong(3, x.getRYMid());
            if (x.getYear() == null) {
                preparedStatement.setNull(4, Types.SMALLINT);
            } else {
                preparedStatement.setShort(4, (short) x.getYear().getValue());

            }

            preparedStatement.execute();

            ResultSet ids = preparedStatement.getResultSet();
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                x.setId(generatedKeys.getLong("GENERATED_KEY"));
            } else {
                try {
                    long albumId = getAlbumByName(connection, x.getTitle(), x.getArtist_id());
                    x.setId(albumId);
                } catch (InstanceNotFoundException e) {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void insertCombo(Connection connection, StreakEntity combo, long discordID, long artistId, @Nullable Long albumId) {

        String mySql = "INSERT INTO top_combos (artist_id,discord_id,album_id,track_name,artist_combo,album_combo,track_combo,streak_start) VALUES" +
                       " (?,?,?,?,?,?,?,?)" + " ON DUPLICATE KEY UPDATE " +
                       "artist_combo = if(artist_combo < values(artist_combo),values(artist_combo),artist_combo)," +
                       "album_combo = if(artist_combo < values(artist_combo),values(album_combo),album_combo)," +
                       "track_combo = if(artist_combo < values(artist_combo),values(track_combo),track_combo)," +
                       " album_id = if(album_combo > 1,values(album_id),null)," +
                       " track_name = if(track_combo > 1,values(track_name),null)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(mySql)) {
            preparedStatement.setLong(+1, artistId);
            preparedStatement.setLong(+2, discordID);
            if (albumId == null) {
                preparedStatement.setNull(+3, Types.BIGINT);
            } else {
                preparedStatement.setLong(+3, albumId);

            }
            if (combo.gettCounter() <= 1) {
                preparedStatement.setNull(+4, Types.VARCHAR);
            } else {
                preparedStatement.setString(4, combo.getCurrentSong());
            }
            preparedStatement.setInt(+5, combo.getaCounter());
            preparedStatement.setInt(+6, combo.getAlbCounter());
            preparedStatement.setInt(+7, combo.gettCounter());
            preparedStatement.setTimestamp(+8, Timestamp.from(combo.getStreakStart()));


            preparedStatement.execute();
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void addUrlRating(Connection connection, long author, int rating, String url) {
        String mySql = "INSERT INTO random_links_ratings (url,discord_id,rating) VALUES" +
                       " (?,?,?)" + " ON DUPLICATE KEY UPDATE " +
                       " rating = values(rating)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(mySql)) {
            preparedStatement.setString(+1, url);
            preparedStatement.setLong(+2, author);
            preparedStatement.setInt(+3, rating);
            preparedStatement.execute();
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public UsersWrapper getRandomUser(Connection connection) {
        String queryString =
                "SELECT a.discord_id,a.role, a.lastfm_id,(if(last_update = '0000-00-00 00:00:00', '1971-01-01 00:00:01', last_update)) updating,(if(control_timestamp = '0000-00-00 00:00:00', '1971-01-01 00:00:01', control_timestamp)) controling,timezone " +
                "FROM user a   " +
                "ORDER BY  rand() LIMIT 1";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */

            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {

                String name = resultSet.getString("a.lastFm_Id");
                long discordID = resultSet.getLong("a.discord_ID");
                Timestamp timestamp = resultSet.getTimestamp("updating");
                Timestamp controlTimestamp = resultSet.getTimestamp("controling");
                Role role = Role.valueOf(resultSet.getString("a.role"));
                TimeZone tz = TimeZone.getTimeZone(Objects.requireNonNullElse(resultSet.getString("timezone"), "GMT"));

                return new UpdaterUserWrapper(discordID, name, ((int) timestamp.toInstant()
                        .getEpochSecond()), ((int) controlTimestamp.toInstant().getEpochSecond()), role, tz);
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return null;

    }

    @Override
    public void updateMbids(Connection connection, List<ScrobbledArtist> artistData) {

        String mySql = "INSERT INTO artist (name,mbid) VALUES (?,?)" + ",(?,?)".repeat(Math.max(0, artistData.size() - 1)) +
                       " on duplicate key update mbid = values(mbid) ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(mySql)) {
            for (int i = 0; i < artistData.size(); i++) {
                preparedStatement.setString(2 * i + 1, artistData.get(i).getArtist());
                preparedStatement.setString(2 * i + 2, artistData.get(i).getArtistMbid());
            }
            preparedStatement.execute();

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }


    }

    @Override
    public void updateAlbumImage(Connection connection, long albumId, String albumUrl) {
        String queryString = "UPDATE album SET url = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */

            int i = 1;
            preparedStatement.setString(i++, albumUrl);
            preparedStatement.setLong(i, albumId);
            int i1 = preparedStatement.executeUpdate();
//            logger.warn(String.valueOf(i1));


        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }


    }

    @Override
    public List<ScrobbledAlbum> fillAlbumsByMBID(Connection connection, List<AlbumInfo> albums) {
        String queryString = "SELECT id,artist_id,mbid,url FROM  album WHERE mbid IN (%s)  ";
        String sql = String.format(queryString, albums.isEmpty() ? null : preparePlaceHolders(albums.size()));
        List<ScrobbledAlbum> scrobbledAlbums = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {


            for (int i = 0; i < albums.size(); i++) {
                preparedStatement.setString(i + 1, albums.get(i).getMbid());
            }
            /* Fill "preparedStatement". */
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<String, ScrobbledAlbum> mbidToAlbum = albums.stream().collect(Collectors.toMap(EntityInfo::getMbid, x -> new ScrobbledAlbum(x.getArtist(), 0, null, -1, x.getName(), null), (x, y) -> x));

            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                long artistId = resultSet.getLong("artist_id");
                String mbid = resultSet.getString("mbid");
                String url = resultSet.getString("url");
                ScrobbledAlbum scrobbledAlbum = mbidToAlbum.get(mbid);
                if (scrobbledAlbum == null) {
                    continue;
                }
                scrobbledAlbum.setArtistId(artistId);
                scrobbledAlbum.setAlbumId(id);
                scrobbledAlbum.setUrl(url);
                scrobbledAlbum.setAlbumMbid(mbid);
                scrobbledAlbums.add(scrobbledAlbum);
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return scrobbledAlbums;
    }

    @Override
    public void insertAlbumTags(Connection connection, Map<Genre, List<ScrobbledAlbum>> genres, Map<String, String> correctedTags) {

        List<Pair<Genre, ScrobbledAlbum>> list = genres.entrySet().stream().flatMap(x -> x.getValue().stream().map(t -> Pair.of(x.getKey(), t))).toList();

        String mySql = "INSERT ignore INTO  album_tags" +
                       "                  (artist_id,album_id,tag) VALUES (?, ?, ?) " + ", (?,?,?)".repeat(Math.max(0, list.size() - 1));
        try (PreparedStatement preparedStatement = connection.prepareStatement(mySql)) {
            for (int i = 0; i < list.size(); i++) {
                preparedStatement.setLong(3 * i + 1, list.get(i).getRight().getArtistId());
                preparedStatement.setLong(3 * i + 2, list.get(i).getRight().getAlbumId());
                String genreName = list.get(i).getLeft().getName();
                String s = correctedTags.get(genreName);
                if (s != null) {
                    genreName = s;
                }
                preparedStatement.setString(3 * i + 3, genreName);
            }
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public void insertArtistTags(Connection connection, Map<Genre, List<ScrobbledArtist>> genres, Map<String, String> correctedTags) {

        List<Pair<Genre, ScrobbledArtist>> list = genres.entrySet().stream().flatMap(x -> x.getValue().stream().map(t -> Pair.of(x.getKey(), t))).toList();

        String mySql = "INSERT ignore INTO  artist_tags" +
                       "                  (artist_id,tag) VALUES (?, ?) " + ", (?,?)".repeat(Math.max(0, list.size() - 1));
        try (PreparedStatement preparedStatement = connection.prepareStatement(mySql)) {
            for (int i = 0; i < list.size(); i++) {
                preparedStatement.setLong(2 * i + 1, list.get(i).getRight().getArtistId());
                String genreName = list.get(i).getLeft().getName();
                String s = correctedTags.get(genreName);
                if (s != null) {
                    genreName = s;
                }
                preparedStatement.setString(2 * i + 2, genreName);
            }
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }


    @Override
    public Map<String, String> validateTags(Connection connection, List<Genre> genreList) {
        Map<String, String> returnMap = new HashMap<>();
        String queryString = "Select invalid,correction from corrected_tags where invalid in (%s)";
        String sql = String.format(queryString, genreList.isEmpty() ? null : preparePlaceHolders(genreList.size()));
        List<ScrobbledAlbum> scrobbledAlbums = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            for (int i = 0; i < genreList.size(); i++) {
                preparedStatement.setString(i + 1, genreList.get(i).getName());
            }
            /* Fill "preparedStatement". */
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String invalid = resultSet.getString("invalid");
                String correction = resultSet.getString("correction");
                returnMap.put(invalid, correction);

            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return returnMap;
    }

    @Override
    public void addBannedTag(Connection connection, String tag) {


        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT ignore INTO  banned_tags  (tag) VALUES (?) ")) {
            preparedStatement.setString(1, tag);
            preparedStatement.executeUpdate();
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void logBannedTag(Connection connection, String tag, long discordId) {


        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT ignore INTO  log_tags  (tag,discord_id) VALUES (?,?) ")) {
            preparedStatement.setString(1, tag);
            preparedStatement.setLong(2, discordId);

            preparedStatement.executeUpdate();
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void removeTagWholeArtist(Connection connection, String tag) {


        try (PreparedStatement preparedStatement = connection.prepareStatement("delete from artist_tags  where tag = ? ")) {
            preparedStatement.setString(1, tag);
            preparedStatement.executeUpdate();
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void removeTagWholeAlbum(Connection connection, String tag) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("delete from album_tags  where tag = ? ")) {
            preparedStatement.setString(1, tag);
            preparedStatement.executeUpdate();
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void removeTagWholeTrack(Connection connection, String tag) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("delete from track_tags  where tag = ? ")) {
            preparedStatement.setString(1, tag);
            preparedStatement.executeUpdate();
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void addArtistBannedTag(Connection connection, String tag, long artistId) {


        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT ignore INTO  banned_artist_tags  (tag,artist_id) VALUES (?,?) ")) {
            preparedStatement.setString(1, tag);
            preparedStatement.setLong(2, artistId);

            preparedStatement.executeUpdate();
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public void removeTagArtist(Connection connection, String tag, long artistId) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("delete from artist_tags  where tag = ? and artist_id = ?  ")) {
            preparedStatement.setString(1, tag);
            preparedStatement.setLong(2, artistId);
            preparedStatement.executeUpdate();
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public void removeTagAlbum(Connection connection, String tag, long artistId) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("delete from album_tags  where tag = ? and artist_id = ?  ")) {
            preparedStatement.setString(1, tag);
            preparedStatement.setLong(2, artistId);
            preparedStatement.executeUpdate();
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public void removeTagTrack(Connection connection, String tag, long artistId) {
        try (PreparedStatement preparedStatement = connection.prepareStatement("delete from track_tags  where tag = ? and artist_id = ?  ")) {
            preparedStatement.setString(1, tag);
            preparedStatement.setLong(2, artistId);
            preparedStatement.executeUpdate();
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public void logCommand(Connection connection, long discordId, Long guildId, String commandName, long nanos, Instant utc, boolean success, boolean isNormalCommand) {


        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO  command_logs  (discord_id,guild_id,command,nanos,success,is_slash) VALUES (?,?,?,?,?,?) ")) {
            preparedStatement.setLong(1, discordId);
            if (guildId != null) {
                preparedStatement.setLong(2, guildId);
            } else {
                preparedStatement.setNull(2, Types.BIGINT);
            }
            preparedStatement.setString(3, commandName);
            preparedStatement.setLong(4, nanos);
            preparedStatement.setBoolean(5, success);
            preparedStatement.setBoolean(6, isNormalCommand);
            preparedStatement.executeUpdate();
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public void updateTrackImage(Connection connection, long trackId, String imageUrl) {
        String queryString = "UPDATE track SET url = ? WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setString(i++, imageUrl);
            preparedStatement.setLong(i, trackId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void updateSpotifyInfo(Connection connection, long trackId, String spotifyId, int duration, String url, int popularity) {
        String queryString = "UPDATE track SET spotify_id = ?, duration = ?,popularity = ?  WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setString(i++, spotifyId);
            preparedStatement.setInt(i++, duration);
            preparedStatement.setInt(i++, popularity);
            preparedStatement.setLong(i, trackId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void insertAudioFeatures(Connection connection, List<AudioFeatures> audioFeatures) {


        String queryString = "insert ignore  into audio_features(spotify_id,acousticness,danceability,energy,instrumentalness,`key`,liveness,loudness,speechiness,tempo,valence,time_signature) values " +
                             "(?,?,?,?,?,?,?,?,?,?,?,?)" + ",(?,?,?,?,?,?,?,?,?,?,?,?)".repeat(Math.max(0, audioFeatures.size() - 1));
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            for (int i = 0; i < audioFeatures.size(); i++) {
                preparedStatement.setString(12 * i + 1, audioFeatures.get(i).getId());
                preparedStatement.setFloat(12 * i + 2, audioFeatures.get(i).getAcousticness());
                preparedStatement.setFloat(12 * i + 3, audioFeatures.get(i).getDanceability());
                preparedStatement.setFloat(12 * i + 4, audioFeatures.get(i).getEnergy());
                preparedStatement.setFloat(12 * i + 5, audioFeatures.get(i).getInstrumentalness());
                preparedStatement.setInt(12 * i + 6, audioFeatures.get(i).getKey());
                preparedStatement.setFloat(12 * i + 7, audioFeatures.get(i).getLiveness());
                preparedStatement.setFloat(12 * i + 8, audioFeatures.get(i).getLoudness());
                preparedStatement.setFloat(12 * i + 9, audioFeatures.get(i).getSpeechiness());
                preparedStatement.setFloat(12 * i + 10, audioFeatures.get(i).getTempo());
                preparedStatement.setFloat(12 * i + 11, audioFeatures.get(i).getValence());
                preparedStatement.setInt(12 * i + 12, audioFeatures.get(i).getTimeSignature());
            }
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public void insertUserInfo(Connection connection, UserInfo userInfo) {
        String queryString = "insert ignore  into user_info(lastfm_id,profile_pic,login_moment) values (?,?,?) on duplicate key update profile_pic = values(profile_pic) ";


        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setString(1, userInfo.getUsername());
            preparedStatement.setString(2, userInfo.getImage());
            preparedStatement.setTimestamp(3, Timestamp.from(Instant.ofEpochSecond(userInfo.getUnixtimestamp())));
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public Optional<UserInfo> getUserInfo(Connection connection, String lastfmId) {
        String queryString = "select lastfm_id,profile_pic,login_moment,(select sum(playnumber) from scrobbled_artist where scrobbled_artist.lastfm_id = ? )  " +
                             " from user_info where lastfm_id = ?";


        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setString(1, lastfmId);
            preparedStatement.setString(2, lastfmId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String lastfm = resultSet.getString(1);
                String profilePic = resultSet.getString(2);
                int loginMoment = Math.toIntExact(resultSet.getTimestamp(3).toInstant().getEpochSecond());
                int scrobbleCount = resultSet.getInt(4);
                return Optional.of(new UserInfo(scrobbleCount, profilePic, lastfm, loginMoment));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void storeToken(Connection connection, String authToken, String lastfm) {
        String queryString = "UPDATE user SET token =  ? , sess = null WHERE lastfm_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setString(i++, authToken);
            preparedStatement.setString(i, lastfm);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void storeSession(Connection connection, String session, String lastfm) {
        String queryString = "UPDATE user SET token = null, sess = ?  WHERE lastfm_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setString(i++, session);
            preparedStatement.setString(i, lastfm);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void clearSess(Connection connection, String lastfm) {
        String queryString = "UPDATE user SET token = null, sess = null  WHERE lastfm_id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setString(i, lastfm);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public long storeRejected(Connection connection, ImageQueue reportEntity) {
        String queryString = "insert into rejected(url,artist_id,discord_id) values (?,?,?) returning id  ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setString(i++, reportEntity.url());
            preparedStatement.setLong(i++, reportEntity.artistId());
            preparedStatement.setLong(i, reportEntity.uploader());
            preparedStatement.executeUpdate();
            ResultSet resultSet = preparedStatement.getResultSet();
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            throw new ChuuServiceException(new RuntimeException("Nothing created while storing rejection"));
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void banUserImage(Connection connection, long uploader) {
        String queryString = "insert ignore into image_blocked(discord_id) values (?) ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setLong(i, uploader);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void addStrike(Connection connection, long uploader, long rejectedId) {
        String queryString = "insert into strike(discord_id,rejected_id) values (?,?)  ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setLong(i++, uploader);
            preparedStatement.setLong(i, rejectedId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public long userStrikes(Connection connection, long uploader) {
        String queryString = "select count(*) from strike where discord_id = ?  ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setLong(i, uploader);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            throw new ChuuServiceException(new RuntimeException(" No rows"));
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public void deleteRandomUrl(Connection connection, String url) {

        String queryString = "delete from randomlinks where url = ? ; ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setString(1, url);

            preparedStatement.executeUpdate();


        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public void insertTrackTags(Connection connection, Map<Genre, List<ScrobbledTrack>> genres, Map<String, String> correctedTags) {
        List<Pair<Genre, ScrobbledTrack>> list = genres.entrySet().stream().flatMap(x -> x.getValue().stream().map(t -> Pair.of(x.getKey(), t))).toList();

        String mySql = "INSERT ignore INTO  track_tags" +
                       "                  (artist_id,track_id,tag) VALUES (?, ?, ?) " + ", (?,?,?)".repeat(Math.max(0, list.size() - 1));
        try (PreparedStatement preparedStatement = connection.prepareStatement(mySql)) {
            for (int i = 0; i < list.size(); i++) {
                preparedStatement.setLong(3 * i + 1, list.get(i).getRight().getArtistId());
                preparedStatement.setLong(3 * i + 2, list.get(i).getRight().getTrackId());
                String genreName = list.get(i).getLeft().getName();
                String s = correctedTags.get(genreName);
                if (s != null) {
                    genreName = s;
                }
                preparedStatement.setString(3 * i + 3, genreName);
            }
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


}


