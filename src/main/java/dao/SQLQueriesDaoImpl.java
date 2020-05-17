package dao;

import core.Chuu;
import core.exceptions.ChuuServiceException;
import dao.entities.*;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.*;

public class SQLQueriesDaoImpl implements SQLQueriesDao {


    @Override
    public void getGlobalRank(Connection connection, String lastfmId) {
        // TODO ONE DAY
    }

    @Override
    public UniqueWrapper<ArtistPlays> getGlobalCrowns(Connection connection, String lastfmId, int threshold) {
        List<ArtistPlays> returnList = new ArrayList<>();
        long discordId;

        @Language("MariaDB") String queryString = "SELECT c.name , b.discord_id , playnumber AS orden" +
                " FROM  scrobbled_artist  a" +
                " JOIN user b ON a.lastfm_id = b.lastfm_id" +
                " JOIN artist c ON " +
                " a.artist_id = c.id" +
                " WHERE  a.lastfm_id = ?" +
                " AND playnumber >= ?" +
                " AND  playnumber >= ALL" +
                "       (SELECT max(b.playnumber) " +
                " FROM " +
                "(SELECT in_a.artist_id,in_a.playnumber" +
                " FROM scrobbled_artist in_a  " +
                " JOIN " +
                " user in_b" +
                " ON in_a.lastfm_id = in_b.lastfm_id" +
                "   ) AS b" +
                " WHERE b.artist_id = a.artist_id" +
                " GROUP BY artist_id)" +
                " ORDER BY orden DESC";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setString(1, lastfmId);
            preparedStatement.setInt(2, threshold);


            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                return new UniqueWrapper<>(0, 0, lastfmId, returnList);

            } else {
                discordId = resultSet.getLong("b.discord_id");
                resultSet.beforeFirst();
            }

