package dao;

import core.Chuu;
import dao.entities.*;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SQLQueriesDaoImpl implements SQLQueriesDao {


    @Override
    public void getGlobalRank(Connection connection, String lastfmid) {

    }

    @Override
    public UniqueWrapper<UniqueData> getGlobalCrowns(Connection connection, String lastFmId) {
        List<UniqueData> returnList = new ArrayList<>();
        long discordID;

        @Language("MariaDB") String queryString = "SELECT artist_id, b.discordID , playNumber as orden" +
                " FROM  artist  a" +
                " join lastfm b on a.lastFMID = b.lastFmId" +
                " where  a.lastFMID = ?" +
                " and playNumber > 0" +
                " AND  playNumber >= all" +
                "       (Select max(b.playNumber) " +
                " from " +
                "(Select in_A.artist_id,in_A.playNumber" +
                " from artist in_A  " +
                " join " +
                " lastfm in_B" +
                " on in_A.lastFMID = in_B.lastFmid" +
                " natural join " +
                " user_guild in_C" +
                "   ) as b" +
                " where b.artist_id = a.artist_id" +
                " group by artist_id)" +
                " order by orden DESC";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setString(i++, lastFmId);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                return new UniqueWrapper<>(0, 0, lastFmId, returnList);

            } else {
                discordID = resultSet.getLong("b.discordID");
                resultSet.beforeFirst();
            }

            while (resultSet.next()) {

                String artist = resultSet.getString("artist_id");
                int plays = resultSet.getInt("orden");
                returnList.add(new UniqueData(artist, plays));
            }
        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return new UniqueWrapper<>(returnList.size(), discordID, lastFmId, returnList);

    }

    @Override
    public UniqueWrapper<UniqueData> getGlobalUniques(Connection connection, String lastfmId) {

        @Language("MySQL") String queryString = "SELECT * " +
                "FROM(  " +
                "       select artist_id, playNumber, a.lastFMID ,b.discordID" +
                "       from artist a join lastfm b " +
                "       ON a.lastFMID = b.lastFmId " +
                "       JOIN user_guild c ON b.discordID = c.discordId " +
                "       where  a.playNumber > 2 " +
                "       group by a.artist_id " +
                "       having count( *) = 1) temp " +
                "Where temp.lastFMID = ? and temp.playNumber > 1 " +
                " order by temp.playNumber desc ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setString(i, lastfmId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                return new UniqueWrapper<>(0, 0, lastfmId, new ArrayList<>());
            }

            List<UniqueData> returnList = new ArrayList<>();
            resultSet.last();
            int rows = resultSet.getRow();
            long discordId = resultSet.getLong("temp.discordID");

            resultSet.beforeFirst();
            /* Get results. */

            while (resultSet.next()) { //&& (j < 10 && j < rows)) {
                String name = resultSet.getString("temp.artist_id");
                int count_a = resultSet.getInt("temp.playNumber");

                returnList.add(new UniqueData(name, count_a));

            }
            return new UniqueWrapper<>(rows, discordId, lastfmId, returnList);


        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public UniqueWrapper<UniqueData> getUniqueArtist(Connection connection, Long guildID, String lastFmId) {
        @Language("MySQL") String queryString = "SELECT * " +
                "FROM(  " +
                "       select artist_id, playNumber, a.lastFMID ,b.discordID" +
                "       from artist a join lastfm b " +
                "       ON a.lastFMID = b.lastFmId " +
                "       JOIN user_guild c ON b.discordID = c.discordId " +
                "       where c.guildId = ? and a.playNumber > 2 " +
                "       group by a.artist_id " +
                "       having count( *) = 1) temp " +
                "Where temp.lastFMID = ? and temp.playNumber > 1 " +
                " order by temp.playNumber desc ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setLong(i++, guildID);
            preparedStatement.setString(i, lastFmId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                return new UniqueWrapper<>(0, 0, lastFmId, new ArrayList<>());
            }

            List<UniqueData> returnList = new ArrayList<>();
            resultSet.last();
            int rows = resultSet.getRow();
            long discordId = resultSet.getLong("temp.discordID");

            resultSet.beforeFirst();
            /* Get results. */

            while (resultSet.next()) { //&& (j < 10 && j < rows)) {
                String name = resultSet.getString("temp.artist_id");
                int count_a = resultSet.getInt("temp.playNumber");

                returnList.add(new UniqueData(name, count_a));

            }
            return new UniqueWrapper<>(rows, discordId, lastFmId, returnList);


        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public ResultWrapper similar(Connection connection, List<String> lastfMNames) {
        int MAX_IN_DISPLAY = 10;
        String userA = lastfMNames.get(0);
        String userB = lastfMNames.get(1);

        @Language("MySQL") String queryString = "SELECT a.artist_id ,a.lastFMID , a.playNumber, b.lastFMID,b.playNumber ,((a.playNumber + b.playNumber)/(abs(a.playNumber-b.playNumber)+1)* ((a.playNumber + b.playNumber))*2.5) media , c.url " +
                "FROM " +
                "(SELECT * " +
                "FROM artist " +
                "WHERE lastFMID = ? ) a " +
                "JOIN " +
                "(SELECT * " +
                "FRom artist " +
                "where  lastFMID = ? ) b " +
                "ON a.artist_id=b.artist_id " +
                "JOIN artist_url c " +
                "on c.artist_id=b.artist_id" +
                " order by media desc ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setString(i++, userA);
            preparedStatement.setString(i, userB);


            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();
            List<Results> returnList = new ArrayList<>();

            if (!resultSet.next()) {
                return new ResultWrapper(0, returnList);
            }
            resultSet.last();
            int rows = resultSet.getRow();
            resultSet.beforeFirst();
            /* Get results. */
            int j = 0;
            while (resultSet.next() && (j < MAX_IN_DISPLAY && j < rows)) {
                j++;
                String name = resultSet.getString("a.artist_id");
                int count_a = resultSet.getInt("a.playNumber");
                int count_b = resultSet.getInt("b.playNumber");
                String url = resultSet.getString("c.url");
                returnList.add(new Results(count_a, count_b, name, userA, userB, url));

            }

            return new ResultWrapper(rows, returnList);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public WrapperReturnNowPlaying knows(Connection con, String artist, long guildId, int limit) {

        String queryString = "Select temp.artist_id, temp.lastFMID,temp.playNumber,b.url, c.discordID " +
                "FROM (SELECT a.artist_id, a.lastFMID, a.playNumber " +
                "FROM  artist a  " +
                "where artist_id = ?" +
                "group by a.artist_id,a.lastFMID,a.playNumber " +
                "order by playNumber desc) temp " +
                "JOIN artist_url b ON temp.artist_id=b.artist_id " +
                "JOIN lastfm c on c.lastFmId = temp.lastFMID " +
                "JOIN user_guild d on c.discordID = d.discordId " +
                "where d.guildId = ? " +
                "ORDER BY temp.playNumber desc ";
        queryString = limit == Integer.MAX_VALUE ? queryString : queryString + "limit " + limit;
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setString(i++, artist);
            preparedStatement.setLong(i, guildId);


            /* Execute query. */

            ResultSet resultSet = preparedStatement.executeQuery();
            int rows;
            String url = "";
            List<ReturnNowPlaying> returnList = new ArrayList<>();
            if (!resultSet.next()) {
                rows = 0;
            } else {
                resultSet.last();
                rows = resultSet.getRow();
                url = resultSet.getString("b.url");

            }
            /* Get generated identifier. */

            resultSet.beforeFirst();
            /* Get results. */
            int j = 0;
            while (resultSet.next() && (j < limit && j < rows)) {
                j++;
                String lastFMId = resultSet.getString("temp.lastFMID");
                int playNumber = resultSet.getInt("temp.playNumber");
                long discordId = resultSet.getLong("c.discordID");

                returnList.add(new ReturnNowPlaying(discordId, lastFMId, artist, playNumber));
            }
            /* Return booking. */
            return new WrapperReturnNowPlaying(returnList, rows, url, artist);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UniqueWrapper<UniqueData> getCrowns(Connection connection, String lastFmId, long guildID) {
        List<UniqueData> returnList = new ArrayList<>();
        long discordID;

        @Language("MariaDB") String queryString = "SELECT artist_id, b.discordID , playNumber as orden" +
                " FROM  artist  a" +
                " join lastfm b on a.lastFMID = b.lastFmId" +
                " where  a.lastFMID = ?" +
                " and playNumber > 0" +
                " AND  playNumber >= all" +
                "       (Select max(b.playNumber) " +
                " from " +
                "(Select in_A.artist_id,in_A.playNumber" +
                " from artist in_A  " +
                " join " +
                " lastfm in_B" +
                " on in_A.lastFMID = in_B.lastFmid" +
                " natural join " +
                " user_guild in_C" +
                " where guildId = ?" +
                "   ) as b" +
                " where b.artist_id = a.artist_id" +
                " group by artist_id)" +
                " order by orden DESC";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setString(i++, lastFmId);
            preparedStatement.setLong(i, guildID);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                return new UniqueWrapper<>(0, 0, lastFmId, returnList);

            } else {
                discordID = resultSet.getLong("b.discordID");
                resultSet.beforeFirst();
            }

            while (resultSet.next()) {

                String artist = resultSet.getString("artist_id");
                int plays = resultSet.getInt("orden");
                returnList.add(new UniqueData(artist, plays));
            }
        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return new UniqueWrapper<>(returnList.size(), discordID, lastFmId, returnList);
    }

    @Override
    public List<UrlCapsule> getGuildTop(Connection connection, Long guildID) {
        @Language("MariaDB") String queryString = "SELECT a.artist_id, sum(playNumber) as orden ,url  FROM  artist a" +
                " JOIN lastfm b" +
                " ON a.lastFMID = b.lastFmId" +
                " JOIN artist_url d " +
                " ON a.artist_id = d.artist_id" +
                " JOIN  user_guild c" +
                " On b.discordID=c.discordId" +
                " Where c.guildId = ?" +
                " group by artist_id,url" +
                " order BY orden DESC" +
                " LIMIT 25;";
        List<UrlCapsule> list = new LinkedList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setLong(1, guildID);

            ResultSet resultSet = preparedStatement.executeQuery();
            int count = 0;
            while (resultSet.next()) {
                String artist = resultSet.getString("a.artist_id");
                String url = resultSet.getString("url");

                int plays = resultSet.getInt("orden");

                UrlCapsule capsule = new UrlCapsule(url, count++, artist, "", "");
                capsule.setPlays(plays);
                list.add(capsule);
            }
        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
        }
        return list;
    }

    @Override
    public int userPlays(Connection con, String artist, String whom) {
        @Language("MariaDB") String queryString = "Select a.playNumber " +
                "FROM artist a JOIN lastfm b on a.lastFMID=b.lastFmId " +
                "JOIN artist_url c on a.artist_id = c.artist_id " +
                "where a.lastFMID = ? and a.artist_id =?";
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {
            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setString(i++, whom);
            preparedStatement.setString(i, artist);




            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next())
                return 0;
            return resultSet.getInt("a.playNumber");


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<LbEntry> crownsLeaderboard(Connection connection, long guildID) {
        @Language("MariaDB") String queryString = "SELECT t2.lastFMID,t3.discordID,count(t2.lastFMID) ord " +
                "From " +
                "( " +
                "Select " +
                "        a.artist_id,max(a.playNumber) plays " +
                "    FROM " +
                "         artist a  " +
                "    JOIN " +
                "        lastfm b  " +
                "            ON a.lastFMID = b.lastFmId  " +
                "    JOIN " +
                "        user_guild c  " +
                "            ON b.discordID = c.discordId  " +
                "    WHERE " +
                "        c.guildId = ?  " +
                "    GROUP BY " +
                "        a.artist_id  " +
                "  ) t " +
                "  JOIN artist t2  " +
                "   " +
                "  on t.plays = t2.playNumber and t.artist_id = t2.artist_id " +
                "  JOIN lastfm t3  ON t2.lastFMID = t3.lastFmId  " +
                "    JOIN " +
                "        user_guild t4  " +
                "            ON t3.discordID = t4.discordId  " +
                "    WHERE " +
                "        t4.guildId = ?  " +
                "  group by t2.lastFMID,t3.discordID " +
                "  order by ord desc";

        return getLbEntries(connection, guildID, queryString, CrownsLbEntry::new, true);


    }

    @Override
    public List<LbEntry> uniqueLeaderboard(Connection connection, long guildId) {
        @Language("MariaDB") String queryString = "SELECT  " +
                "    count(temp.lastfmID) as ord,temp.lastFMID,temp.discordID " +
                "FROM " +
                "    (SELECT  " +
                "         a.lastFMID, b.discordID " +
                "    FROM " +
                "        artist a " +
                "    JOIN lastfm b ON a.lastFMID = b.lastFmId " +
                "    JOIN user_guild c ON b.discordID = c.discordId " +
                "    WHERE " +
                "        c.guildId = ? " +
                "            AND a.playNumber > 2 " +
                "    GROUP BY a.artist_id " +
                "    HAVING COUNT(*) = 1) temp " +
                "group by lastFMID " +
                "ORDER BY ord DESC";

        return getLbEntries(connection, guildId, queryString, UniqueLbEntry::new, false);
    }


    @Override
    public int userArtistCount(Connection con, String whom) {
        String queryString = "Select count(*) as numb from artist where artist.lastFMID=?";
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
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<LbEntry> artistLeaderboard(Connection con, long guildID) {
        @Language("MariaDB") String queryString = "(SELECT  " +
                "        a.lastfmID , count(*) as ord, c.discordId" +
                "    FROM " +
                "        artist a " +
                "    JOIN lastfm b ON a.lastFMID = b.lastFmId " +
                "    JOIN user_guild c ON b.discordID = c.discordId " +
                "    WHERE " +
                "        c.guildId = ? " +
                " group by a.lastFMID,c.discordId " +
                "    order by ord desc    )";

        return getLbEntries(con, guildID, queryString, ArtistLbEntry::new, false);
    }

    @Override
    public List<LbEntry> obscurityLeaderboard(Connection connection, long guildId) {
        @Language("MariaDB") String queryString = "\n" +
                "Select finalMain.lastfmid,  POW(((mytotalPlays / (other_plays_on_my_artists)) * (as_unique_coefficient + 1)),\n" +
                "            0.4) as ord , c.discordId\n" +
                "from (\n" +
                "SELECT \n" +
                "    main.lastFMID,\n" +                //OBtains total plays, and other users plays on your artist
                "    (SELECT \n" +
                "              COALESCE(SUM(a.playNumber) * (COUNT(*)), 0)\n" +
                "        FROM\n" +
                "            artist a\n" +
                "        WHERE\n" +
                "            lastfmid = main.lastfmid) AS mytotalPlays,\n" +
                "    (SELECT \n" +
                "             COALESCE(SUM(a.playNumber), 1)\n" +
                "        FROM\n" +
                "            artist a\n" +
                "        WHERE\n" +
                "            lastfmid != main.lastfmid\n" +
                "                AND a.artist_id IN (SELECT \n" +
                "                    artist_id\n" +
                "                FROM\n" +
                "                    artist\n" +
                "                WHERE\n" +
                "                    lastfmid = main.lastfmid))as  other_plays_on_my_artists,\n" +
                "  " +
                "  (SELECT \n" +                // Obtains uniques, percentage of uniques, and plays on uniques
                "            COUNT(*) / (SELECT \n" +
                "                        COUNT(*) + 1\n" +
                "                    FROM\n" +
                "                        artist a\n" +
                "                    WHERE\n" +
                "                        lastfmid = main.lastfmid) * (COALESCE(SUM(playNumber), 1))\n" +
                "        FROM\n" +
                "            (SELECT \n" +
                "                artist_id, playNumber, a.lastFMID\n" +
                "            FROM\n" +
                "                artist a\n" +
                "            GROUP BY a.artist_id\n" +
                "            HAVING COUNT(*) = 1) temp \n" +
                "        WHERE\n" +
                "            temp.lastFMID = main.lastfmID\n" +
                "                AND temp.playNumber > 1\n" +
                "        ) as_unique_coefficient\n" +
                "FROM\n" +
                //"\t#full artist table, we will filter later because is somehow faster :D\n" +
                "    artist main\n" +
                "    \n" +
                "GROUP BY main.lastfmid\n" +
                ") finalMain" +
                " join lastfm b\n" +
                "ON finalMain.lastFMID = b.lastFmId \n" +
                "JOIN user_guild c ON b.discordID = c.discordId \n" +
                "where c.guildId = ?" +
                " order by ord desc";

        return getLbEntries(connection, guildId, queryString, ObscurityEntry::new, false);
    }

    @Override
    public PresenceInfo getRandomArtistWithUrl(Connection connection) {

        @Language("MariaDB") String queryString =
                "SELECT \n" +
                        "    a.artist_id,\n" +
                        "    b.url,\n " +
                        "    discordID,\n" +
                        "    (SELECT \n" +
                        "            SUM(playNumber)\n" +
                        "        FROM\n" +
                        "            artist\n" +
                        "        WHERE\n" +
                        "            artist_id = a.artist_id) as summa\n" +
                        "FROM\n" +
                        "    artist a\n" +
                        "        JOIN\n" +
                        "    artist_url b ON a.artist_id = b.artist_id\n" +
                        "        NATURAL JOIN\n" +
                        "    lastfm c\n" +
                        "WHERE\n" +
                        "    b.artist_id IN (SELECT \n" +
                        "            artist_id\n" +
                        "        FROM\n" +
                        "            (SELECT \n" +
                        "                a.artist_id\n" +
                        "            FROM\n" +
                        "                artist a\n" +
                        "            JOIN artist_url b ON a.artist_id = b.artist_id\n" +
                        "                AND b.url IS NOT NULL\n" +
                        "                AND b.url != ''\n" +
                        "            ORDER BY RAND()\n" +
                        "            LIMIT 1) artist)\n" +
                        "ORDER BY RAND()\n" +
                        "LIMIT 1;";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next())
                return null;

            String artist_id = resultSet.getString("artist_id");
            String url = resultSet.getString("url");

            long summa = resultSet.getLong("summa");
            long discordID = resultSet.getLong("discordID");
            return new PresenceInfo(artist_id, url, summa, discordID);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public StolenCrownWrapper getCrownsStolenBy(Connection connection, String ogUser, String queriedUser, long guildId) {
        List<StolenCrown> returnList = new ArrayList<>();
        long discordID;
        long discordID2 = 0L;
        @Language("MariaDB") String queryString = "SELECT \n" +
                "    inn.artist_id as artist ,inn.orden as ogPlays , inn.discordID as ogId , inn2.discordID queriedId,  inn2.orden as queriedPlays\n" +
                "FROM\n" +
                "    (SELECT \n" +
                "        artist_id, b.discordID, playNumber AS orden\n" +
                "    FROM\n" +
                "        artist a\n" +
                "    JOIN lastfm b ON a.lastFMID = b.lastFmId\n" +
                "    WHERE\n" +
                "        a.lastFMID = ?) inn\n" +
                "        JOIN\n" +
                "    (SELECT \n" +
                "        artist_id, b.discordID, playNumber AS orden\n" +
                "    FROM\n" +
                "        artist a\n" +
                "    JOIN lastfm b ON a.lastFMID = b.lastFmId\n" +
                "    WHERE\n" +
                "        b.lastFMID = ?) inn2 ON inn.artist_id = inn2.artist_id\n" +
                "WHERE\n" +
                "    (inn2.artist_id , inn2.orden) = (SELECT \n" +
                "            in_A.artist_id, MAX(in_A.playnumber)\n" +
                "        FROM\n" +
                "            artist in_A\n" +
                "                JOIN\n" +
                "            lastfm in_B ON in_A.lastFMID = in_B.lastFmid\n" +
                "                NATURAL JOIN\n" +
                "            user_guild in_C\n" +
                "        WHERE\n" +
                "            guildId = ?\n" +
                "                AND artist_id = inn2.artist_id)\n" +
                "        AND (inn.artist_id , inn.orden) = (SELECT \n" +
                "            in_A.artist_id, in_A.playnumber\n" +
                "        FROM\n" +
                "            artist in_A\n" +
                "                JOIN\n" +
                "            lastfm in_B ON in_A.lastFMID = in_B.lastFmid\n" +
                "                NATURAL JOIN\n" +
                "            user_guild in_C\n" +
                "        WHERE\n" +
                "            guildId = ?\n" +
                "                AND artist_id = inn.artist_id\n" +
                "        ORDER BY in_A.playnumber DESC\n" +
                "        LIMIT 1 , 1)\n" +
                "ORDER BY inn.orden DESC , inn2.orden DESC\n" +
                "        \n";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setString(i++, ogUser);
            preparedStatement.setString(i++, queriedUser);

            preparedStatement.setLong(i++, guildId);
            preparedStatement.setLong(i, guildId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                return new StolenCrownWrapper(0, 0, returnList);
            } else {
                discordID = resultSet.getLong("ogId");
                discordID2 = resultSet.getLong("queriedId");
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
            throw new RuntimeException(e);
        }
        //Ids will be 0 if returnlist is empty;
        return new StolenCrownWrapper(discordID, discordID2, returnList);
    }

    @Override
    public UniqueWrapper<UniqueData> getUserAlbumCrowns(Connection connection, String lastfmid, long guildId) {

        @Language("MySQL") String queryString = "Select a.artist_id,a.album,a.plays,b.discordID from album_crowns a join lastfm b on a.discordId = b.discordID where guildId = ? and b.lastFmId = ? order by plays desc";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setLong(i++, guildId);
            preparedStatement.setString(i, lastfmid);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                return new UniqueWrapper<>(0, 0, lastfmid, new ArrayList<>());
            }

            List<UniqueData> returnList = new ArrayList<>();
            resultSet.last();
            int rows = resultSet.getRow();

            long discordId = resultSet.getLong("discordID");

            resultSet.beforeFirst();
            /* Get results. */

            while (resultSet.next()) { //&& (j < 10 && j < rows)) {
                String name = resultSet.getString("artist_id");
                String album = resultSet.getString("album");

                int count_a = resultSet.getInt("plays");

                returnList.add(new UniqueData(name + " - " + album, count_a));

            }
            return new UniqueWrapper<>(rows, discordId, lastfmid, returnList);


        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
        }
        return null;
    }


    @Override
    public List<LbEntry> albumCrownsLeaderboard(Connection con, long guildID) {
        @Language("MariaDB") String queryString = "SELECT \n" +
                "    a.discordId, b.lastfmid, COUNT(*) AS ord\n" +
                "FROM\n" +
                "    album_crowns a\n" +
                "      RIGHT JOIN\n" +
                "    lastfm b ON a.discordId = b.discordId\n" +
                "WHERE\n" +
                "    guildID = ?\n" +
                "GROUP BY a.discordID , b.lastfmid\n" +
                "ORDER BY ord desc ;";

        return getLbEntries(con, guildID, queryString, AlbumCrownLbEntry::new, false);
    }

    @Override
    public ObscuritySummary getUserObscuritPoints(Connection connection, String lastfmid) {
        @Language("MariaDB") String queryString = "\tSelect  b, other_plays_on_my_artists, unique_coefficient,\n" +
                "\tPOW(((b/ (other_plays_on_my_artists)) * (unique_coefficient + 1)),0.4) as total\n" +
                "\t\tfrom (\n" +
                "\n" +
                "\tSELECT (Select sum(a.playnumber) * count(*) from \n" +
                "\tartist a \n" +
                "\twhere lastfmid = main.lastfmid) as b ,  \n" +
                "\t\t   (SELECT COALESCE(Sum(a.playnumber), 1) \n" +
                "\t\t\tFROM   artist a \n" +
                " WHERE  lastfmid != main.lastfmid \n" +
                "   AND a.artist_id IN (SELECT artist_id \n" +
                "   FROM   artist \n" +
                "   WHERE  lastfmid = main.lastfmid)) AS \n" +
                "   other_plays_on_my_artists, \n" +
                "   (SELECT Count(*) / (SELECT Count(*) + 1 \n" +
                "   FROM   artist a \n" +
                "\t\t\t\t\t\t\t   WHERE  lastfmid = main.lastfmid) * ( \n" +
                "\t\t\t\t   COALESCE(Sum(playnumber \n" +
                "\t\t\t\t\t\t\t), 1) ) \n" +
                "\t\t\tFROM   (SELECT artist_id, \n" +
                "\t\t\t\t\t\t   playnumber, \n" +
                "\t\t\t\t\t\t   a.lastfmid \n" +
                "\t\t\t\t\tFROM   artist a \n" +
                "\t\t\t\t\tGROUP  BY a.artist_id \n" +
                "\t\t\t\t\tHAVING Count(*) = 1) temp \n" +
                "\t\t\tWHERE  temp.lastfmid = main.lastfmid \n" +
                "\t\t\t\t   AND temp.playnumber > 1) \n" +
                "\t\t   as unique_coefficient                      \n" +
                "\tFROM   artist main \n" +
                "\tgroup by lastFMID\n" +
                "\thaving lastFMID =  ?\n" +
                "\t) outer_main\n";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setString(i, lastfmid);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                return null;

            }

            int totalPlays = resultSet.getInt("b");
            int other_plays_on_my_artists = resultSet.getInt("other_plays_on_my_artists");
            int unique_coefficient = resultSet.getInt("unique_coefficient");
            int total = resultSet.getInt("total");

            return new ObscuritySummary(totalPlays, other_plays_on_my_artists, unique_coefficient, total);


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
            queryString += "WHERE discordId = ?";

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
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<GlobalCrown> getGlobalKnows(Connection connection, String artistID) {
        List<GlobalCrown> returnedList = new ArrayList<>();
        @Language("MariaDB") String queryString = "Select  @rank := @rank + 1  ranking , playnumber as ord, discordId, l.lastfmID\n" +
                " FROM  artist ar\n" +
                "JOIN  ( SELECT  @rank := 0 ) AS init " +
                "  	 	 JOIN lastfm l on ar.lastfmid = l.lastfmid " +
                "        WHERE  artist_id = ? " +
                "        ORDER BY  playNumber desc";


        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setString(i, artistID);


            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) { //&& (j < 10 && j < rows)) {
                int rank = resultSet.getInt("ranking");

                String lastFMId = resultSet.getString("lastfmID");
                long discordId = resultSet.getLong("discordId");
                int crowns = resultSet.getInt("ord");

                returnedList.add(new GlobalCrown(lastFMId, discordId, crowns, rank));
            }
            return returnedList;
        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new RuntimeException((e));
        }
    }

    //TriFunction is not the simplest approach but i felt like using it so :D
    @NotNull
    private List<LbEntry> getLbEntries(Connection connection, long guildId, String queryString, TriFunction<String, Long, Integer, LbEntry> fun, boolean needs_reSet) {
        List<LbEntry> returnedList = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setLong(i, guildId);
            if (needs_reSet)
                preparedStatement.setLong(++i, guildId);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) { //&& (j < 10 && j < rows)) {
                String lastFMId = resultSet.getString("lastfmID");
                long discordId = resultSet.getLong("discordId");
                int crowns = resultSet.getInt("ord");

                returnedList.add(fun.apply(lastFMId, discordId, crowns));


            }
            return returnedList;
        } catch (SQLException e) {
            Chuu.getLogger().warn(e.getMessage(), e);
            throw new RuntimeException((e));
        }
    }
}



