package dao;

import dao.entities.*;
import dao.exceptions.ChuuServiceException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class BillboardDaoImpl implements BillboardDao {
    @Override
    public Week getCurrentWeekId(Connection connection) {
        String queryString = "" +
                "" +
                "" +
                " select * from (SELECT id,week_start,DATEDIFF(week_start,now()) as ord  from week ) main where ord >= -6 order by ord  limit 1";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            /* Fill "preparedStatement". */

            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next())
                throw new ChuuServiceException();
            int id = resultSet.getInt("id");
            Date week_start = resultSet.getDate("week_start");
            return new Week(id, week_start);


        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public List<BillboardEntity> getBillboard(Connection connection, int week_id, long idLong, boolean doListeners) {
        String table = doListeners ? "weekly_billboard_listeners" : "weekly_billboard_scrobbles";
        String metric = doListeners ? "listeners" : "scrobble_count";
        String streakFunction = doListeners ? "streak_billboard_track" : "streak_billboard_track_scrobbles";
        String queryString =
                "SELECT \n" +
                        "\ta.id,b.name,b.url,week_id,track_name," + metric + ",position,@t := a.id,@t2 := week_id,\n" +
                        " (select min(position) from " + table + " t where t.artist_id = a.artist_id and t.guild_id = a.guild_id and t.track_name = a.track_name and week_id = a.week_id - 1   ) as last_week,\n" +
                        "(select min(position) from " + table + " t where t.artist_id = a.artist_id and t.guild_id = a.guild_id and t.track_name = a.track_name ) as peak,\n" +
                        streakFunction + "(a.id) as streak  \n" +
                        "  from " + table + " a  join artist b on a.artist_id = b.id where guild_id = ? and week_id = ?  order by position asc ," + metric + " desc  \n" +
                        "  \n" +
                        "  \n" +
                        "  \n" +
                        "  ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(1, idLong);
            preparedStatement.setInt(2, week_id);


            /* Execute query. */
            List<BillboardEntity> bills = new ArrayList<>();
            ResultSet resultSet = preparedStatement.executeQuery();
            return getBillboardEntities(metric, bills, resultSet);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }


    }

    private List<BillboardEntity> getBillboardEntities(String metric, List<BillboardEntity> bills, ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            String track_name = resultSet.getString("track_name");
            String artist = resultSet.getString("name");
            int position = resultSet.getInt("position");
            int listeners = resultSet.getInt(metric);

            int peak = resultSet.getInt("peak");
            int streak = resultSet.getInt("streak");
            int previous_week = resultSet.getInt("last_week");
            String url = resultSet.getString("url");
            bills.add(new BillboardEntity(artist, track_name, (long) listeners, peak, streak, previous_week, position, url));
        }

        return bills;
    }

    @Override
    public List<BillboardEntity> getArtistBillboard(Connection connection, int week_id, long idLong, boolean doListeners) {
        String table = doListeners ? "weekly_billboard_artist_listeners" : "weekly_billboard_artist_scrobbles";
        String metric = doListeners ? "listeners" : "scrobble_count";
        String streakFunction = doListeners ? "streak_billboard_artist" : "streak_billboard_artist_scrobbles";

        String queryString =
                "SELECT \n" +
                        "\ta.id,b.name,b.url,week_id," + metric + ",position,@t := a.id,@t2 := week_id,\n" +
                        " (select min(position) from " + table + " t where t.artist_id = a.artist_id and t.guild_id = a.guild_id  and week_id = a.week_id - 1   ) as last_week,\n" +
                        "(select min(position) from " + table + " t where t.artist_id = a.artist_id and t.guild_id = a.guild_id ) as peak,\n" +
                        streakFunction + "(a.id) as streak  \n" +
                        "  from " + table + " a  join artist b on a.artist_id = b.id where guild_id = ? and week_id = ?  order by position asc ," + metric + " desc  \n" +
                        "  \n" +
                        "  \n" +
                        "  \n" +
                        "  ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(1, idLong);
            preparedStatement.setInt(2, week_id);


            /* Execute query. */
            List<BillboardEntity> bills = new ArrayList<>();
            ResultSet resultSet = preparedStatement.executeQuery();
            return getArtistEntities(metric, bills, resultSet);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }


    }

    private List<BillboardEntity> getArtistEntities(String metric, List<BillboardEntity> bills, ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            String artist = resultSet.getString("name");
            int position = resultSet.getInt("position");
            int listeners = resultSet.getInt(metric);

            int peak = resultSet.getInt("peak");
            int streak = resultSet.getInt("streak");
            int previous_week = resultSet.getInt("last_week");
            String url = resultSet.getString("url");
            bills.add(new BillboardEntity(null, artist, (long) listeners, peak, streak, previous_week, position, url));
        }

        return bills;
    }

    @Override
    public List<BillboardEntity> getGlobalArtistBillboard(Connection connection, int week_id, boolean doListeners) {
        String table = doListeners ? "weekly_billboard_artist_global_listeners" : "weekly_billboard_artist_global_scrobbles";
        String metric = doListeners ? "listeners" : "scrobble_count";
        String streakFunction = doListeners ? "streak_global_billboard_artist" : "streak_global_billboard_artist_scrobbles";

        String queryString =
                "SELECT \n" +
                        "\ta.id,b.name,b.url,week_id," + metric + ",position,@t := a.id,@t2 := week_id,\n" +
                        " (select min(position) from " + table + " t where t.artist_id = a.artist_id and week_id = a.week_id - 1   ) as last_week,\n" +
                        "(select min(position) from " + table + " t where t.artist_id = a.artist_id ) as peak,\n" +
                        streakFunction + "(a.id) as streak  \n" +
                        "  from " + table + " a  join artist b on a.artist_id = b.id where week_id = ?  order by position asc ," + metric + " desc  \n" +
                        "  \n" +
                        "  \n" +
                        "  \n" +
                        "  ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            /* Fill "preparedStatement". */
            preparedStatement.setInt(1, week_id);


            /* Execute query. */
            List<BillboardEntity> bills = new ArrayList<>();
            ResultSet resultSet = preparedStatement.executeQuery();
            return getArtistEntities(metric, bills, resultSet);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public List<BillboardEntity> getAlbumBillboard(Connection connection, int week_id, long idLong, boolean doListeners) {
        String table = doListeners ? "weekly_billboard_album_listeners" : "weekly_billboard_album_scrobbles";
        String metric = doListeners ? "listeners" : "scrobble_count";
        String streakFunction = doListeners ? "streak_billboard_album_listeners" : "streak_billboard_album_scrobbles";

        String queryString =
                "SELECT \n" +
                        "\ta.id,b.name,c.url,week_id,a.album_name," + metric + ",position,@t := a.id,@t2 := week_id,\n" +
                        " (select min(position) from " + table + " t where t.artist_id = a.artist_id and t.guild_id = a.guild_id and t.album_name = a.album_name  and week_id = a.week_id - 1   ) as last_week,\n" +
                        "(select min(position) from " + table + " t where t.artist_id = a.artist_id and  t.album_name = a.album_name and t.guild_id = a.guild_id ) as peak,\n" +
                        streakFunction + "(a.id) as streak  \n" +

                        "  from " + table + " a  join artist b on a.artist_id = b.id " +
                        " left join album c on a.artist_id = c.artist_id and c.album_name = a.album_name" +
                        " where guild_id = ? and week_id = ?  order by position asc ," + metric + " desc  \n" +
                        "  \n" +
                        "  \n" +
                        "  \n" +
                        "  ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(1, idLong);
            preparedStatement.setInt(2, week_id);


            /* Execute query. */
            return getBillboardEntities(metric, preparedStatement);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }


    }

    @Override
    public List<BillboardEntity> getGlobalAlbumBillboard(Connection connection, int week_id, boolean doListeners) {
        String table = doListeners ? "weekly_billboard_album_global_listeners" : "weekly_billboard_album_global_scrobbles";
        String metric = doListeners ? "listeners" : "scrobble_count";
        String streakFunction = doListeners ? "streak_global_billboard_album_listeners" : "streak_global_billboard_album_scrobbles";

        String queryString =
                "SELECT \n" +
                        "\ta.id,b.name,c.url,week_id,a.album_name," + metric + ",position,@t := a.id,@t2 := week_id,\n" +
                        " (select min(position) from " + table + " t where t.artist_id = a.artist_id and t.album_name = a.album_name  and week_id = a.week_id - 1   ) as last_week,\n" +
                        "(select min(position) from " + table + " t where t.artist_id = a.artist_id and  t.album_name = a.album_name  ) as peak,\n" +
                        streakFunction + "(a.id) as streak  \n" +

                        "  from " + table + " a  join artist b on a.artist_id = b.id " +
                        " left join album c on a.artist_id = c.artist_id and c.album_name = a.album_name" +
                        " where week_id = ?  order by position asc ," + metric + " desc  \n" +
                        "  \n" +
                        "  \n" +
                        "  \n" +
                        "  ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setInt(1, week_id);


            /* Execute query. */
            return getBillboardEntities(metric, preparedStatement);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }


    }

    @NotNull
    private List<BillboardEntity> getBillboardEntities(String metric, PreparedStatement preparedStatement) throws SQLException {
        List<BillboardEntity> bills = new ArrayList<>();
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String artist = resultSet.getString("name");
            int position = resultSet.getInt("position");
            int listeners = resultSet.getInt(metric);

            int peak = resultSet.getInt("peak");
            int streak = resultSet.getInt("streak");
            int previous_week = resultSet.getInt("last_week");
            String url = resultSet.getString("url");
            String album_name = resultSet.getString("album_name");

            bills.add(new BillboardEntity(artist, album_name
                    , (long) listeners, peak, streak, previous_week, position, url));
        }

        return bills;
    }

    @Override
    public void insertBillboardDataScrobbles(Connection con, int week_id, long guildId) {
        StringBuilder mySql =
                new StringBuilder("""
                        INSERT INTO  weekly_billboard_scrobbles  (guild_id,week_id,artist_id,track_name,position,scrobble_count)
                        SELECT  ? , ? , artist_id, track_name,rank() over w as 'cum',sum(scrobble_count) as listeners
                        from user_billboard_data a
                        join user b on a.lastfm_id = b.lastfm_id
                        join user_guild c on b.discord_id = c.discord_id\s
                        where guild_id = ? and week_id = ? \s
                        group by a.artist_id,a.track_name
                        window w as (order by sum(scrobble_count) desc)
                        order by listeners desc
                        limit 100\s""");


        insertBillboard(con, week_id, guildId, mySql);

    }

    private void insertBillboard(Connection con, int week_id, long guildId, StringBuilder mySql) {
        try {
            PreparedStatement preparedStatement = con.prepareStatement(mySql.toString());
            preparedStatement.setLong(1, guildId);
            preparedStatement.setInt(2, week_id);
            preparedStatement.setLong(3, guildId);
            preparedStatement.setInt(4, week_id);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void insertBillboardDataListeners(Connection con, int week_id, long guildId) {
        StringBuilder mySql =
                new StringBuilder("""
                        INSERT INTO  weekly_billboard_listeners (guild_id,week_id,artist_id,track_name,position,listeners)
                        SELECT ? , ? ,  artist_id, track_name,rank() over w as 'cum',count(*) as listeners
                        from user_billboard_data a
                        join user b on a.lastfm_id = b.lastfm_id
                        join user_guild c on b.discord_id = c.discord_id\s
                        where guild_id = ? and week_id = ? \s
                        group by a.artist_id,a.track_name
                        window w as (order by count(*) desc)
                        order by listeners desc
                        limit 100""");


        insertBillboard(con, week_id, guildId, mySql);

    }

    @Override
    public List<PreBillboardUserDataTimestamped> getUngroupedUserData(Connection connection, @Nullable Integer from, @Nullable Integer to, String lastfmId) {

        String queryString = "SELECT artist_id,track_name,`timestamp`  from user_billboard_data_scrobbles where lastfm_id = ?";
        if (from != null && to != null) {
            queryString += " and timestamp between ? and ?  ";

        } else {
            if (from != null) {
                queryString += " and timestamp >= ? ";
            }
            if (to != null) {
                queryString += " and timestamp <= ? ";
            }
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setString(i++, lastfmId);
            if (from != null) {
                preparedStatement.setTimestamp(i++, Timestamp.from(Instant.ofEpochSecond(from)));
            }
            if (to != null) {
                preparedStatement.setTimestamp(i, Timestamp.from(Instant.ofEpochSecond(to)));
            }
            /* Execute query. */
            return getPreBillboardUserDataTimestampeds(lastfmId, preparedStatement);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @NotNull
    private List<PreBillboardUserDataTimestamped> getPreBillboardUserDataTimestampeds(String lastfmId, PreparedStatement preparedStatement) throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();
        List<PreBillboardUserDataTimestamped> a = new ArrayList<>();
        while (resultSet.next()) {
            String track_name = resultSet.getString("track_name");
            long artist = resultSet.getLong("artist_id");
            Timestamp timestamp = resultSet.getTimestamp("timestamp");

            a.add(new PreBillboardUserDataTimestamped(artist, lastfmId, track_name, 1, timestamp));
        }

        return a;
    }

    @Override
    public List<PreBillboardUserDataTimestamped> getUngroupedUserData(Connection connection, String lastfmId, int weekId) {

        String queryString = "SELECT artist_id,track_name,`timestamp`  from user_billboard_data_scrobbles where week_id = ? and lastfm_id = ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setInt(1, weekId);
            preparedStatement.setString(2, lastfmId);

            /* Execute query. */
            return getPreBillboardUserDataTimestampeds(lastfmId, preparedStatement);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public List<PreBillboardUserData> getUserData(Connection connection, String lastfmId, int weekId) {

        String queryString = "SELECT artist_id,track_name,scrobble_count  from user_billboard_data where week_id = ? and lastfm_id = ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setInt(1, weekId);
            preparedStatement.setString(2, lastfmId);

            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();
            List<PreBillboardUserData> a = new ArrayList<>();
            while (resultSet.next()) {
                String track_name = resultSet.getString("track_name");
                long artist = resultSet.getLong("artist_id");
                int scrobbleCount = resultSet.getInt("scrobble_count");
                a.add(new PreBillboardUserData(artist, lastfmId, track_name, scrobbleCount));
            }

            return a;

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public void insertUserData(Connection connection, List<TrackWithArtistId> trackList, String lastfmId, int weekId) {

        StringBuilder mySql =
                new StringBuilder("INSERT ignore INTO  user_billboard_data_scrobbles" +
                        "                  (week_id,artist_id,track_name,lastfm_id,album_name,timestamp) VALUES (?,?,?,?,?,?) ");

        mySql.append(", (?,?,?,?,?,?)".repeat(Math.max(0, trackList.size() - 1)));

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(mySql.toString());
            for (int i = 0; i < trackList.size(); i++) {
                TrackWithArtistId trackWithArtistId = trackList.get(i);
                preparedStatement.setLong(6 * i + 1, weekId);

                preparedStatement.setLong(6 * i + 2, trackWithArtistId.getArtistId());
                preparedStatement.setString(6 * i + 3, trackWithArtistId.getName());
                preparedStatement.setString(6 * i + 4, lastfmId);
                String album = trackWithArtistId.getAlbum();
                preparedStatement.setString(6 * i + 5, album.isBlank() ? null : album);
                preparedStatement.setTimestamp(6 * i + 6, Timestamp.from(Instant.ofEpochSecond(trackWithArtistId.getUtc())));
            }
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public void insertBillboardDataScrobblesByArtist(Connection connection, int week_id, long guildId) {
        StringBuilder mySql =
                new StringBuilder("""
                        INSERT INTO  weekly_billboard_artist_scrobbles(guild_id,week_id,artist_id,position,scrobble_count)
                        SELECT  ? , ? , artist_id,rank() over w as 'cum',sum(scrobble_count) as listeners
                        from user_billboard_data a
                        join user b on a.lastfm_id = b.lastfm_id
                        join user_guild c on b.discord_id = c.discord_id\s
                        where guild_id = ? and week_id = ? \s
                        group by a.artist_id
                        window w as (order by sum(scrobble_count) desc)
                        order by listeners desc
                        limit 100\s""");


        insertBillboard(connection, week_id, guildId, mySql);

    }

    @Override
    public void insertBillboardDataListenersByArtist(Connection connection, int week_id, long guildId) {
        StringBuilder mySql =
                new StringBuilder("""
                        INSERT INTO  weekly_billboard_artist_listeners (guild_id,week_id,artist_id,position,listeners)
                         SELECT ? , ? ,  artist_id,rank() over w as 'cum',count(distinct b.discord_id) as listeners
                        from user_billboard_data a
                        join user b on a.lastfm_id = b.lastfm_id
                        join user_guild c on b.discord_id = c.discord_id\s
                        where guild_id = ? and week_id = ? \s
                        group by a.artist_id
                        window w as (order by count(distinct b.discord_id) desc)
                        order by listeners desc
                        limit 100""");


        insertBillboard(connection, week_id, guildId, mySql);
    }

    @Override
    public void insertBillboardDataListenersByAlbum(Connection connection, int week_id, long guildId) {
        StringBuilder mySql =
                new StringBuilder("""
                        INSERT INTO  weekly_billboard_album_listeners(guild_id,week_id,artist_id,album_name,position,listeners)
                        SELECT ? , ? ,  artist_id,album_name,rank() over w as 'cum',count(distinct b.discord_id) as listeners
                        from user_billboard_data a
                        join user b on a.lastfm_id = b.lastfm_id
                        join user_guild c on b.discord_id = c.discord_id\s
                        where guild_id = ? and week_id = ? and a.album_name is not null \s
                        group by a.artist_id,a.album_name
                        window w as (order by count(distinct b.discord_id) desc)
                        order by listeners desc
                        limit 100""");


        insertBillboard(connection, week_id, guildId, mySql);

    }

    @Override
    public void insertBillboardDataScrobblesByAlbum(Connection connection, int week_id, long guildId) {
        StringBuilder mySql =
                new StringBuilder("""
                        INSERT INTO  weekly_billboard_album_scrobbles(guild_id,week_id,artist_id,album_name,position,scrobble_count)
                        SELECT  ? , ? , artist_id,album_name,rank() over w as 'cum',sum(scrobble_count) as listeners
                        from user_billboard_data a
                        join user b on a.lastfm_id = b.lastfm_id
                        join user_guild c on b.discord_id = c.discord_id\s
                        where guild_id = ? and week_id = ? and a.album_name is not null \s
                        group by a.artist_id,a.album_name window w as (order by sum(scrobble_count) desc)
                        order by listeners desc
                        limit 100""");


        insertBillboard(connection, week_id, guildId, mySql);
    }

    @Override
    public List<BillboardEntity> getGlobalBillboard(Connection connection, int weekId, boolean doListeners) {
        String table = doListeners ? "weekly_billboard_global_listeners" : "weekly_billboard_global_scrobbles";
        String metric = doListeners ? "listeners" : "scrobble_count";
        String streakFunction = doListeners ? "streak_global_billboard_track" : "streak_billboard_global_track_scrobbles";

        String queryString =
                "SELECT \n" +
                        "\ta.id,b.name,b.url,week_id,track_name," + metric + ",position,@t := a.id,@t2 := week_id,\n" +
                        " (select min(position) from " + table + " t where t.artist_id = a.artist_id  and t.track_name = a.track_name and week_id = a.week_id - 1   ) as last_week,\n" +
                        "(select min(position) from " + table + " t where t.artist_id = a.artist_id and t.track_name = a.track_name ) as peak,\n" +
                        streakFunction + "(a.id) as streak  \n" +
                        "  from " + table + " a  join artist b on a.artist_id = b.id where week_id = ?  order by position asc ," + metric + " desc  \n" +
                        "  \n" +
                        "  \n" +
                        "  \n" +
                        "  ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(1, weekId);


            /* Execute query. */
            List<BillboardEntity> bills = new ArrayList<>();
            ResultSet resultSet = preparedStatement.executeQuery();
            return getBillboardEntities(metric, bills, resultSet);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void insertGlobalBillboardDataScrobblesByAlbum(Connection connection, int week_id) {
        StringBuilder mySql =
                new StringBuilder("""
                        INSERT INTO  weekly_billboard_album_global_scrobbles(week_id,artist_id,album_name,position,scrobble_count)
                        SELECT   ? , artist_id,album_name,rank() over w as 'cum',sum(scrobble_count) as listeners
                        from user_billboard_data a
                         where week_id = ? and a.album_name is not null \s
                        group by a.artist_id,a.album_name window w as (order by sum(scrobble_count) desc)
                        order by listeners desc
                        limit 100\s""");


        try {
            PreparedStatement preparedStatement = connection.prepareStatement(mySql.toString());
            preparedStatement.setLong(1, week_id);
            preparedStatement.setInt(2, week_id);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void insertGlobalBillboardDataListenersByAlbum(Connection connection, int week_id) {
        StringBuilder mySql =
                new StringBuilder("""
                        INSERT INTO  weekly_billboard_album_global_listeners(week_id,artist_id,album_name,position,listeners)
                        SELECT ? ,  artist_id,album_name,rank() over w as 'cum',count(distinct a.lastfm_id)  as listeners
                        from user_billboard_data a
                        where week_id = ? and a.album_name is not null \s
                        group by a.artist_id,a.album_name
                        window w as (order by count(distinct a.lastfm_id) desc)
                        order by listeners desc
                        limit 100""");


        try {
            PreparedStatement preparedStatement = connection.prepareStatement(mySql.toString());
            preparedStatement.setLong(1, week_id);
            preparedStatement.setInt(2, week_id);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void insertGlobalBillboardDataScrobblesByArtist(Connection connection, int week_id) {
        StringBuilder mySql =
                new StringBuilder("""
                        INSERT INTO  weekly_billboard_artist_global_scrobbles(week_id,artist_id,position,scrobble_count)
                        SELECT   ? , artist_id,rank() over w as 'cum',sum(scrobble_count) as listeners
                        from user_billboard_data a
                        where week_id = ? \s
                        group by a.artist_id
                        window w as (order by sum(scrobble_count) desc)
                        order by listeners desc
                        limit 100\s""");


        try {
            PreparedStatement preparedStatement = connection.prepareStatement(mySql.toString());
            preparedStatement.setInt(1, week_id);
            preparedStatement.setInt(2, week_id);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void insertGlobalBillboardDataListenersByArtist(Connection connection, int week_id) {
        StringBuilder mySql =
                new StringBuilder("""
                        INSERT INTO  weekly_billboard_artist_global_listeners(week_id,artist_id,position,listeners)
                        SELECT ? ,  artist_id,rank() over w as 'cum',count(distinct a.lastfm_id)  as listeners
                        from user_billboard_data a
                        where week_id = ? \s
                        group by a.artist_id
                        window w as (order by count(distinct a.lastfm_id)  desc)
                        order by listeners desc
                        limit 100""");


        try {
            PreparedStatement preparedStatement = connection.prepareStatement(mySql.toString());
            preparedStatement.setInt(1, week_id);
            preparedStatement.setInt(2, week_id);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void insertGlobalBillboardDataScrobbles(Connection connection, int week_id) {
        StringBuilder mySql =
                new StringBuilder("""
                        INSERT INTO  weekly_billboard_global_scrobbles(week_id,artist_id,track_name,position,scrobble_count) SELECT   ? , artist_id, track_name,rank() over w as 'cum',sum(scrobble_count) as listeners
                        from user_billboard_data a
                        where week_id = ? \s
                        group by a.artist_id,a.track_name
                        window w as (order by sum(scrobble_count) desc)
                        order by listeners desc
                        limit 100\s""");


        try {
            PreparedStatement preparedStatement = connection.prepareStatement(mySql.toString());
            preparedStatement.setInt(1, week_id);
            preparedStatement.setInt(2, week_id);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void insertGlobalBillboardDataListeners(Connection connection, int week_id) {
        StringBuilder mySql =
                new StringBuilder("""
                        INSERT INTO  weekly_billboard_global_listeners(week_id,artist_id,track_name,position,listeners)
                        SELECT ? ,  artist_id, track_name,rank() over w as 'cum',count(*) as listeners
                        from user_billboard_data a
                        where week_id = ? \s
                        group by a.artist_id,a.track_name
                        window w as (order by count(*) desc)
                        order by listeners desc
                        limit 100""");


        try {
            PreparedStatement preparedStatement = connection.prepareStatement(mySql.toString());
            preparedStatement.setInt(1, week_id);
            preparedStatement.setInt(2, week_id);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

/*

    @Override
    public void generateBillboardFromLocalData(Connection connection, long guildId, int weekId) {

        StringBuilder mySql =
                new StringBuilder("INSERT INTO  weekly_billboard" +
                        "                  (guild_id,week_id,artist_id,track_name,position,listeners) " +
                        "SELECT (?,? artist_id, track_name,position,count(*) as listeners, sum(scrobble_count) as scrobble_count from" +
                        "user_billboard_data a " +
                        "join user b on a.lastfm_id = b.lastfm_id" +
                        "join user_guild c on b.discord_id = c.discord_id " +
                        "where guild_id = ? " +
                        "group by a.artist_id,a.track_name" l



        try {
            PreparedStatement preparedStatement = con.prepareStatement(mySql.toString());
            for (int i = 0; i < billboards.size(); i++) {
                BillboardEntity scrobbledAlbum = billboards.get(i);
                preparedStatement.setLong(6 * i + 1, guildId);

                preparedStatement.setInt(6 * i + 2, week_id);
                preparedStatement.setLong(6 * i + 3, scrobbledAlbum.getArtistId());
                preparedStatement.setString(6 * i + 4, scrobbledAlbum.getName());
                preparedStatement.setInt(6 * i + 5, i);
                preparedStatement.setLong(6 * i + 6, scrobbledAlbum.getListeners());

            }
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    ;
*/

    }

    @Override
    public void groupUserData(Connection connection, String lastfmId, int week_id) {

        StringBuilder sql = new StringBuilder("INSERT INTO  user_billboard_data" +
                "                  (week_id,artist_id,track_name,lastfm_id,album_name,scrobble_count) select week_id,artist_id,track_name,lastfm_id,album_name,count(*) from " +
                "user_billboard_data_scrobbles where week_id = ? and lastfm_id = ? group by artist_id,track_name");


        try {
            PreparedStatement preparedStatement = connection.prepareStatement(String.valueOf(sql));
            preparedStatement.setInt(1, week_id);
            preparedStatement.setString(2, lastfmId);
            preparedStatement.execute();
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public void cleanUserData(Connection connection, String lastfmId, int weekId) {
        String queryString = "DELETE   FROM user_billboard_data  WHERE lastfm_id = ? and week_id = ?  ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setString(i++, lastfmId);
            preparedStatement.setInt(i, weekId);


            preparedStatement.executeUpdate();


        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }
}
