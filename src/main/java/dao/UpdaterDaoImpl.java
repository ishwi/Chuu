package dao;

import core.Chuu;
import core.exceptions.DuplicateInstanceException;
import core.exceptions.InstanceNotFoundException;
import dao.entities.*;
import org.intellij.lang.annotations.Language;

import java.sql.*;
import java.text.Normalizer;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UpdaterDaoImpl implements UpdaterDao {


    public static String preparePlaceHolders(int length) {
        return String.join(",", Collections.nCopies(length, "?"));
    }

    @Override
    public void addSrobbledArtists(Connection con, List<ScrobbledArtist> scrobbledArtists) {
        /* Create "queryString". */

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
            throw new RuntimeException(e);
        }
    }

    @Override
    public UpdaterUserWrapper getLessUpdated(Connection connection) {
        @Language("MariaDB") String queryString =
                "SELECT a.discord_id, a.lastfm_id,a.last_update,a.control_timestamp " +
                "FROM user a   " +
                "ORDER BY  control_timestamp LIMIT 1";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */

            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {

                String name = resultSet.getString("a.lastFm_Id");
                long discordID = resultSet.getLong("a.discord_ID");
                Timestamp timestamp = resultSet.getTimestamp("a.last_update");
                Timestamp controlTimestamp = resultSet.getTimestamp("a.control_timestamp");

                return new UpdaterUserWrapper(discordID, name, ((int) timestamp.toInstant()
                        .getEpochSecond()), ((int) controlTimestamp.toInstant().getEpochSecond()));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }
    }

    @Override
    public void upsertUrl(Connection con, ArtistInfo artistInfo) {
        /* Create "queryString". */
        String queryString = "INSERT  INTO  artist ( name,url)   VALUES (?, ?) ON DUPLICATE  KEY UPDATE url= ?  ";

        insertArtistInfo(con, artistInfo, queryString);
    }

    private void insertArtistInfo(Connection con, ArtistInfo artistInfo, String queryString) {
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setString(i++, artistInfo.getArtist());
            preparedStatement.setString(i++, artistInfo.getArtistUrl());
            preparedStatement.setString(i, artistInfo.getArtistUrl());


            /* Execute query. */
            preparedStatement.execute();
            ResultSet ids = preparedStatement.getResultSet();

            int counter = 0;
           /* if (ids.next()) {
                return ids.getLong(1);

            }*/
            /* Get generated identifier. */
//            throw new RuntimeException();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void upsertArtistsDetails(Connection con, List<ScrobbledArtist> scrobbledArtists) {
        /* Create "queryString". */
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
            throw new RuntimeException(e);
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
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Set<String> selectNullUrls(Connection connection, boolean doSpotifySearch) {
        Set<String> returnList = new HashSet<>();

        String queryString = doSpotifySearch ? "SELECT * FROM artist where url = '' and  url_status = 1 or url is null limit 20" :
                "SELECT * FROM artist where url is null limit 20";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            ResultSet resultSet = preparedStatement.executeQuery();


            /* Get generated identifier. */

            while (resultSet.next()) {
                returnList.add(resultSet.getString("name"));
            }

        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
        }
        return returnList;
    }

    @Override
    public void upsertSpotify(Connection con, ArtistInfo artistInfo) {
        /* Create "queryString". */
        String queryString = "INSERT  INTO  artist"
                             + " ( name,url,url_status) " + " VALUES (?, ?,0) ON DUPLICATE KEY UPDATE url= ? , url_status = 0 ";

        insertArtistInfo(con, artistInfo, queryString);
    }

    @Override
    public UpdaterStatus getUpdaterStatus(Connection connection, String artist) throws InstanceNotFoundException {
        String queryString = "SELECT a.id,url,correction_status FROM artist a " +
                             " WHERE a.name = ? ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setString(1, artist);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String url = resultSet.getString("url");
                boolean status = resultSet.getBoolean("correction_status");
                long artistId = resultSet.getLong("a.id");

                return new UpdaterStatus(url, status, artistId);
            }
            throw new InstanceNotFoundException(artist);
        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insertCorrection(Connection connection, long artistId, String correction) {
        @Language("MariaDB") String queryString = "INSERT INTO corrections"
                                                  + " (alias,artist_id) VALUES (?, ?) ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setString(i++, correction);
            preparedStatement.setLong(i, artistId);


            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateStatusBit(Connection connection, long artistId) {
        @Language("MariaDB") String queryString = "UPDATE artist SET correction_status = 1 WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(i, artistId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String findCorrection(Connection connection, String artist) {
        @Language("MariaDB") String queryString = "SELECT  name  FROM corrections JOIN artist a ON corrections.artist_id = a.id" +
                                                  " WHERE alias = ? ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setString(1, artist);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return (resultSet.getString(1));
            }

        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void updateMetric(Connection connection, int metricId, long value) {
        @Language("MariaDB") String queryString = "UPDATE   metrics SET value = value + ?  WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(i++, value);
            preparedStatement.setInt(i, metricId);

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAllArtists(Connection con, String id) {
        @Language("MariaDB") String queryString = "DELETE   FROM scrobbled_artist  WHERE lastfm_id = ? ";
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setString(i, id);

            preparedStatement.executeUpdate();


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean insertRandomUrl(Connection con, String url, long discordId, Long guildId) {
        String queryString = "INSERT INTO  randomlinks"
                             + " ( discord_id,url,guild_id) " + " VALUES (?,  ?, ?)";
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

            int i = 1;
            preparedStatement.setLong(i++, discordId);
            preparedStatement.setString(i++, url);
            preparedStatement.setLong(i, guildId);


            /* Execute query. */
            int rows = preparedStatement.executeUpdate();
            return rows != 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RandomUrlEntity getRandomUrl(Connection con) {
        String queryString = "SELECT * FROM randomlinks WHERE url IN " +
                             "    (SELECT url FROM (SELECT url FROM randomlinks ORDER BY RAND() LIMIT 1) random)\n" +
                             "        ";
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next())
                return null;

            String url = resultSet.getString("url");
            long discordID = resultSet.getLong("discord_Id");
            Long guildId = resultSet.getLong("guild_Id");
            return new RandomUrlEntity(url, discordID, guildId);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RandomUrlEntity findRandomUrlById(Connection con, String urlQ) {
        @Language("MariaDB") String queryString = "SELECT * " +
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
            long discordID = resultSet.getLong("discordId");
            long guildId = resultSet.getLong("guildId");
            return new RandomUrlEntity(url, discordID, guildId);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insertAlbumCrown(Connection connection, long artistId, String album, long discordID,
                                 long guildId,
                                 int plays) {
        String queryString = "INSERT INTO album_crowns\n" +
                             "(artist_id,\n" +
                             "discordid,\n" +
                             "album,\n" +
                             "plays,\n" +
                             "guildid)\n" +
                             "VALUES\n" +
                             "(?,\n" +
                             "?,\n" +
                             "?,\n" +
                             "?,\n" +
                             "?)\n" +
                             "\n" +
                             "ON DUPLICATE KEY UPDATE\n" +
                             "  plays = ?,\n" +
                             "  discordid =  ?;";
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
            throw new RuntimeException(e);
        }


    }

    @Override
    public Map<Long, Character> getGuildPrefixes(Connection connection) {
        Map<Long, Character> returnedMap = new HashMap<>();
        String queryString = "SELECT guild_id, prefix FROM guild;";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            /* Fill "preparedStatement". */




            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String prefix = resultSet.getString("prefix");
                long guildId = resultSet.getLong("guild_id");
                returnedMap.put(guildId, prefix.charAt(0));
            }

            return returnedMap;
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAlbumCrown(Connection connection, String artist, String album, long discordID,
                                 long guildId) {
        @Language("MariaDB") String queryString = "DELETE   FROM album_crowns WHERE  artist_id = ? AND discordid = ? AND album = ?  AND guildid = ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setString(i++, artist);
            preparedStatement.setLong(i++, discordID);
            preparedStatement.setString(i++, album);
            preparedStatement.setLong(i, guildId);

            preparedStatement.executeUpdate();


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void truncateRandomPool(Connection connection) {
        @Language("MariaDB") String queryString = "TRUNCATE randomlinks; ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            preparedStatement.executeUpdate();


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void fillIds(Connection connection, List<ScrobbledArtist> list) {
        String queryString = "SELECT id, name FROM  artist WHERE name IN (%s)  ";
        String sql = String.format(queryString, list.size() == 0 ? null : preparePlaceHolders(list.size()));

        sql += " ORDER BY  name";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {


            for (int i = 0; i < list.size(); i++) {
                preparedStatement.setString(i + 1, list.get(i).getArtist());
            }
            /* Fill "preparedStatement". */
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<String, ScrobbledArtist> collect = list.stream().collect(Collectors.toMap(ScrobbledArtist::getArtist, Function.identity()));
            Pattern compile = Pattern.compile("\\p{M}");

            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                String name = resultSet.getString("name");
                ScrobbledArtist scrobbledArtist = collect.get(name);
                if (scrobbledArtist != null) {
                    scrobbledArtist.setArtistId(id);
                } else {
                    // name can be stripped or maybe the element is collect is the stripped one
                    String normalizeArtistName = compile.matcher(
                            Normalizer.normalize(name, Normalizer.Form.NFKD)
                    ).replaceAll("");
                    ScrobbledArtist normalizedArtist = collect.get(normalizeArtistName);
                    if (normalizedArtist != null) {
                        normalizedArtist.setArtistId(id);
                    }
                }
            }
            collect = null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insertArtistSad(Connection connection, ScrobbledArtist nonExistingId) {
        StringBuilder mySql =
                new StringBuilder("INSERT INTO artist (name,url,url_status) VALUES (?,?,?)");
        mySql.append(" on duplicate key update correction_status = correction_status");

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(mySql.toString(), Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(+1, nonExistingId.getArtist());
            preparedStatement.setString(2, nonExistingId.getUrl());
            preparedStatement.setBoolean(3, nonExistingId.isUpdateBit());
            preparedStatement.execute();

            ResultSet ids = preparedStatement.getResultSet();
            int counter = 0;
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    nonExistingId.setArtistId(generatedKeys.getLong("GENERATED_KEY"));
                } else {
                    try {
                        long artistId = getArtistId(connection, nonExistingId.getArtist());
                        nonExistingId.setArtistId(artistId);
                    } catch (InstanceNotFoundException e) {
                        throw new SQLException("Creating user failed, no ID obtained.");
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (
                SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void insertArtists(Connection connection, List<ScrobbledArtist> nonExistingId) {
        StringBuilder mySql =
                new StringBuilder("INSERT INTO artist (name,url,url_status) VALUES (?,?,?)");

        mySql.append(",(?,?,?)".repeat(Math.max(0, nonExistingId.size() - 1)));
        mySql.append(" on duplicate key update correction_status = correction_status  returning id,name ");

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(mySql.toString());
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
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getArtistId(Connection connection, String artistName) throws InstanceNotFoundException {
        @Language("MariaDB") String queryString = "SELECT id, name FROM  artist WHERE name = ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setString(1, artistName);

            ResultSet execute = preparedStatement.executeQuery();
            if (execute.next()) {
                return execute.getLong(1);
            }
            throw new InstanceNotFoundException(artistName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UpdaterUserWrapper getUserUpdateStatus(Connection connection, long discordId) throws
            InstanceNotFoundException {
        @Language("MariaDB") String queryString =
                "SELECT a.discord_id, a.lastfm_id,a.last_update,a.control_timestamp " +
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
                Timestamp timestamp = resultSet.getTimestamp("a.last_update");
                Timestamp controlTimestamp = resultSet.getTimestamp("a.control_timestamp");

                return new UpdaterUserWrapper(discordID, name, ((int) timestamp.toInstant()
                        .getEpochSecond()), ((int) controlTimestamp.toInstant().getEpochSecond()));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAliasById(Connection con, long aliasId) {
        String queryString = "DELETE FROM queued_alias WHERE id = ? ";

        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {
            preparedStatement.setLong(1, aliasId);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}