            while (resultSet.next()) {

                String artist = resultSet.getString("c.name");
                int plays = resultSet.getInt("orden");
                returnList.add(new ArtistPlays(artist, plays));
            }
        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return new UniqueWrapper<>(returnList.size(), discordId, lastfmId, returnList);

    }

    @Override
    public UniqueWrapper<ArtistPlays> getGlobalUniques(Connection connection, String lastfmId) {

        @Language("MariaDB") String queryString = "SELECT a.name, temp.playnumber, temp.lastfm_id, temp.discord_id " +
                "FROM(  " +
                "       SELECT artist_id, playnumber, a.lastfm_id ,b.discord_id" +
                "       FROM scrobbled_artist a JOIN user b " +
                "       ON a.lastfm_id = b.lastfm_id " +
                "       WHERE  a.playnumber > 2 " +
                "       GROUP BY a.artist_id " +
                "       HAVING count( *) = 1) temp " +
                " JOIN artist a ON temp.artist_id = a.id " +
                "WHERE temp.lastfm_id = ? AND temp.playnumber > 1 " +
                " ORDER BY temp.playnumber DESC ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setString(i, lastfmId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                return new UniqueWrapper<>(0, 0, lastfmId, new ArrayList<>());
            }

            List<ArtistPlays> returnList = new ArrayList<>();
            resultSet.last();
            int rows = resultSet.getRow();
            long discordId = resultSet.getLong("temp.discord_id");

            resultSet.beforeFirst();
            /* Get results. */

            while (resultSet.next()) {
                String name = resultSet.getString("a.name");
                int countA = resultSet.getInt("temp.playNumber");

                returnList.add(new ArtistPlays(name, countA));

            }
            return new UniqueWrapper<>(rows, discordId, lastfmId, returnList);


        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public ResultWrapper<ArtistPlays> getArtistPlayCount(Connection connection, Long guildId) {
        @Language("MariaDB") String queryBody =
                "FROM  (SELECT artist_id,playnumber " +
                        "FROM scrobbled_artist a" +
                        " JOIN user b  " +
                        " ON a.lastfm_id = b.lastfm_id " +
                        " JOIN user_guild c " +
                        " ON b.discord_id = c.discord_id" +
                        " WHERE c.guild_id = ?) main" +
                        " JOIN artist b ON" +
                        " main.artist_id = b.id ";

        String normalQuery = "SELECT b.name, sum(playNumber) AS orden " + queryBody + " GROUP BY main.artist_id ORDER BY orden DESC  Limit 200";
        String countQuery = "Select sum(playnumber) " + queryBody;
        return getArtistPlaysResultWrapper(connection, guildId, normalQuery, countQuery);
    }

    @NotNull
    private ResultWrapper<ArtistPlays> getArtistPlaysResultWrapper(Connection connection, Long guildId, String normalQuery, String countQuery) {
        try (PreparedStatement preparedStatement2 = connection.prepareStatement(countQuery)) {
            preparedStatement2.setLong(1, guildId);

            ResultSet resultSet = preparedStatement2.executeQuery();
            if (!resultSet.next()) {
                throw new ChuuServiceException();
            }
            int rows = resultSet.getInt(1);
            try (PreparedStatement preparedStatement = connection.prepareStatement(normalQuery)) {
                preparedStatement.setLong(1, guildId);
                return getArtistPlaysResultWrapper(rows, preparedStatement);
            }
        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public ResultWrapper<ArtistPlays> getArtistsFrequencies(Connection connection, Long guildId) {
        @Language("MariaDB") String queryBody =
                "FROM  (SELECT artist_id " +
                        "FROM scrobbled_artist a" +
                        " JOIN user b  " +
                        " ON a.lastfm_id = b.lastfm_id " +
                        " JOIN user_guild c " +
                        " ON b.discord_id = c.discord_id" +
                        " WHERE c.guild_id = ?) main" +
                        " JOIN artist b ON" +
                        " main.artist_id = b.id ";

        String normalQuery = "SELECT b.name, count(*) AS orden " + queryBody + " GROUP BY b.id ORDER BY orden DESC  Limit 200";
        String countQuery = "Select count(*) " + queryBody;
        return getArtistPlaysResultWrapper(connection, guildId, normalQuery, countQuery);
    }

    @NotNull
    private ResultWrapper<ArtistPlays> getArtistPlaysResultWrapper(int rows, PreparedStatement preparedStatement) throws SQLException {
        ResultSet resultSet;
        resultSet = preparedStatement.executeQuery();
        List<ArtistPlays> returnList = new ArrayList<>();
        while (resultSet.next()) {
            String name = resultSet.getString("b.name");
            int count = resultSet.getInt("orden");
            returnList.add(new ArtistPlays(name, count));
        }

        return new ResultWrapper<>(rows, returnList);
    }


    @Override
    public ResultWrapper<ArtistPlays> getGlobalArtistPlayCount(Connection connection) {
        @Language("MariaDB") String queryString =
                "FROM  scrobbled_artist a" +
                        " JOIN artist b " +
                        " ON a.artist_id = b.id ";


        String normalQuery = "SELECT b.name, sum(playNumber) AS orden " + queryString + "     GROUP BY artist_id  ORDER BY orden DESC  Limit 200";
        String countQuery = "Select sum(playNumber) " + queryString;

        return getArtistPlaysResultWrapper(connection, normalQuery, countQuery);

    }

    @NotNull
    private ResultWrapper<ArtistPlays> getArtistPlaysResultWrapper(Connection connection, String normalQuery, String countQuery) {
        int rows;
        try (PreparedStatement preparedStatement2 = connection.prepareStatement(countQuery)) {

            ResultSet resultSet = preparedStatement2.executeQuery();
            if (!resultSet.next()) {
                throw new ChuuServiceException();
            }
            rows = resultSet.getInt(1);
            try (PreparedStatement preparedStatement = connection.prepareStatement(normalQuery)) {


                return getArtistPlaysResultWrapper(rows, preparedStatement);
            }
        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public ResultWrapper<ArtistPlays> getGlobalArtistFrequencies(Connection connection) {
        @Language("MariaDB") String queryString =
                "FROM  scrobbled_artist a" +
                        " JOIN artist b " +
                        " ON a.artist_id = b.id ";


        String normalQuery = "SELECT b.name, count(*) AS orden " + queryString + "     GROUP BY artist_id  ORDER BY orden DESC  Limit 200";
        String countQuery = "Select count(*)" + queryString;
        return getArtistPlaysResultWrapper(connection, normalQuery, countQuery);

    }

    @Override
    public List<ScrobbledArtist> getAllUsersArtist(Connection connection, long discordId) {
        List<ScrobbledArtist> scrobbledArtists = new ArrayList<>();
        String queryString = " Select * from scrobbled_artist a join artist b on a.artist_id = b.id join user" +
                " c on a.lastfm_id = c.lastfm_id where c.lastfmid = ? order by a.plays desc";
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setLong(1, discordId);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String artistName = resultSet.getString("b.name");
                String artistUrl = resultSet.getString("b.url");
                int plays = resultSet.getInt("a.playNumber");
                scrobbledArtists.add(new ScrobbledArtist(artistName, artistUrl, plays));
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return scrobbledArtists;
    }

    @Override
    public List<LbEntry> matchingArtistCount(Connection connection, long userId, long guildId, Long threshold) {

        String queryString = "        select discord_id,lastfm_id,count(*) as orden\n" +
                "        from\n" +
                "                (select artist_id,a.lastfm_id,b.discord_id from scrobbled_artist a\n" +
                "                        join user b\n" +
                "                        on a.lastfm_id = b.lastfm_id\n" +
                "                        join user_guild c\n" +
                "                        on c.discord_id = b.discord_id\n" +
                "                        where c.guild_id = ?\n" +
                "                and b.discord_id != ?) main\n" +
                "\n" +
                "\n" +
                "        where main.artist_id in\n" +
                "        (Select artist_id from scrobbled_artist a join user b on a.lastfm_id = b.lastfm_id where\n" +
                "        discord_id = ?\n" +
                "	)\n" +
                "        group by lastfm_id,discord_id\n" +
                "        order by orden desc;\n";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setLong(i++, guildId);
            preparedStatement.setLong(i++, userId);
            preparedStatement.setLong(i, userId);

            List<LbEntry> returnedList = new ArrayList<>();

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) { //&& (j < 10 && j < rows)) {
                String lastfmId = resultSet.getString("lastfm_id");
                long discordId = resultSet.getLong("discord_id");
                int crowns = resultSet.getInt("orden");

                returnedList.add(new ArtistLbEntry(lastfmId, discordId, crowns));


            }
            return returnedList;
        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException((e));
        }
    }

    @Override
    public List<VotingEntity> getAllArtistImages(Connection connection, long artistId) {
        List<VotingEntity> returnedList = new ArrayList<>();
        String queryString = " Select a.id,a.url,a.score,a.discord_id,a.added_date,b.name,b.id,(select count(*) from vote where alt_id = a.id) as totalVotes from alt_url a join artist b on a.artist_id = b.id  where a.artist_id = ? order by a.score desc , added_date asc ";
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setLong(1, artistId);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String artistName = resultSet.getString("b.name");
                String artistUrl = resultSet.getString("a.url");
                long owner = resultSet.getLong("a.discord_id");
                artistId = resultSet.getLong("b.id");
                long urlId = resultSet.getLong("a.id");

                int votes = resultSet.getInt("a.score");
                int totalVotes = resultSet.getInt("totalVotes");
                Timestamp date = resultSet.getTimestamp("a.added_date");
                returnedList.add(new VotingEntity(artistName, artistUrl, date.toLocalDateTime(), owner, artistId, votes, totalVotes, urlId));
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return returnedList;
    }

    @Override
    public Boolean hasUserVotedImage(Connection connection, long urlId, long discordId) {
        String queryString = "Select ispositive from vote where alt_id = ? and discord_id = ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setLong(1, urlId);
            preparedStatement.setLong(2, discordId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getBoolean(1);
            }
            return null;

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    @Override
    public List<String> getArtistAliases(Connection connection, long artistId) {
        @Language("MariaDB") String queryString = "SELECT alias FROM corrections WHERE artist_id = ? ";
        List<String> returnList = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setLong(i, artistId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) { //&& (j < 10 && j < rows)) {
                String name = resultSet.getString("alias");
                returnList.add(name);
            }
            return returnList;

        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }


    }

    @Override
    public long getArtistPlays(Connection connection, Long guildID, long artistId) {
        @Language("MariaDB") String queryString = "SELECT sum(playnumber) FROM scrobbled_artist a " +
                "JOIN user b " +
                "ON a.lastfm_id = b.lastfm_id " +
                "JOIN user_guild c " +
                " ON b.discord_id = c.discord_id " +
                " WHERE  artist_id = ?";
        if (guildID != null) {
            queryString += " and c.guild_id = ?";
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setLong(1, artistId);
            if (guildID != null) {
                preparedStatement.setLong(2, guildID);
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public long getArtistFrequencies(Connection connection, Long guildID, long artistId) {

        @Language("MariaDB") String queryBody =
                "Select count(*) from  scrobbled_artist where artist_id = ? \n";

        if (guildID != null) queryBody += "and lastfm_id in (\n" +
                "(SELECT lastfm_id from user b \n" +
                " JOIN user_guild c \n" +
                " ON b.discord_id = c.discord_id\n" +
                " WHERE c.guild_id = ?))";
        else queryBody += "";

        try (PreparedStatement preparedStatement2 = connection.prepareStatement(queryBody)) {
            preparedStatement2.setLong(1, artistId);
            if (guildID != null)
                preparedStatement2.setLong(2, guildID);

            ResultSet resultSet = preparedStatement2.executeQuery();
            if (!resultSet.next()) {
                return 0;
            }
            return resultSet.getLong(1);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public int getGuildCrownThreshold(Connection connection, long guildID) {
        @Language("MariaDB") String queryBody = "SELECT crown_threshold FROM guild WHERE guild_id = ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryBody)) {
            preparedStatement.setLong(1, guildID);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        } catch (SQLException ex) {
            throw new ChuuServiceException(ex);
        }
    }

    @Override
    public List<LbEntry> getScrobblesLeaderboard(Connection connection, long guildId) {
        @Language("MariaDB") String queryBody = "SELECT b.lastfm_id,b.discord_id, sum(a.playnumber) AS ord " +
                "FROM scrobbled_artist a JOIN user b ON a.lastfm_id = b.lastfm_id " +
                "JOIN user_guild ug ON b.discord_id = ug.discord_id " +
                "WHERE ug.guild_id = ? " +
                "GROUP BY b.discord_id,b.lastfm_id " +
                "ORDER BY ord DESC ";
        return getLbEntries(connection, guildId, queryBody, ScrobbleLbEntry::new, false, -1);
    }

    @Override
    public List<CrownableArtist> getCrownable(Connection connection, Long discordId, Long guildId, boolean skipCrowns) {
        List<CrownableArtist> list = new ArrayList<>();
        String guildQuery;
        if (guildId != null) {
            guildQuery = "SELECT * \n" +
                    "FROM   (SELECT temp_2.name                                    AS name, \n" +
                    "               temp_2.artist_id                               AS artist, \n" +
                    "               temp_2.playnumber                              AS plays, \n" +
                    "               Max(inn.playnumber)                            AS maxPlays, \n" +
                    "               (SELECT Count(*) \n" +
                    "                FROM   scrobbled_artist b \n" +
                    "                       JOIN user d \n" +
                    "                         ON b.lastfm_id = d.lastfm_id \n" +
                    "                       JOIN user_guild c \n" +
                    "                         ON d.discord_id = c.discord_id \n" +
                    "                WHERE  c.guild_id = ? \n" +
                    "                       AND artist_id = temp_2.artist_id \n" +
                    "                       AND temp_2.playnumber <= b.playnumber) rank, \n" +
                    "               Count(*)                                       AS total \n" +
                    "        FROM   (SELECT b.* \n" +
                    "                FROM   scrobbled_artist b \n" +
                    "                       JOIN user d \n" +
                    "                         ON b.lastfm_id = d.lastfm_id \n" +
                    "                       JOIN user_guild c \n" +
                    "                         ON d.discord_id = c.discord_id \n" +
                    "                WHERE  c.guild_id = ?) inn \n" +
                    "               JOIN (SELECT artist_id, \n" +
                    "                            inn_c.name AS name, \n" +
                    "                            playnumber \n" +
                    "                     FROM   scrobbled_artist inn_a \n" +
                    "                            JOIN artist inn_c \n" +
                    "                              ON inn_a.artist_id = inn_c.id \n" +
                    "                            JOIN user inn_b \n" +
                    "                              ON inn_a.lastfm_id = inn_b.lastfm_id \n" +
                    "                            JOIN user_guild c \n" +
                    "                              ON inn_b.discord_id = c.discord_id \n" +
                    "                     WHERE  c.guild_id = ? \n" +
                    "                            AND inn_b.discord_id = ?) temp_2 \n" +
                    "                 ON temp_2.artist_id = inn.artist_id \n" +
                    "        GROUP  BY temp_2.artist_id \n" +
                    "        ORDER  BY temp_2.playnumber DESC) main \n" +
                    "WHERE  rank != 1 \n" +
                    "        or not ? \n" +
                    "LIMIT  500 ";
        } else {
            guildQuery = "Select * from (SELECT temp_2.name as name, temp_2.artist_id as artist , \n" +
                    "       temp_2.playnumber as plays, \n" +
                    "       Max(inn.playnumber) as maxPlays, \n" +
                    "       (SELECT Count(*) \n" +
                    "        FROM   scrobbled_artist b \n" +
                    "        WHERE  artist_id = temp_2.artist_id \n" +
                    "               AND temp_2.playnumber <= b.playnumber) as rank, count(*) as total\n" +
                    "FROM   scrobbled_artist inn \n" +
                    "       JOIN (SELECT artist_id,inn_c.name as name,\n" +
                    "                    playnumber \n" +
                    "             FROM   scrobbled_artist inn_a \n" +
                    "                      join artist inn_c on inn_a.artist_id = inn_c.id                     \n" +
                    "                       JOIN user inn_b \n" +
                    "                      ON inn_a.lastfm_id = inn_b.lastfm_id \n" +
                    "             WHERE  inn_b.discord_id = ?) temp_2 \n" +
                    "         ON temp_2.artist_id = inn.artist_id \n" +
                    "GROUP  BY temp_2.artist_id \n" +
                    "ORDER  BY temp_2.playnumber DESC \n" +
                    ") main " +
                    " where rank != 1 or not ? " +
                    "limit 500;";
        }


        int count = 0;
        int i = 1;
        try (PreparedStatement preparedStatement1 = connection.prepareStatement(guildQuery)) {
            if (guildId != null) {
                preparedStatement1.setLong(i++, guildId);
                preparedStatement1.setLong(i++, guildId);
                preparedStatement1.setLong(i++, guildId);
            }
            preparedStatement1.setLong(i++, discordId);
            preparedStatement1.setBoolean(i++, skipCrowns);


            ResultSet resultSet1 = preparedStatement1.executeQuery();

            while (resultSet1.next()) {
                String artist = resultSet1.getString("name");

                int plays = resultSet1.getInt("plays");
                int rank = resultSet1.getInt("rank");
                int total = resultSet1.getInt("total");
                int maxPlays = resultSet1.getInt("maxPlays");

                CrownableArtist who = new CrownableArtist(artist, plays, maxPlays, rank, total);
                list.add(who);
            }

        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return list;
    }

    @Override
    public Map<Long, Float> getRateLimited(Connection connection) {
        @Language("MariaDB") String queryString = "SELECT discord_id,queries_second  FROM rate_limited ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */


            Map<Long, Float> returnList = new HashMap<>();
            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                long guildId = resultSet.getLong("discord_id");
                float queries_second = resultSet.getFloat("queries_second");

                returnList.put(guildId, queries_second);

            }
            return returnList;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }


    @Override
    public List<ScrobbledArtist> getRecommendations(Connection connection, long giverDiscordId,
                                                    long receiverDiscordId, boolean doPast, int limit) {
        @Language("MariaDB") String queryBody = "SELECT \n" +
                "b.name,b.url,b.id,a.playnumber \n" +
                "FROM scrobbled_artist a \n" +
                "JOIN artist b ON a.artist_id = b.id \n" +
                "JOIN user c ON a.lastfm_id = c.lastfm_id " +
                "WHERE c.discord_id =  ? \n" +
                " AND  (?  OR (a.artist_id NOT IN (SELECT r.artist_id FROM past_recommendations r WHERE receiver_id =  ? ))) " +
                "AND a.artist_id NOT IN " +
                "(SELECT in_b.artist_id FROM scrobbled_artist in_b " +
                " JOIN user in_c ON in_b.lastfm_id = in_c.lastfm_id WHERE in_c.discord_id = ? ) ORDER BY a.playnumber DESC LIMIT ?";

        List<ScrobbledArtist> scrobbledArtists = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryBody)) {
            int i = 1;
            preparedStatement.setLong(i++, giverDiscordId);
            preparedStatement.setBoolean(i++, doPast);
            preparedStatement.setLong(i++, receiverDiscordId);

            preparedStatement.setLong(i++, receiverDiscordId);
            preparedStatement.setInt(i, limit);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String url = resultSet.getString("url");
                long id = resultSet.getLong("id");
                int playnumber = resultSet.getInt("playnumber");

                ScrobbledArtist scrobbledArtist = new ScrobbledArtist(name, playnumber, url);
                scrobbledArtist.setArtistId(id);
                scrobbledArtists.add(scrobbledArtist);
            }
            return scrobbledArtists;
        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
    }


    @Override
    public UniqueWrapper<ArtistPlays> getUniqueArtist(Connection connection, Long guildID, String lastfmId) {
        @Language("MariaDB") String queryString = "SELECT * " +
                "FROM(  " +
                "       SELECT a2.name, playnumber, a.lastfm_id ,b.discord_id" +
                "       FROM scrobbled_artist a JOIN user b " +
                "       ON a.lastfm_id = b.lastfm_id " +
                "       JOIN user_guild c ON b.discord_id = c.discord_id " +
                " JOIN artist a2 ON a.artist_id = a2.id " +
                "       WHERE c.guild_id = ? AND a.playnumber > 2 " +
                "       GROUP BY a.artist_id " +
                "       HAVING count( *) = 1) temp " +
                "WHERE temp.lastfm_id = ? AND temp.playnumber > 1 " +
                " ORDER BY temp.playnumber DESC ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setLong(i++, guildID);
            preparedStatement.setString(i, lastfmId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                return new UniqueWrapper<>(0, 0, lastfmId, new ArrayList<>());
            }

            List<ArtistPlays> returnList = new ArrayList<>();
            resultSet.last();
            int rows = resultSet.getRow();
            long discordId = resultSet.getLong("temp.discord_id");

            resultSet.beforeFirst();
            /* Get results. */

            while (resultSet.next()) {
                String name = resultSet.getString("temp.name");
                int countA = resultSet.getInt("temp.playNumber");

                returnList.add(new ArtistPlays(name, countA));

            }
            return new UniqueWrapper<>(rows, discordId, lastfmId, returnList);


        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
    }


    @Override
    public ResultWrapper<UserArtistComparison> similar(Connection connection, List<String> lastfMNames, int limit) {
        String userA = lastfMNames.get(0);
        String userB = lastfMNames.get(1);

        @Language("MariaDB") String queryString =
                "SELECT c.name  , a.playnumber,b.playnumber ," +
                        "((a.playnumber + b.playnumber)/(abs(a.playnumber-b.playnumber)+1))  *" +
                        " (((a.playnumber + b.playnumber)) * 2.5) * " +
                        " IF((a.playnumber > 10 * b.playnumber OR b.playnumber > 10 * a.playnumber) AND LEAST(a.playnumber,b.playnumber) < 400 ,0.01,2) " +
                        "media ," +
                        " c.url " +
                        "FROM " +
                        "(SELECT artist_id,playnumber " +
                        "FROM scrobbled_artist " +
                        "JOIN user b ON scrobbled_artist.lastfm_id = b.lastfm_id " +
                        "WHERE b.lastfm_id = ? ) a " +
                        "JOIN " +
                        "(SELECT artist_id,playnumber " +
                        "FROM scrobbled_artist " +
                        " JOIN user b ON scrobbled_artist.lastfm_id = b.lastfm_id " +
                        " WHERE b.lastfm_id = ? ) b " +
                        "ON a.artist_id=b.artist_id " +
                        "JOIN artist c " +
                        "ON c.id=b.artist_id" +
                        " ORDER BY media DESC";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setString(i++, userA);
            preparedStatement.setString(i, userB);


            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();
            List<UserArtistComparison> returnList = new ArrayList<>();

            if (!resultSet.next()) {
                return new ResultWrapper<>(0, returnList);
            }
            resultSet.last();
            int rows = resultSet.getRow();
            resultSet.beforeFirst();
            /* Get results. */
            int j = 0;
            while (resultSet.next() && (j < limit && j < rows)) {
                j++;
                String name = resultSet.getString("c.name");
                int countA = resultSet.getInt("a.playNumber");
                int countB = resultSet.getInt("b.playNumber");
                String url = resultSet.getString("c.url");
                returnList.add(new UserArtistComparison(countA, countB, name, userA, userB, url));
            }

            return new ResultWrapper<>(rows, returnList);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public WrapperReturnNowPlaying knows(Connection con, long artistId, long guildId, int limit) {

        @Language("MariaDB")
        String queryString =
                "SELECT a2.name, a.lastfm_id, a.playNumber, a2.url, c.discord_id " +
                        "FROM  scrobbled_artist a" +
                        " JOIN artist a2 ON a.artist_id = a2.id  " +
                        "JOIN `user` c on c.lastFm_Id = a.lastFM_ID " +
                        "JOIN user_guild d on c.discord_ID = d.discord_Id " +
                        "where d.guild_Id = ? " +
                        "and  a2.id = ? " +
                        "ORDER BY a.playNumber desc ";
        queryString = limit == Integer.MAX_VALUE ? queryString : queryString + "limit " + limit;
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(i++, guildId);
            preparedStatement.setLong(i, artistId);



            /* Execute query. */

            ResultSet resultSet = preparedStatement.executeQuery();
            int rows;
            String url = "";
            String artistName = "";
            List<ReturnNowPlaying> returnList = new ArrayList<>();
            if (!resultSet.next()) {
                rows = 0;
            } else {
                resultSet.last();
                rows = resultSet.getRow();
                url = resultSet.getString("a2.url");
                artistName = resultSet.getString("a2.name");


            }
            /* Get generated identifier. */

            resultSet.beforeFirst();
            /* Get results. */
            int j = 0;
            while (resultSet.next() && (j < limit && j < rows)) {
                j++;
                String lastfmId = resultSet.getString("a.lastFM_ID");

                int playNumber = resultSet.getInt("a.playNumber");
                long discordId = resultSet.getLong("c.discord_ID");

                returnList.add(new ReturnNowPlaying(discordId, lastfmId, artistName, playNumber));
            }
            /* Return booking. */
            return new WrapperReturnNowPlaying(returnList, rows, url, artistName);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public UniqueWrapper<ArtistPlays> getCrowns(Connection connection, String lastfmId, long guildID,
                                                int crownThreshold) {
        List<ArtistPlays> returnList = new ArrayList<>();
        long discordId;

        @Language("MariaDB") String queryString = "SELECT a2.name, b.discord_id , playnumber AS orden" +
                " FROM  scrobbled_artist  a" +
                " JOIN user b ON a.lastfm_id = b.lastfm_id " +
                " JOIN artist a2 ON a.artist_id = a2.id " +
                " WHERE  b.lastfm_id = ?" +
                " AND playnumber >= ? " +
                " AND  playnumber >= ALL" +
                "       (SELECT max(b.playnumber) " +
                " FROM " +
                "(SELECT in_a.artist_id,in_a.playnumber" +
                " FROM scrobbled_artist in_a  " +
                " JOIN " +
                " user in_b" +
                " ON in_a.lastfm_id = in_b.lastfm_id" +
                " NATURAL JOIN " +
                " user_guild in_c" +
                " WHERE guild_id = ?" +
                "   ) AS b" +
                " WHERE b.artist_id = a.artist_id" +
                " GROUP BY artist_id)" +
                " ORDER BY orden DESC";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setString(i++, lastfmId);
            preparedStatement.setInt(i++, crownThreshold);
            preparedStatement.setLong(i, guildID);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                return new UniqueWrapper<>(0, 0, lastfmId, returnList);

            } else {
                discordId = resultSet.getLong("b.discord_id");
                resultSet.beforeFirst();
            }

            while (resultSet.next()) {

                String artist = resultSet.getString("a2.name");
                int plays = resultSet.getInt("orden");
                returnList.add(new ArtistPlays(artist, plays));
            }
        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return new UniqueWrapper<>(returnList.size(), discordId, lastfmId, returnList);
    }

    @Override
    public ResultWrapper<ScrobbledArtist> getGuildTop(Connection connection, Long guildID, int limit,
                                                      boolean doCount) {
        @Language("MariaDB") String normalQUery = "SELECT d.name, sum(playnumber) AS orden ,url  ";

        String countQuery = "Select count(*) as orden ";


        String queryBody = "FROM  scrobbled_artist a" +
                " JOIN user b" +
                " ON a.lastfm_id = b.lastfm_id" +
                " JOIN artist d " +
                " ON a.artist_id = d.id";
        if (guildID != null) {
            queryBody += " JOIN  user_guild c" +
                    " ON b.discord_id=c.discord_id" +
                    " WHERE c.guild_id = ?";
        }

        List<ScrobbledArtist> list = new ArrayList<>();
        int count = 0;
        int i = 1;
        try (PreparedStatement preparedStatement1 = connection.prepareStatement(normalQUery + queryBody + " GROUP BY artist_id,url  ORDER BY orden DESC  limit ?")) {
            if (guildID != null)
                preparedStatement1.setLong(i++, guildID);

            preparedStatement1.setInt(i, limit);

            ResultSet resultSet1 = preparedStatement1.executeQuery();

            while (resultSet1.next()) {
                String artist = resultSet1.getString("d.name");
                String url = resultSet1.getString("url");

                int plays = resultSet1.getInt("orden");
                ScrobbledArtist who = new ScrobbledArtist(artist, plays, url);
                list.add(who);
            }
            if (doCount) {

                PreparedStatement preparedStatement = connection.prepareStatement(countQuery + queryBody);
                i = 1;
                if (guildID != null)
                    preparedStatement.setLong(i, guildID);

                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    count = resultSet.getInt(1);
                }


            }
        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        return new ResultWrapper<>(count, list);
    }

    @Override
    public int userPlays(Connection con, long artistId, String whom) {
        @Language("MariaDB") String queryString = "SELECT a.playnumber " +
                "FROM scrobbled_artist a JOIN user b ON a.lastfm_id=b.lastfm_id " +
                "JOIN artist c ON a.artist_id = c.id " +
                "WHERE a.lastfm_id = ? AND c.id = ?";
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {
            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setString(i++, whom);
            preparedStatement.setLong(i, artistId);




            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next())
                return 0;
            return resultSet.getInt("a.playNumber");


        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<LbEntry> crownsLeaderboard(Connection connection, long guildID, int threshold) {
        @Language("MariaDB") String queryString = "SELECT t2.lastfm_id,t3.discord_id,count(t2.lastfm_id) ord " +
                "FROM " +
                "( " +
                "SELECT " +
                "        a.artist_id,max(a.playnumber) plays " +
                "    FROM " +
                "         scrobbled_artist a  " +
                "    JOIN " +
                "        user b  " +
                "            ON a.lastfm_id = b.lastfm_id  " +
                "    JOIN " +
                "        user_guild c  " +
                "            ON b.discord_id = c.discord_id  " +
                "    WHERE " +
                "        c.guild_id = ?  " +
                "    GROUP BY " +
                "        a.artist_id  " +
                "  ) t " +
                "  JOIN scrobbled_artist t2  " +
                "   " +
                "  ON t.plays = t2.playnumber AND t.artist_id = t2.artist_id " +
                "  JOIN user t3  ON t2.lastfm_id = t3.lastfm_id  " +
                "    JOIN " +
                "        user_guild t4  " +
                "            ON t3.discord_id = t4.discord_id  " +
                "    WHERE " +
                "        t4.guild_id = ?  ";
        if (threshold != 0) {
            queryString += " and t2.playnumber > ? ";
        }


        queryString += "  GROUP BY t2.lastfm_id,t3.discord_id " +
                "  ORDER BY ord DESC";

        return getLbEntries(connection, guildID, queryString, CrownsLbEntry::new, true, threshold);


    }

    @Override
    public List<LbEntry> uniqueLeaderboard(Connection connection, long guildId) {
        @Language("MariaDB") String queryString = "SELECT  " +
                "    count(temp.lastfm_id) AS ord,temp.lastfm_id,temp.discord_id " +
                "FROM " +
                "    (SELECT  " +
                "         a.lastfm_id, b.discord_id " +
                "    FROM " +
                "        scrobbled_artist a " +
                "    JOIN user b ON a.lastfm_id = b.lastfm_id " +
                "    JOIN user_guild c ON b.discord_id = c.discord_id " +
                "    WHERE " +
                "        c.guild_id = ? " +
                "            AND a.playnumber > 2 " +
                "    GROUP BY a.artist_id " +
                "    HAVING COUNT(*) = 1) temp " +
                "GROUP BY lastfm_id " +
                "ORDER BY ord DESC";

        return getLbEntries(connection, guildId, queryString, UniqueLbEntry::new, false, 0);
    }


    @Override
    public int userArtistCount(Connection con, String whom) {
        @Language("MariaDB") String queryString = "SELECT count(*) AS numb FROM scrobbled_artist WHERE scrobbled_artist.lastfm_id=?";
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {
            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setString(i, whom);

            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next())
                return 0;
            return resultSet.getInt("numb");


        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<LbEntry> artistLeaderboard(Connection con, long guildID) {
        @Language("MariaDB") String queryString = "(SELECT  " +
                "        a.lastfm_id , count(*) AS ord, c.discord_id" +
                "    FROM " +
                "        scrobbled_artist a " +
                "    JOIN user b ON a.lastfm_id = b.lastfm_id " +
                "    JOIN user_guild c ON b.discord_id = c.discord_id " +
                "    WHERE " +
                "        c.guild_id = ? " +
                " GROUP BY a.lastfm_id,c.discord_id " +
                "    ORDER BY ord DESC    )";

        return getLbEntries(con, guildID, queryString, ArtistLbEntry::new, false, 0);
    }

    @Override
    public List<LbEntry> obscurityLeaderboard(Connection connection, long guildId) {
        @Language("MariaDB") String queryString = "\n" +
                "SELECT finalmain.lastfm_id,  POW(((mytotalplays / (other_plays_on_my_artists)) * (as_unique_coefficient + 1)),\n" +
                "            0.4) AS ord , c.discord_id\n" +
                "FROM (\n" +
                "SELECT \n" +
                "    main.lastfm_id,\n" +                //OBtains total plays, and other users plays on your artist
                "    (SELECT \n" +
                "              COALESCE(SUM(a.playnumber) * (COUNT(*)), 0)\n" +
                "        FROM\n" +
                "            scrobbled_artist a\n" +
                "        WHERE\n" +
                "            lastfm_id = main.lastfm_id) AS mytotalplays,\n" +
                "    (SELECT \n" +
                "             COALESCE(SUM(a.playnumber), 1)\n" +
                "        FROM\n" +
                "            scrobbled_artist a\n" +
                "        WHERE\n" +
                "            lastfm_id != main.lastfm_id\n" +
                "                AND a.artist_id IN (SELECT \n" +
                "                    artist_id\n" +
                "                FROM\n" +
                "                    artist\n" +
                "                WHERE\n" +
                "                    lastfm_id = main.lastfm_id))AS  other_plays_on_my_artists,\n" +
                "  " +
                "  (SELECT \n" +                // Obtains uniques, percentage of uniques, and plays on uniques
                "            COUNT(*) / (SELECT \n" +
                "                        COUNT(*) + 1\n" +
                "                    FROM\n" +
                "                        scrobbled_artist a\n" +
                "                    WHERE\n" +
                "                        lastfm_id = main.lastfm_id) * (COALESCE(SUM(playnumber), 1))\n" +
                "        FROM\n" +
                "            (SELECT \n" +
                "                artist_id, playnumber, a.lastfm_id\n" +
                "            FROM\n" +
                "                scrobbled_artist a\n" +
                "            GROUP BY a.artist_id\n" +
                "            HAVING COUNT(*) = 1) temp \n" +
                "        WHERE\n" +
                "            temp.lastfm_id = main.lastfm_id\n" +
                "                AND temp.playnumber > 1\n" +
                "        ) as_unique_coefficient\n" +
                "FROM\n" +
                //"\t#full artist table, we will filter later because is somehow faster :D\n" +
                "    scrobbled_artist main\n" +
                "    \n" +
                "GROUP BY main.lastfm_id\n" +
                ") finalmain" +
                " JOIN user b\n" +
                "ON finalmain.lastfm_id = b.lastfm_id \n" +
                "JOIN user_guild c ON b.discord_id = c.discord_id \n" +
                "WHERE c.guild_id = ?" +
                " ORDER BY ord DESC";

        return getLbEntries(connection, guildId, queryString, ObscurityEntry::new, false, 0);
    }

    @Override
    public PresenceInfo getRandomArtistWithUrl(Connection connection) {

        @Language("MariaDB") String queryString =
                "SELECT \n" +
                        "    b.name,\n" +
                        "    b.url,\n " +
                        "    discord_id,\n" +
                        "    (SELECT \n" +
                        "            SUM(playnumber)\n" +
                        "        FROM\n" +
                        "            scrobbled_artist\n" +
                        "        WHERE\n" +
                        "            artist_id = a.artist_id) AS summa\n" +
                        "FROM\n" +
                        "    scrobbled_artist a\n" +
                        "        JOIN\n" +
                        "    artist b ON a.artist_id = b.id\n" +
                        "        NATURAL JOIN\n" +
                        "    user c\n" +
                        "WHERE\n" +
                        "    b.id IN (SELECT \n" +
                        "            rando.id\n" +
                        "        FROM\n" +
                        "            (SELECT \n" +
                        "                a.id\n" +
                        "            FROM\n" +
                        "                artist a\n" +
                        "                WHERE a.url IS NOT NULL\n" +
                        "                AND a.url != ''\n" +
                        "            ORDER BY RAND()\n" +
                        "            LIMIT 1) rando)\n" +
                        "ORDER BY RAND()\n" +
                        "LIMIT 1;";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next())
                return null;

            String artistName = resultSet.getString("name");
            String url = resultSet.getString("url");

            long summa = resultSet.getLong("summa");
            long discordId = resultSet.getLong("discord_id");
            return new PresenceInfo(artistName, url, summa, discordId);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public StolenCrownWrapper getCrownsStolenBy(Connection connection, String ogUser, String queriedUser,
                                                long guildId, int threshold) {
        List<StolenCrown> returnList = new ArrayList<>();
        long discordid;
        long discordid2;
        @Language("MariaDB") String queryString = "SELECT \n" +
                "    inn.name AS artist ,inn.orden AS ogplays , inn.discord_id AS ogid , inn2.discord_id queriedid,  inn2.orden AS queriedplays\n" +
                "FROM\n" +
                "    (SELECT \n" +
                "        a.artist_id, a2.name, b.discord_id, playnumber AS orden\n" +
                "    FROM\n" +
                "        scrobbled_artist a\n" +
                "    JOIN user b ON a.lastfm_id = b.lastfm_id\n" +
                " JOIN artist a2 ON a.artist_id = a2.id " +
                "    WHERE\n" +
                "        a.lastfm_id = ?) inn\n" +
                "        JOIN\n" +
                "    (SELECT \n" +
                "        artist_id, b.discord_id, playnumber AS orden\n" +
                "    FROM\n" +
                "        scrobbled_artist a\n" +
                "    JOIN user b ON a.lastfm_id = b.lastfm_id\n" +
                "    WHERE\n" +
                "        b.lastfm_id = ?) inn2 ON inn.artist_id = inn2.artist_id\n" +
                "WHERE\n" +
                "    (inn2.artist_id , inn2.orden) = (SELECT \n" +
                "            in_a.artist_id, MAX(in_a.playnumber)\n" +
                "        FROM\n" +
                "            scrobbled_artist in_a\n" +
                "                JOIN\n" +
                "            user in_b ON in_a.lastfm_id = in_b.lastfm_id\n" +
                "                NATURAL JOIN\n" +
                "            user_guild in_c\n" +
                "        WHERE\n" +
                "            guild_id = ?\n" +
                "                AND artist_id = inn2.artist_id)\n" +
                "        AND (inn.artist_id , inn.orden) = (SELECT \n" +
                "            in_a.artist_id, in_a.playnumber\n" +
                "        FROM\n" +
                "            scrobbled_artist in_a\n" +
                "                JOIN\n" +
                "            user in_b ON in_a.lastfm_id = in_b.lastfm_id\n" +
                "                NATURAL JOIN\n" +
                "            user_guild in_c\n" +
                "        WHERE\n" +
                "            guild_id = ?\n" +
                "                AND artist_id = inn.artist_id\n" +
                "        ORDER BY in_a.playnumber DESC\n" +
                "        LIMIT 1 , 1)\n";

        if (threshold != 0) {
            queryString += " and inn.orden >= ? && inn2.orden >= ? ";
        }


        queryString += " ORDER BY inn.orden DESC , inn2.orden DESC\n";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setString(i++, ogUser);
            preparedStatement.setString(i++, queriedUser);

            preparedStatement.setLong(i++, guildId);
            preparedStatement.setLong(i++, guildId);
            if (threshold != 0) {
                preparedStatement.setInt(i++, threshold);
                preparedStatement.setInt(i, threshold);
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                return new StolenCrownWrapper(0, 0, returnList);
            } else {
                discordid = resultSet.getLong("ogId");
                discordid2 = resultSet.getLong("queriedId");
                resultSet.beforeFirst();
            }

            while (resultSet.next()) {

                String artist = resultSet.getString("artist");
                int plays = resultSet.getInt("ogPlays");
                int plays2 = resultSet.getInt("queriedPlays");

                returnList.add(new StolenCrown(artist, plays, plays2));
            }
        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
        //Ids will be 0 if returnlist is empty;
        return new StolenCrownWrapper(discordid, discordid2, returnList);
    }

    @Override
    public UniqueWrapper<ArtistPlays> getUserAlbumCrowns(Connection connection, String lastfmId,
                                                         long guildId) {

        @Language("MariaDB") String queryString = "SELECT a2.name ,a.album,a.plays,b.discord_id " +
                "FROM album_crowns a " +
                "JOIN user b ON a.discordid = b.discord_id" +
                " JOIN artist a2 ON a.artist_id = a2.id " +
                " WHERE guildid = ? AND b.lastfm_id = ? ORDER BY plays DESC";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setLong(i++, guildId);
            preparedStatement.setString(i, lastfmId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                return new UniqueWrapper<>(0, 0, lastfmId, new ArrayList<>());
            }

            List<ArtistPlays> returnList = new ArrayList<>();
            resultSet.last();
            int rows = resultSet.getRow();

            long discordId = resultSet.getLong("discord_id");

            resultSet.beforeFirst();
            /* Get results. */

            while (resultSet.next()) { //&& (j < 10 && j < rows)) {
                String name = resultSet.getString("name");
                String album = resultSet.getString("album");

                int countA = resultSet.getInt("plays");

                returnList.add(new ArtistPlays(name + " - " + album, countA));

            }
            return new UniqueWrapper<>(rows, discordId, lastfmId, returnList);


        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
        }
        return null;
    }


    @Override
    public List<LbEntry> albumCrownsLeaderboard(Connection con, long guildID) {
        @Language("MariaDB") String queryString = "SELECT \n" +
                "    b.discord_id , b.lastfm_id, COUNT(*) AS ord\n" +
                "FROM\n" +
                "    album_crowns a\n" +
                "      RIGHT JOIN\n" +
                "    user b ON a.discordid = b.discord_id\n" +
                "WHERE\n" +
                "    guildid = ?\n" +
                "GROUP BY a.discordid , b.lastfm_id\n" +
                "ORDER BY ord DESC ;";

        return getLbEntries(con, guildID, queryString, AlbumCrownLbEntry::new, false, 0);
    }

    @Override
    public ObscuritySummary getUserObscuritPoints(Connection connection, String lastfmId) {
        @Language("MariaDB") String queryString = "\tSELECT  b, other_plays_on_my_artists, unique_coefficient,\n" +
                "\tPOW(((b/ (other_plays_on_my_artists)) * (unique_coefficient + 1)),0.4) AS total\n" +
                "\t\tFROM (\n" +
                "\n" +
                "\tSELECT (SELECT sum(a.playnumber) * count(*) FROM \n" +
                "\tscrobbled_artist a \n" +
                "\tWHERE lastfm_id = main.lastfm_id) AS b ,  \n" +
                "\t\t   (SELECT COALESCE(Sum(a.playnumber), 1) \n" +
                "\t\t\tFROM   scrobbled_artist a \n" +
                " WHERE  lastfm_id != main.lastfm_id \n" +
                "   AND a.artist_id IN (SELECT artist_id \n" +
                "   FROM   artist \n" +
                "   WHERE  lastfm_id = main.lastfm_id)) AS \n" +
                "   other_plays_on_my_artists, \n" +
                "   (SELECT Count(*) / (SELECT Count(*) + 1 \n" +
                "   FROM   scrobbled_artist a \n" +
                "\t\t\t\t\t\t\t   WHERE  lastfm_id = main.lastfm_id) * ( \n" +
                "\t\t\t\t   COALESCE(Sum(playnumber \n" +
                "\t\t\t\t\t\t\t), 1) ) \n" +
                "\t\t\tFROM   (SELECT artist_id, \n" +
                "\t\t\t\t\t\t   playnumber, \n" +
                "\t\t\t\t\t\t   a.lastfm_id \n" +
                "\t\t\t\t\tFROM   scrobbled_artist a \n" +
                "\t\t\t\t\tGROUP  BY a.artist_id \n" +
                "\t\t\t\t\tHAVING Count(*) = 1) temp \n" +
                "\t\t\tWHERE  temp.lastfm_id = main.lastfm_id \n" +
                "\t\t\t\t   AND temp.playnumber > 1) \n" +
                "\t\t   AS unique_coefficient                      \n" +
                "\tFROM   scrobbled_artist main \n" +
                "\tWHERE  lastfm_id =  ?" +
                " GROUP BY lastfm_id\n" +
                "\t\n" +
                "\t) outer_main\n";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setString(i, lastfmId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                return null;

            }

            int totalPlays = resultSet.getInt("b");
            int otherPlaysOnMyArtists = resultSet.getInt("other_plays_on_my_artists");
            int uniqueCoefficient = resultSet.getInt("unique_coefficient");
            int total = resultSet.getInt("total");

            return new ObscuritySummary(totalPlays, otherPlaysOnMyArtists, uniqueCoefficient, total);


        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public int getRandomCount(Connection connection, Long userId) {
        @Language("MariaDB") String queryString = "SELECT \n" +
                "  count(*) as counted " +
                "FROM\n" +
                "    randomlinks \n";
        if (userId != null) {
            queryString += "WHERE discord_id = ?";

        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            if (userId != null) {
                preparedStatement.setLong(1, userId);
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                return 0;
            }

            return resultSet.getInt("counted");

        } catch (SQLException e) {
            Chuu.getLogger().error(e.getMessage(), e);
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<GlobalCrown> getGlobalKnows(Connection connection, long artistID) {
        List<GlobalCrown> returnedList = new ArrayList<>();
        @Language("MariaDB") String queryString = "SELECT  playnumber AS ord, discord_id, l.lastfm_id\n" +
                " FROM  scrobbled_artist ar\n" +
                "  	 	 JOIN user l ON ar.lastfm_id = l.lastfm_id " +
                "        WHERE  ar.artist_id = ? " +
                "        ORDER BY  playnumber DESC";


        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setLong(i, artistID);


            ResultSet resultSet = preparedStatement.executeQuery();
            int j = 1;
            while (resultSet.next()) {

                String lastfmId = resultSet.getString("lastfm_id");
                long discordId = resultSet.getLong("discord_id");
                int crowns = resultSet.getInt("ord");

                returnedList.add(new GlobalCrown(lastfmId, discordId, crowns, j++));
            }
            return returnedList;
        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException((e));
        }
    }

    //TriFunction is not the simplest approach but i felt like using it so :D
    @NotNull
    private List<LbEntry> getLbEntries(Connection connection, long guildId, String
            queryString, TriFunction<String, Long, Integer, LbEntry> fun, boolean needsReSet, int resetThreshold) {
        List<LbEntry> returnedList = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setLong(i, guildId);
            if (needsReSet) {
                preparedStatement.setLong(++i, guildId);
                if (resetThreshold != 0)
                    preparedStatement.setLong(++i, resetThreshold);
            }
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) { //&& (j < 10 && j < rows)) {
                String lastfmId = resultSet.getString("lastfm_id");
                long discordId = resultSet.getLong("discord_id");
                int crowns = resultSet.getInt("ord");

                returnedList.add(fun.apply(lastfmId, discordId, crowns));


            }
            return returnedList;
        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new ChuuServiceException((e));
        }
    }
}



