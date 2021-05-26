package dao;

import dao.entities.*;
import dao.exceptions.ChuuServiceException;
import dao.exceptions.InstanceNotFoundException;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UserGuildDaoImpl implements UserGuildDao {


    @Override
    public void createGuild(Connection con, long guildId) {
        String queryString = "INSERT IGNORE INTO  guild"
                             + " (guild_id) " + " VALUES (?) ";

        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            preparedStatement.setLong(1, guildId);
            /* Execute query. */
            preparedStatement.executeUpdate();

            /* Get generated identifier. */

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void insertUserData(Connection con, LastFMData lastFMData) {
        String queryString = "INSERT INTO  user"
                             + " (lastfm_id, discord_id) " + " VALUES (?, ?) ON DUPLICATE KEY UPDATE lastfm_id=" + "?";

        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setString(i++, lastFMData.getName());
            preparedStatement.setLong(i++, lastFMData.getDiscordId());
            preparedStatement.setString(i, lastFMData.getName());


            /* Execute query. */
            preparedStatement.executeUpdate();

            /* Get generated identifier. */

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void insertTempUser(Connection con, long discordId, String token) {
        String queryString = "INSERT INTO  pending_login"
                             + " (discord_id,token ) " + " VALUES ( ?,?) ON DUPLICATE KEY UPDATE token  = VALUES(token)";

        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(i++, discordId);
            preparedStatement.setString(i, token);


            /* Execute query. */
            preparedStatement.executeUpdate();

            /* Get generated identifier. */

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public LastFMData findLastFmData(Connection con, long discordId) throws InstanceNotFoundException {

        String queryString = "SELECT   discord_id, lastfm_id,role,private_update,notify_image,chart_mode,whoknows_mode,remaining_mode,default_x, default_y,privacy_mode,notify_rating,private_lastfm,timezone,show_botted,token,sess,scrobbling,color,own_tags,artist_threshold,chart_options FROM user WHERE discord_id = ?";

        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(i, discordId);


            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                throw new dao.exceptions.InstanceNotFoundException(discordId);
            }

            /* Get results. */
            i = 1;
            long resDiscordID = resultSet.getLong(i++);
            String lastFmID = resultSet.getString(i++);
            Role role = Role.valueOf(resultSet.getString(i++));
            boolean privateUpdate = resultSet.getBoolean(i++);
            boolean notify_image = resultSet.getBoolean(i++);
            ChartMode chartMode = ChartMode.valueOf(resultSet.getString(i++));
            WhoKnowsMode whoKnowsMode = WhoKnowsMode.valueOf(resultSet.getString(i++));
            RemainingImagesMode remainingImagesMode = RemainingImagesMode.valueOf(resultSet.getString(i++));
            int defaultX = resultSet.getInt(i++);
            int defaultY = resultSet.getInt(i++);
            PrivacyMode privacyMode = PrivacyMode.valueOf(resultSet.getString(i++));
            boolean ratingNotify = resultSet.getBoolean(i++);
            boolean privateLastfmId = resultSet.getBoolean(i++);
            TimeZone tz = TimeZone.getTimeZone(Objects.requireNonNullElse(resultSet.getString(i++), "GMT"));
            boolean showBotted = resultSet.getBoolean(i++);
            String token = (resultSet.getString(i++));
            String session = (resultSet.getString(i++));
            boolean scrobbling = (resultSet.getBoolean(i++));
            String color = resultSet.getString(i++);
            EmbedColor embedColor = EmbedColor.fromString(color);
            boolean ownTags = (resultSet.getBoolean(i++));
            int artistThreshold = (resultSet.getInt(i++));
            long chartmodes = (resultSet.getLong(i));

            return new LastFMData(lastFmID, resDiscordID, role, privateUpdate, notify_image, whoKnowsMode, chartMode, remainingImagesMode, defaultX, defaultY, privacyMode, ratingNotify, privateLastfmId, showBotted, tz, token, session, scrobbling, embedColor, ownTags, artistThreshold, ChartOptions.getChartOptions(chartmodes));

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<Long> guildsFromUser(Connection connection, long userId) {
        String queryString = "SELECT discord_id,guild_id  FROM user_guild  WHERE discord_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(i, userId);

            List<Long> returnList = new LinkedList<>();
            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                long guildId = resultSet.getLong("guild_Id");

                returnList.add(guildId);

            }
            return returnList;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public MultiValuedMap<Long, Long> getWholeUserGuild(Connection connection) {
        String queryString = "SELECT discord_id,guild_id  FROM user_guild ";

        MultiValuedMap<Long, Long> map = new ArrayListValuedHashMap<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                long guildId = resultSet.getLong("guild_Id");
                long discordId = resultSet.getLong("discord_Id");
                map.put(guildId, discordId);


            }

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return map;
    }

    @Override
    public void updateLastFmData(Connection con, LastFMData lastFMData) {
        String queryString = "UPDATE user SET lastfm_id= ? WHERE discord_id = ?";

        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setString(i++, lastFMData.getName());

            preparedStatement.setLong(i, lastFMData.getDiscordId());

            /* Execute query. */
            int updatedRows = preparedStatement.executeUpdate();

            if (updatedRows == 0) {
                throw new ChuuServiceException("E");
            }

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void removeUser(Connection con, Long discordId) {
        String queryString = "DELETE FROM  user WHERE discord_id = ?";

        deleteIdLong(con, discordId, queryString);

    }

    private void deleteIdLong(Connection con, Long discordID, String queryString) {
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(i, discordID);

            /* Execute query. */
            int removedRows = preparedStatement.executeUpdate();

            if (removedRows == 0) {
                System.err.println("No rows removed");
            }

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void removeUserGuild(Connection con, long discordId, long guildId) {
        String queryString = "DELETE FROM  user_guild  WHERE discord_id = ? AND guild_id = ?";

        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(i++, discordId);
            preparedStatement.setLong(i, guildId);


            /* Execute query. */
            int removedRows = preparedStatement.executeUpdate();

            if (removedRows == 0) {
                System.err.println("No rows removed");
            }

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<UsersWrapper> getAll(Connection connection, long guildId) {
        String queryString = "SELECT a.discord_id, a.lastfm_id,a.role,a.timezone FROM user a JOIN (SELECT discord_id FROM user_guild WHERE guild_id = ? ) b ON a.discord_id = b.discord_id";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */

            /* Execute query. */
            preparedStatement.setLong(1, guildId);
            return getUsersWrappers(preparedStatement);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<UsersWrapper> getAllNotObscurify(Connection connection, long guildId) {
        String queryString = """
                SELECT a.discord_id, a.lastfm_id,a.role,a.timezone FROM user a
                JOIN (SELECT discord_id FROM user_guild WHERE guild_id = ? ) b
                ON a.discord_id = b.discord_id
                WHERE NOT exists(SELECT 1 FROM obscurity m WHERE m.lastfm_id = a.lastfm_id)
                """;
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */

            /* Execute query. */
            preparedStatement.setLong(1, guildId);
            return getUsersWrappers(preparedStatement);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<UsersWrapper> getAllNonPrivate(Connection connection, long guildId) {
        String queryString = "SELECT a.discord_id, a.lastfm_id,a.role,a.timezone,a.last_update FROM user a JOIN (SELECT discord_id FROM user_guild WHERE guild_id = ? ) b ON a.discord_id = b.discord_id WHERE a.private_update = FALSE";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */

            /* Execute query. */
            preparedStatement.setLong(1, guildId);
            return getUsersWrappersTimestamp(preparedStatement);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @NotNull
    private List<UsersWrapper> getUsersWrappers(PreparedStatement preparedStatement) throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();
        List<UsersWrapper> returnList = new ArrayList<>();
        while (resultSet.next()) {

            String name = resultSet.getString("a.lastFm_Id");
            long discordID = resultSet.getLong("a.discord_ID");
            Role role = Role.valueOf(resultSet.getString(3));
            TimeZone tz = TimeZone.getTimeZone(Objects.requireNonNullElse(resultSet.getString(4), "GMT"));


            returnList.add(new UsersWrapper(discordID, name, role, tz));
        }
        return returnList;
    }

    @NotNull
    private List<UsersWrapper> getUsersWrappersTimestamp(PreparedStatement preparedStatement) throws SQLException {
        ResultSet resultSet = preparedStatement.executeQuery();
        List<UsersWrapper> returnList = new ArrayList<>();
        while (resultSet.next()) {

            String name = resultSet.getString("a.lastFm_Id");
            long discordID = resultSet.getLong("a.discord_ID");
            Role role = Role.valueOf(resultSet.getString(3));
            TimeZone tz = TimeZone.getTimeZone(Objects.requireNonNullElse(resultSet.getString(4), "GMT"));
            Timestamp timestamp = resultSet.getTimestamp(5);
            UsersWrapper e = new UsersWrapper(discordID, name, role, tz);
            e.setTimestamp(Math.toIntExact(timestamp.toInstant().getEpochSecond()));
            returnList.add(e);
        }
        return returnList;
    }

    @Override
    public void addGuild(Connection con, long userId, long guildId) {
        String queryString = "INSERT IGNORE INTO  user_guild"
                             + " ( discord_id,guild_id) " + " VALUES (?, ?) ";

        updateUserGuild(con, userId, guildId, queryString);
    }

    private void updateUserGuild(Connection con, long userId, long guildId, String queryString) {
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(i++, userId);
            preparedStatement.setLong(i, guildId);


            /* Execute query. */
            preparedStatement.executeUpdate();

            /* Get generated identifier. */

            /* Return booking. */

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void addLogo(Connection con, long guildID, BufferedImage image) {
        String queryString = "UPDATE  guild SET  logo = ? WHERE guild_id = ? ";
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            InputStream is = new ByteArrayInputStream(baos.toByteArray());
            preparedStatement.setBlob(i++, new BufferedInputStream(is));
            preparedStatement.setLong(i, guildID);

            preparedStatement.executeUpdate();


        } catch (SQLException | IOException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public boolean isFlagged(Connection con, String lastfm) {
        String queryString = "SELECT * FROM botted  WHERE lastfm_id = ?";

        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

            int i = 1;
            preparedStatement.setString(i, lastfm);

            ResultSet resultSet = preparedStatement.executeQuery();

            return resultSet.next();


        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public void removeLogo(Connection connection, long guildId) {
        String queryString = "UPDATE  guild SET logo = NULL WHERE guild_id = ?";

        deleteIdLong(connection, guildId, queryString);
    }

    @Override
    public InputStream findLogo(Connection connection, long guildID) {
        String queryString = "SELECT logo FROM guild WHERE guild_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(i, guildID);


            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                return null;
            }

            /* Get results. */
            return resultSet.getBinaryStream("logo");


        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public long getDiscordIdFromLastFm(Connection connection, String lastFmName) throws InstanceNotFoundException {
        String queryString = "SELECT a.discord_id " +
                             "FROM   user a" +
                             " WHERE  a.lastfm_id = ?  ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setString(i, lastFmName);

            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                throw new InstanceNotFoundException("Not found ");
            }

            /* Get results. */

            return resultSet.getLong(1);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public long getDiscordIdFromLastFm(Connection connection, String lastFmName, long guildId) throws InstanceNotFoundException {
        String queryString = "SELECT a.discord_id " +
                             "FROM   user a" +
                             " JOIN  user_guild  b " +
                             "ON a.discord_id = b.discord_id " +
                             " WHERE  a.lastfm_id = ? AND b.guild_id = ? ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setString(i++, lastFmName);
            preparedStatement.setLong(i, guildId);

            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                throw new InstanceNotFoundException(lastFmName);
            }

            /* Get results. */

            return resultSet.getLong(1);

        } catch (SQLException e) {
            // Illegal mix of collation
            if (e.getErrorCode() == 1267) {
                throw new InstanceNotFoundException(lastFmName);
            }
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public LastFMData findByLastFMId(Connection connection, String lastFmID) throws InstanceNotFoundException {
        String queryString = "SELECT a.discord_id, a.lastfm_id , a.role,a.private_update,a.notify_image," +
                             "a.chart_mode,a.whoknows_mode,a.remaining_mode,a.default_x,a.default_y,a.privacy_mode,a.notify_rating,a.private_lastfm," +
                             "timezone,show_botted,token,sess,scrobbling,color,own_tags,artist_threshold,chart_options " +
                             "FROM   user a" +
                             " WHERE  a.lastfm_id = ? ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setString(i, lastFmID);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                throw new InstanceNotFoundException("Not found ");
            }
            long aLong = resultSet.getLong(1);
            // String string = resultSet.getString(2);
            Role role = Role.valueOf(resultSet.getString(3));
            boolean privateUpdate = resultSet.getBoolean(4);
            boolean imageNOtify = resultSet.getBoolean(5);
            ChartMode chartMode = ChartMode.valueOf(resultSet.getString(6));
            WhoKnowsMode whoKnowsMode = WhoKnowsMode.valueOf(resultSet.getString(7));
            RemainingImagesMode remainingImagesMode = RemainingImagesMode.valueOf(resultSet.getString(8));
            int defaultX = resultSet.getInt(9);
            int defaultY = resultSet.getInt(10);
            PrivacyMode privacyMode = PrivacyMode.valueOf(resultSet.getString(11));
            boolean ratingNotify = resultSet.getBoolean(12);
            boolean privateLastfmId = resultSet.getBoolean(13);
            TimeZone tz = TimeZone.getTimeZone(Objects.requireNonNullElse(resultSet.getString(14), "GMT"));
            boolean showBotted = resultSet.getBoolean(15);
            String token = (resultSet.getString(16));
            String session = (resultSet.getString(17));
            boolean scrobbling = (resultSet.getBoolean(18));
            String color = resultSet.getString(19);
            EmbedColor embedColor = EmbedColor.fromString(color);
            boolean ownTags = resultSet.getBoolean(20);
            int artistThreshold = resultSet.getInt(21);
            long chart_options = resultSet.getInt(22);
            return new LastFMData(lastFmID, aLong, role, privateUpdate, imageNOtify, whoKnowsMode, chartMode, remainingImagesMode, defaultX, defaultY, privacyMode, ratingNotify, privateLastfmId, showBotted, tz, token, session, scrobbling, embedColor, ownTags, artistThreshold, ChartOptions.getChartOptions(chart_options));


            /* Get results. */


        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<UsersWrapper> getAll(Connection connection) {
        String queryString = "SELECT a.discord_id, a.lastfm_id, a.role, timezone FROM user a ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */

            /* Execute query. */
            return getUsersWrappers(preparedStatement);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void removeRateLimit(Connection connection, long discordId) {
        String queryString = "DELETE FROM rate_limited WHERE discord_id = ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setLong(1, discordId);
            /* Fill "preparedStatement". */

            /* Execute query. */
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void upsertRateLimit(Connection connection, long discordId, float queriesPerSecond) {
        String queryString = "INSERT INTO  rate_limited"
                             + " (discord_id,queries_second) " + " VALUES (?, ?) ON DUPLICATE KEY UPDATE queries_second = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(i++, discordId);
            preparedStatement.setFloat(i++, queriesPerSecond);
            preparedStatement.setFloat(i, queriesPerSecond);


            /* Execute query. */
            preparedStatement.executeUpdate();

            /* Get generated identifier. */

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    @Override
    public void insertServerDisabled(Connection connection, long discordId, String commandName) {
        String queryString = "INSERT IGNORE INTO  command_guild_disabled"
                             + " (guild_id,command_name) VALUES (?, ?) ";

        executeLongString(connection, discordId, commandName, queryString);
    }

    private void executeLongString(Connection connection, long discordId, String commandName, String queryString) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(i++, discordId);
            preparedStatement.setString(i, commandName);


            /* Execute query. */
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void insertChannelCommandStatus(Connection connection, long discordId, long channelId, String commandName, boolean enabled) {
        String queryString = "INSERT IGNORE INTO  command_guild_channel_disabled"
                             + " (guild_id,channel_id,command_name,enabled) VALUES (?, ?,? , ? ) ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(i++, discordId);
            preparedStatement.setLong(i++, channelId);
            preparedStatement.setString(i++, commandName);
            preparedStatement.setBoolean(i, enabled);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void deleteChannelCommandStatus(Connection connection, long discordId, long channelId, String commandName) {
        String queryString = "DELETE FROM command_guild_channel_disabled"
                             + " WHERE guild_id = ? AND channel_id = ? AND command_name = ? ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(i++, discordId);
            preparedStatement.setLong(i++, channelId);
            preparedStatement.setString(i, commandName);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void deleteServerCommandStatus(Connection connection, long discordId, String commandName) {
        String queryString = "DELETE FROM command_guild_disabled"
                             + " WHERE guild_id = ?  and command_name = ? ";

        executeLongString(connection, discordId, commandName, queryString);
    }

    @Override
    public MultiValuedMap<Long, String> initServerCommandStatuses(Connection connection) {
        String queryString = "SELECT guild_id,command_name FROM command_guild_disabled";
        MultiValuedMap<Long, String> map = new HashSetValuedHashMap<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                long guildId = resultSet.getLong("guild_id");
                String commandName = resultSet.getString("command_name");

                map.put(guildId, commandName);

            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return map;
    }

    @Override
    public MultiValuedMap<Pair<Long, Long>, String> initServerChannelsCommandStatuses(Connection connection, boolean enabled) {
        String queryString = "SELECT guild_id,channel_id,command_name FROM command_guild_channel_disabled WHERE enabled = ? ";
        MultiValuedMap<Pair<Long, Long>, String> map = new HashSetValuedHashMap<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            preparedStatement.setBoolean(1, enabled);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                long guildId = resultSet.getLong("guild_id");
                long channel_id = resultSet.getLong("channel_id");

                String commandName = resultSet.getString("command_name");

                map.put(Pair.of(guildId, channel_id), commandName);

            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return map;
    }

    @Override
    public void setUserProperty(Connection connection, long discordId, String property, boolean chartEmbed) {
        String queryString = "UPDATE  user SET  " + property + " = ? WHERE discord_id = ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setBoolean(i++, chartEmbed);
            preparedStatement.setLong(i, discordId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public void setUserProperty(Connection connection, long discordId, String property, String value) {
        String queryString = "UPDATE  user SET  " + property + " = ? WHERE discord_id = ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setString(i++, value);
            preparedStatement.setLong(i, discordId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void setUserProperty(Connection connection, long discordId, String property, Integer value) {
        String queryString = "UPDATE  user SET  " + property + " = ? WHERE discord_id = ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setInt(i++, value);
            preparedStatement.setLong(i, discordId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void setGuildProperty(Connection connection, long guildId, String property, boolean value) {
        String queryString = "UPDATE  guild SET  " + property + " = ? WHERE guild_id = ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setBoolean(i++, value);
            preparedStatement.setLong(i, guildId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public void setGuildProperty(Connection connection, long guildId, String property, String value) {
        String queryString = "UPDATE  guild SET  " + property + " = ? WHERE guild_id = ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setString(i++, value);
            preparedStatement.setLong(i, guildId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }


    @Override
    public <T extends Enum<T>> void setUserProperty(Connection connection, long discordId, String propertyName, T value) {
        String queryString = "UPDATE  user SET  " + propertyName + " = ? WHERE discord_id = ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setString(i++, value.toString().replaceAll("-", "_"));
            preparedStatement.setLong(i, discordId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public GuildProperties getGuild(Connection connection, long discordId) throws InstanceNotFoundException {
        String queryString = "SELECT " +
                             "guild_id,prefix,crown_threshold,whoknows_mode,chart_mode,remaining_mode,delete_message,disabled_warning,override_reactions,allow_reactions,color,allow_covers,override_color FROM guild WHERE guild_id = ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            preparedStatement.setLong(1, discordId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                throw new InstanceNotFoundException(discordId);
            }
            long guild_id = resultSet.getLong("guild_id");
            String prefix = resultSet.getString("prefix");
            int crown_threshold = resultSet.getInt("crown_threshold");
            String whoknows_mode = resultSet.getString("whoknows_mode");
            WhoKnowsMode whoKnowsMode = whoknows_mode == null ? null : WhoKnowsMode.valueOf(whoknows_mode);
            String chart_mode = resultSet.getString("chart_mode");
            ChartMode chartMode = chart_mode == null ? null : ChartMode.valueOf(chart_mode);
            String remaining_mode = resultSet.getString("remaining_mode");
            boolean deleteMessages = resultSet.getBoolean("delete_message");
            boolean disabledWarning = resultSet.getBoolean("disabled_warning");
            boolean allow_covers = resultSet.getBoolean("allow_covers");
            OverrideMode override_reactions = OverrideMode.valueOf(resultSet.getString("override_reactions"));
            boolean allowReactions = resultSet.getBoolean("allow_reactions");
            String color = resultSet.getString("color");
            EmbedColor embedColor = EmbedColor.fromString(color);
            OverrideColorMode override_color = OverrideColorMode.valueOf(resultSet.getString("override_color"));


            RemainingImagesMode remainingImagesMode = remaining_mode == null ? null : RemainingImagesMode.valueOf(remaining_mode);

            return new GuildProperties(guild_id, prefix.charAt(0), crown_threshold, chartMode, whoKnowsMode, override_reactions, allowReactions, remainingImagesMode, deleteMessages, disabledWarning, embedColor, allow_covers, override_color);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public LastFMData findLastFmData(Connection con, long discordId, long guildId) throws InstanceNotFoundException {

        String queryString = "SELECT   a.discord_id, lastfm_id,role,private_update,notify_image," +
                             "IFNULL(c.chart_mode,a.chart_mode), " +
                             "IFNULL(c.whoknows_mode,a.whoknows_mode), " +
                             "IFNULL(c.remaining_mode,a.remaining_mode)" +
                             ", default_x, default_y " +
                             ", a.privacy_mode," +
                             "a.notify_rating, " +
                             " private_lastfm," +
                             "timezone, " +
                             "show_botted, token, sess,scrobbling,IFNULL(c.color,a.color),allow_covers,own_tags,chart_options " +
                             "FROM user a JOIN guild c" +

                             " WHERE a.discord_id = ? AND  c.guild_id = ? ";

        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(i++, discordId);
            preparedStatement.setLong(i, guildId);



            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                throw new dao.exceptions.InstanceNotFoundException(discordId);
            }

            /* Get results. */
            i = 1;
            long resDiscordID = resultSet.getLong(i++);
            String lastFmID = resultSet.getString(i++);
            Role role = Role.valueOf(resultSet.getString(i++));
            boolean privateUpdate = resultSet.getBoolean(i++);
            boolean notify_image = resultSet.getBoolean(i++);
            ChartMode chartMode = ChartMode.valueOf(resultSet.getString(i++));
            WhoKnowsMode whoKnowsMode = WhoKnowsMode.valueOf(resultSet.getString(i++));
            RemainingImagesMode remainingImagesMode = RemainingImagesMode.valueOf(resultSet.getString(i++));
            int defaultX = resultSet.getInt(i++);
            int defaultY = resultSet.getInt(i++);
            PrivacyMode privacyMode = PrivacyMode.valueOf(resultSet.getString(i++));
            boolean ratingNotify = resultSet.getBoolean(i++);
            boolean privateLastfmId = resultSet.getBoolean(i++);
            TimeZone tz = TimeZone.getTimeZone(Objects.requireNonNullElse(resultSet.getString(i++), "GMT"));
            boolean showBotted = resultSet.getBoolean(i++);
            String token = (resultSet.getString(i++));
            String session = (resultSet.getString(i++));
            boolean scrobbling = (resultSet.getBoolean(i++));
            String color = resultSet.getString(i++);
            EmbedColor embedColor = EmbedColor.fromString(color);
            boolean ownTags = (resultSet.getBoolean(i++));
            int artistThreshold = (resultSet.getInt(i++));
            long chart_options = (resultSet.getLong(i));


            return new LastFMData(lastFmID, resDiscordID, role, privateUpdate, notify_image, whoKnowsMode, chartMode, remainingImagesMode, defaultX, defaultY, privacyMode, ratingNotify, privateLastfmId, showBotted, tz, token, session, scrobbling, embedColor, ownTags, artistThreshold, ChartOptions.getChartOptions(chart_options));

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public <T extends Enum<T>> void setGuildProperty(Connection connection, long discordId, String propertyName, @Nullable T value) {
        String queryString = "UPDATE  guild SET " + propertyName + " = ? WHERE guild_id = ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            if (value == null) {
                preparedStatement.setNull(i++, Types.VARCHAR);

            } else {
                preparedStatement.setString(i++, value.toString().replaceAll("-", "_"));
            }
            preparedStatement.setLong(i, discordId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void setChartDefaults(Connection connection, long discordId, int x, int y) {
        String queryString = "UPDATE  user SET  default_x =?, default_y = ?  WHERE discord_id = ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setInt(i++, x);
            preparedStatement.setInt(i++, y);
            preparedStatement.setLong(i, discordId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void serverBlock(Connection connection, long discordId, long guildId) {
        String queryString = "insert ignore into server_blocked(discord_id,guild_id)  values (?,?) ";
        updateUserGuild(connection, discordId, guildId, queryString);
    }

    @Override
    public boolean isUserServerBanned(Connection connection, long userId, long guildID) {
        String queryString = "SELECT EXISTS( SELECT guild_id,discord_id FROM server_blocked WHERE guild_id = ? AND discord_id = ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            preparedStatement.setLong(1, guildID);
            preparedStatement.setLong(2, userId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                return false;
            }
            return resultSet.getBoolean(1);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public void serverUnblock(Connection connection, long discordId, long guildId) {
        String queryString = "delete  from server_blocked where discord_id = ? and guild_id = ?";
        updateUserGuild(connection, discordId, guildId, queryString);
    }

    @Override
    public long getNPRaw(Connection connection, long discordId) {
        String queryString = "SELECT  np_mode FROM user WHERE discord_id = ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(i, discordId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            return 1;

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public void setNpRaw(Connection connection, long discordId, long raw) {
        String queryString = "UPDATE user SET np_mode = ? WHERE discord_id = ?";

        updateUserGuild(connection, raw, discordId, queryString);

    }

    @Override
    public void setChartOptionsRaw(Connection connection, long discordId, long raw) {
        String queryString = "UPDATE user SET chart_options = ? WHERE discord_id = ?";
        updateUserGuild(connection, raw, discordId, queryString);

    }

    @Override
    public long getServerNPRaw(Connection connection, long guildId) {
        String queryString = "SELECT  np_mode FROM guild WHERE guild_id = ? ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(i, guildId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            return -1;

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public void setServerNpRaw(Connection connection, long guild_id, long raw) {
        String queryString = "UPDATE guild SET np_mode = ? WHERE guild_id = ?";

        updateUserGuild(connection, raw, guild_id, queryString);

    }

    @Override
    public void setTimezoneUser(Connection connection, TimeZone timeZone, long userId) {
        String queryString = "UPDATE user SET timezone = ? WHERE discord_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setString(i++, timeZone.getID());
            preparedStatement.setLong(i, userId);

            /* Execute query. */
            preparedStatement.executeUpdate();

            /* Get generated identifier. */

            /* Return booking. */

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public TimeZone getTimezone(Connection connection, long userId) {
        String queryString = "SELECT timezone FROM user WHERE discord_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(i, userId);

            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                return TimeZone.getTimeZone("GMT");
            } else {
                String string = resultSet.getString(1);
                return TimeZone.getTimeZone(Objects.requireNonNullElse(string, "GMT"));
            }


        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }


    }

    @Override
    public Set<Long> getGuildsWithDeletableMessages(Connection connection) {
        String queryString = "Select guild_id from guild WHERE delete_message = true";
        return getIdList(connection, queryString);
    }

    @Override
    public Set<Long> getGuildsWithCoversOn(Connection connection) {
        String queryString = "Select guild_id from guild WHERE allow_covers = true";
        return getIdList(connection, queryString);
    }

    @Override
    public Set<Long> getGuildsWithEmptyColorOverride(Connection connection) {
        String queryString = "Select guild_id from guild WHERE override_color = 'empty'";
        return getIdList(connection, queryString);
    }

    @Override
    public Set<Long> getGuildsDontRespondOnErrros(Connection connection) {
        String queryString = "Select guild_id from guild WHERE disabled_warning = true";
        return getIdList(connection, queryString);
    }

    @Override
    public void changeDiscordId(Connection connection, long userId, String lastFmID) {
        String queryString = "UPDATE user SET discord_id = ? WHERE lastfm_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(i++, userId);
            preparedStatement.setString(i, lastFmID);

            /* Execute query. */
            preparedStatement.executeUpdate();

            /* Get generated identifier. */

            /* Return booking. */

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public CommandStats getCommandStats(long discordId, Connection connection) {
        String queryString = "SELECT  (SELECT COUNT(*) FROM command_logs WHERE discord_id = ?), (SELECT COUNT(*) FROM alt_url WHERE discord_id = ? )";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(i++, discordId);
            preparedStatement.setLong(i, discordId);

            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return new CommandStats(resultSet.getInt(1), resultSet.getInt(2));
            }
            return new CommandStats(0, 0);


        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public Set<LastFMData> findScrobbleableUsers(Connection con, long guildId) {
        Set<LastFMData> lastFMData = new HashSet<>();
        String queryString = "SELECT discord_id, lastfm_id,role,private_update,notify_image,chart_mode,whoknows_mode,remaining_mode,default_x, default_y,privacy_mode,notify_rating,private_lastfm,timezone,show_botted,token,sess,scrobbling,color,own_tags,artist_threshold,chart_options FROM user a natural  join user_guild b where b.guild_id = ? and sess is not null and scrobbling = true ";

        return new HashSet<>(getServerData(con, guildId, queryString));

    }

    @Override
    public void insertServerReactions(Connection connection, long guildId, List<String> reactions) {
        String variables = IntStream.range(0, reactions.size()).mapToObj(t -> "(?,?,?)").collect(Collectors.joining(","));
        String queryString = "INSERT INTO  server_reactions"
                             + " (guild_id, reaction,position)  VALUES  " + variables;

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            for (int i = 0; i < reactions.size(); i++) {
                preparedStatement.setLong(i * 3 + 1, guildId);
                preparedStatement.setString(i * 3 + 2, reactions.get(i));
                preparedStatement.setInt(i * 3 + 3, i);
            }
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void insertUserReactions(Connection connection, long userId, List<String> reactions) {
        String variables = IntStream.range(0, reactions.size()).mapToObj(t -> "(?,?,?)").collect(Collectors.joining(","));
        String queryString = "INSERT INTO  user_reactions"
                             + " (discord_id, reaction,position)  VALUES  " + variables;
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            for (int i = 0; i < reactions.size(); i++) {
                preparedStatement.setLong(i * 3 + 1, userId);
                preparedStatement.setString(i * 3 + 2, reactions.get(i));
                preparedStatement.setInt(i * 3 + 3, i);
            }
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void clearServerReactions(Connection connection, long guildId) {
        Set<Long> returnSet = new HashSet<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM server_reactions WHERE guild_id = ? ")) {

            preparedStatement.setLong(1, guildId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void clearUserReacts(Connection connection, long userId) {
        Set<Long> returnSet = new HashSet<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM user_reactions WHERE discord_id = ? ")) {

            preparedStatement.setLong(1, userId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<String> getServerReactions(Connection connection, long guildId) {
        List<String> results = new ArrayList<>();
        String queryString = "SELECT reaction FROM server_reactions WHERE guild_id = ? ORDER BY position ASC";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setLong(1, guildId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                results.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return results;

    }

    @Override
    public List<String> getUserReacts(Connection connection, long userId) {
        List<String> results = new ArrayList<>();
        String queryString = "SELECT reaction FROM user_reactions WHERE discord_id = ? ORDER BY position ASC";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setLong(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                results.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return results;

    }

    @Override
    public Map<Long, Color[]> getServerWithPalette(Connection connection) {
        Map<Long, Color[]> map = new HashMap<>();
        String queryString = "SELECT guild_id,color FROM guild WHERE color IS NOT NULL AND color <> 'ROLE'";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                long color = resultSet.getLong(1);
                String string = resultSet.getString(2);
                EmbedColor embedColor = EmbedColor.fromString(string);
                assert embedColor != null : "Cannot be null due to filter on where";
                map.put(color, embedColor.mapList());
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return map;
    }

    @Override
    public Map<Long, Color[]> getUsersWithPalette(Connection connection) {
        Map<Long, Color[]> map = new HashMap<>();
        String queryString = "SELECT discord_id,color FROM user WHERE color IS NOT NULL AND color <> 'ROLE'";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                long discordId = resultSet.getLong(1);
                String string = resultSet.getString(2);
                EmbedColor color = EmbedColor.fromString(string);
                assert color != null : "Cannot be null due to filter on where";
                map.put(discordId, color.mapList());
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return map;
    }

    @Override
    public Map<Long, EmbedColor.EmbedColorType> getUserColorTypes(Connection connection) {
        Map<Long, EmbedColor.EmbedColorType> map = new HashMap<>();
        String queryString = "select discord_id,color   from user where color is not null";
        return parseColorType(connection, map, queryString);
    }

    private Map<Long, EmbedColor.EmbedColorType> parseColorType(Connection connection, Map<Long, EmbedColor.EmbedColorType> map, String queryString) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                long discordId = resultSet.getLong(1);
                String embedColor = resultSet.getString(2);
                EmbedColor color = EmbedColor.fromString(embedColor);
                assert color != null : "Cannot be null due to filter on where";
                map.put(discordId, color.type());
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return map;
    }

    @Override
    public Map<Long, EmbedColor.EmbedColorType> getServerColorTypes(Connection connection) {
        Map<Long, EmbedColor.EmbedColorType> map = new HashMap<>();
        String queryString = "select guild_id,color  from guild where color is not null";
        return parseColorType(connection, map, queryString);
    }

    @Override
    public void flagBottedDB(String lastfmId, Connection connection) {
        String queryString = "UPDATE user SET botted_account = TRUE WHERE lastfm_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setString(1, lastfmId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    @Override
    public void flagBotted(String lastfmId, Connection connection) {
        String queryString = "INSERT IGNORE botted(lastfm_id) VALUES (?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setString(1, lastfmId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void insertBannedCover(Connection connection, long albumId, String cover) {
        String queryString = "INSERT INTO banned_cover(album_id,replacement_cover) VALUES (?,?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            preparedStatement.setLong(1, albumId);
            preparedStatement.setString(2, cover);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void removeBannedCover(Connection connection, long albumId, String cover) {
        String queryString = "DELETE FROM  banned_cover WHERE album_id = ? AND replacement_cover =? ";


        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            preparedStatement.setLong(1, albumId);
            preparedStatement.setString(2, cover);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void insertServerCustomUrl(Connection connection, long altId, long guildId, long artistId) {
        String queryString = "INSERT INTO artist_custom_images(alt_id,guild_id,artist_id) VALUES (?,?,?) ON DUPLICATE KEY UPDATE alt_id = VALUES(alt_id)";


        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setLong(1, altId);
            preparedStatement.setLong(2, guildId);
            preparedStatement.setLong(3, artistId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    @Override
    public List<LastFMData> getAllData(Connection con, long guildId) {
        Set<LastFMData> lastFMData = new HashSet<>();
        String queryString = "SELECT   discord_id, lastfm_id,role,private_update,notify_image,chart_mode,whoknows_mode,remaining_mode,default_x, default_y,privacy_mode,notify_rating,private_lastfm,timezone,show_botted,token,sess,scrobbling,color,own_tags,artist_threshold,chart_options FROM user a natural  join user_guild b where b.guild_id = ? ";

        return getServerData(con, guildId, queryString);

    }

    @Override
    public Set<Long> getServerBlocked(Connection connection, long guildId) {
        String queryString = "SELECT discord_id FROM  server_blocked WHERE guild_id = ? ";
        Set<Long> rest = new HashSet<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            preparedStatement.setLong(1, guildId);


            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                rest.add(resultSet.getLong(1));
            }

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return rest;

    }

    @Override
    public List<RoleColour> getRoles(Connection connection, long guildId) {
        String queryString = """
                SELECT id,guild_id,colour,start_breakpoint,end_breakpoint,role_id
                FROM role_colour_server
                WHERE guild_id = ?
                ORDER BY start_breakpoint ASC,end_breakpoint ASC
                """;
        List<RoleColour> roles = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setLong(i, guildId);


            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {


                i = 1;
                long discordId = resultSet.getLong(i++);
                long guildId2 = resultSet.getLong(i++);
                Color color = Color.decode("0x" + resultSet.getString(i++));
                int start = resultSet.getInt(i++);
                int end = resultSet.getInt(i++);
                long role_id = resultSet.getLong(i);

                roles.add(new RoleColour(discordId, guildId2, color, start, end, role_id));
            }

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return roles;

    }

    @Override
    public void addRole(Connection connection, long guildId, int first, int second, String rest, Color color, long roleId) {
        String queryString = "INSERT INTO  role_colour_server"
                             + " (guild_id, colour,start_breakpoint,end_breakpoint,role_id) VALUES (?, ?,?,?,?) ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            int i = 1;
            preparedStatement.setLong(i++, guildId);
            preparedStatement.setString(i++, String.format("%06X", 0xFFFFFF & color.getRGB()));
            preparedStatement.setInt(i++, first);
            preparedStatement.setInt(i++, second);
            preparedStatement.setLong(i, roleId);


            preparedStatement.executeUpdate();


        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public VoiceAnnouncement getGuildVoiceAnnouncement(Connection connection, long guildId) {
        String queryString = """
                SELECT announcement_id,announcement_enabled
                FROM guild
                WHERE guild_id = ?
                """;
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setLong(i, guildId);


            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                long channelId = resultSet.getLong(i++);
                boolean enabled = resultSet.getBoolean(i);
                return new VoiceAnnouncement(channelId == 0 ? null : channelId, enabled);
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return new VoiceAnnouncement(null, true);

    }

    @Override
    public void setGuildVoiceAnnouncement(Connection connection, long guildId, VoiceAnnouncement voiceAnnouncement) {

        String queryString = """
                UPDATE  guild
                SET announcement_id = ? ,announcement_enabled = ?
                WHERE guild_id = ?
                """;
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            if (voiceAnnouncement.channelId() == null) {
                preparedStatement.setNull(1, Types.BIGINT);
            } else {
                preparedStatement.setLong(1, voiceAnnouncement.channelId());
            }
            preparedStatement.setBoolean(2, voiceAnnouncement.enabled());
            preparedStatement.setLong(3, guildId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    @Override
    public void insertObscurity(Connection connection, String lastfmId, double obscurity) {
        String queryString = """
                INSERT INTO obscurity(lastfm_id,score) VALUES (?,?) ON DUPLICATE KEY UPDATE score = VALUES(score)
                """;
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            int i = 1;
            preparedStatement.setString(i++, lastfmId);
            preparedStatement.setDouble(i, obscurity);


            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public ServerStats getServerStats(Connection connection, long guildId) {


        String queryString = """
                WITH
                users AS (
                SELECT discord_id FROM user_guild WHERE guild_id = ?
                )
                ,averages AS
                   (SELECT guild_id,
                           avg(score) AS score
                       FROM obscurity o
                                  JOIN user u ON o.lastfm_id = u.lastfm_id
                              	JOIN user_guild ug ON u.discord_id = ug.discord_id
                                  GROUP BY guild_id
                                  ORDER BY score DESC
                   ),
                      indexes AS
                   (SELECT guild_id,
                           score,
                           (rank() OVER (
                                         ORDER BY score DESC)) AS i
                    FROM averages), counted AS
                   (SELECT COUNT(*) AS tf
                    FROM averages),
                    commands AS (SELECT * FROM command_logs WHERE guild_id = ?),
                    top_command AS (SELECT command,count(*) AS command_count FROM commands GROUP BY command ORDER BY command_count  DESC LIMIT 1)
                 SELECT counted.tf AS total_servers,
                        guild_id,
                        score,
                        i AS `RANK`,
                 
                   (SELECT COUNT(*)
                    FROM users ) AS user_count,
                 
                   (SELECT COUNT(*)
                    FROM commands ) commands_count,
                 (SELECT command
                    FROM top_command) top_name,
                    (SELECT command_count
                    FROM top_command) top_name_count,
                 
                   (SELECT COUNT(*)
                    FROM past_recommendations WHERE receiver_id IN (SELECT discord_id FROM users)) recommedation_count,
                 
                                
                 
                   (SELECT COUNT(*)
                    FROM randomlinks WHERE discord_id IN (SELECT discord_id FROM users)) random_count,
                    
                    (SELECT COUNT(*)
                    FROM alt_url WHERE discord_id IN (SELECT discord_id FROM users)) image_count,
                 
                 
                   (SELECT COUNT(*)
                    FROM vote WHERE discord_id IN (SELECT discord_id FROM user)) vote_count
                 
                 
                 FROM indexes
                 JOIN counted
                 WHERE guild_id = ?""";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
            preparedStatement.setLong(1, guildId);
            preparedStatement.setLong(2, guildId);
            preparedStatement.setLong(3, guildId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                long total_servers = resultSet.getLong("total_servers");
                long guild_id = resultSet.getLong("guild_id");
                long score = resultSet.getLong("score");
                long rank = resultSet.getLong("rank");
                long user_count = resultSet.getLong("user_count");
                long commands_count = resultSet.getLong("commands_count");
                String top_name = resultSet.getString("top_name");
                long top_name_count = resultSet.getLong("top_name_count");
                long recommedation_count = resultSet.getLong("recommedation_count");
                long random_count = resultSet.getLong("random_count");
                long image_count = resultSet.getLong("image_count");
                long vote_count = resultSet.getLong("vote_count");
                ObscurityStats obscurityStats = new ObscurityStats(score, rank, total_servers, guildId);
                return new ServerStats(obscurityStats, user_count, commands_count, top_name, top_name_count, random_count, vote_count, image_count, recommedation_count);
            }
            throw new ChuuServiceException();
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    private List<LastFMData> getServerData(Connection con, long guildId, String queryString) {
        List<LastFMData> lastFMData = new ArrayList<>();
        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(i, guildId);


            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {


                /* Get results. */
                i = 1;
                long resDiscordID = resultSet.getLong(i++);
                String lastFmID = resultSet.getString(i++);
                Role role = Role.valueOf(resultSet.getString(i++));
                boolean privateUpdate = resultSet.getBoolean(i++);
                boolean notify_image = resultSet.getBoolean(i++);
                ChartMode chartMode = ChartMode.valueOf(resultSet.getString(i++));
                WhoKnowsMode whoKnowsMode = WhoKnowsMode.valueOf(resultSet.getString(i++));
                RemainingImagesMode remainingImagesMode = RemainingImagesMode.valueOf(resultSet.getString(i++));
                int defaultX = resultSet.getInt(i++);
                int defaultY = resultSet.getInt(i++);
                PrivacyMode privacyMode = PrivacyMode.valueOf(resultSet.getString(i++));
                boolean ratingNotify = resultSet.getBoolean(i++);
                boolean privateLastfmId = resultSet.getBoolean(i++);
                TimeZone tz = TimeZone.getTimeZone(Objects.requireNonNullElse(resultSet.getString(i++), "GMT"));
                boolean showBotted = resultSet.getBoolean(i++);
                String token = (resultSet.getString(i++));
                String session = (resultSet.getString(i++));
                boolean scrobbling = (resultSet.getBoolean(i++));
                String color = resultSet.getString(i++);
                EmbedColor embedColor = EmbedColor.fromString(color);
                boolean ownTags = (resultSet.getBoolean(i++));
                int artistThreshold = (resultSet.getInt(i++));
                long chartmodes = (resultSet.getLong(i));
                LastFMData e = new LastFMData(lastFmID, resDiscordID, role, privateUpdate, notify_image, whoKnowsMode, chartMode, remainingImagesMode, defaultX, defaultY, privacyMode, ratingNotify, privateLastfmId, showBotted, tz, token, session, scrobbling, embedColor, ownTags, artistThreshold, ChartOptions.getChartOptions(chartmodes));
                lastFMData.add(e);
            }

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return lastFMData;
    }

    @NotNull
    private Set<Long> getIdList(Connection connection, String queryString) {
        Set<Long> returnSet = new HashSet<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                long guildId = resultSet.getLong("guild_id");

                returnSet.add(guildId);

            }

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
        return returnSet;
    }

}


