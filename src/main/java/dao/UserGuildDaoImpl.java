package dao;

import core.exceptions.InstanceNotFoundException;
import dao.entities.LastFMData;
import dao.entities.Role;
import dao.entities.UsersWrapper;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insertUserData(Connection con, LastFMData lastFMData) {
        /* Create "queryString". */
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
            throw new RuntimeException(e);
        }
    }

    @Override
    public LastFMData findLastFmData(Connection con, long discordId) throws InstanceNotFoundException {

        /* Create "queryString". */
        String queryString = "SELECT   discord_id, lastfm_id,role FROM user WHERE discord_id = ?";

        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setLong(i, discordId);


            /* Execute query. */
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                throw new core.exceptions.InstanceNotFoundException(discordId);
            }

            /* Get results. */
            i = 1;
            long resDiscordID = resultSet.getLong(i++);
            String lastFmID = resultSet.getString(i++);
            Role role = Role.valueOf(resultSet.getString(i));

            return new LastFMData(lastFmID, resDiscordID, role);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Long> guildsFromUser(Connection connection, long userId) {
        @Language("MariaDB") String queryString = "SELECT discord_id,guild_id  FROM user_guild  WHERE discord_id = ?";

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
            throw new RuntimeException(e);
        }
    }

    @Override
    public MultiValuedMap<Long, Long> getWholeUser_Guild(Connection connection) {
        @Language("MariaDB") String queryString = "SELECT discord_id,guild_id  FROM user_guild ";

        MultiValuedMap<Long, Long> map = new ArrayListValuedHashMap<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {

                long guildId = resultSet.getLong("guild_Id");
                long discordId = resultSet.getLong("discord_Id");
                map.put(guildId, discordId);


            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    @Override
    public void updateLastFmData(Connection con, LastFMData lastFMData) {
        /* Create "queryString". */
        String queryString = "UPDATE user SET lastfm_id= ? WHERE discord_id = ?";

        try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */
            int i = 1;
            preparedStatement.setString(i++, lastFMData.getName());

            preparedStatement.setLong(i, lastFMData.getDiscordId());

            /* Execute query. */
            int updatedRows = preparedStatement.executeUpdate();

            if (updatedRows == 0) {
                throw new RuntimeException("E");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeUser(Connection con, Long discordId) {
        /* Create "queryString". */
        @Language("MariaDB") String queryString = "DELETE FROM  user WHERE discord_id = ?";

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
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeUserGuild(Connection con, long discordId, long guildId) {
        /* Create "queryString". */
        @Language("MariaDB") String queryString = "DELETE FROM  user_guild  WHERE discord_id = ? AND guild_id = ?";

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
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<UsersWrapper> getAll(Connection connection, long guildId) {
        String queryString = "SELECT a.discord_id, a.lastfm_id,a.role FROM user a JOIN (SELECT discord_id FROM user_guild WHERE guild_id = ? ) b ON a.discord_id = b.discord_id";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */

            /* Execute query. */
            preparedStatement.setLong(1, guildId);
            return getUsersWrappers(preparedStatement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
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

            returnList.add(new UsersWrapper(discordID, name, role));
        }
        return returnList;
    }

    @Override
    public void addGuild(Connection con, long userId, long guildId) {
        /* Create "queryString". */
        String queryString = "INSERT IGNORE INTO  user_guild"
                             + " ( discord_id,guild_id) " + " VALUES (?, ?) ";

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
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addLogo(Connection con, long guildID, BufferedImage image) {
        @Language("MariaDB") String queryString = "UPDATE  guild SET  logo = ? WHERE guild_id = ? ";
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
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeLogo(Connection connection, long guildId) {
        /* Create "queryString". */
        @Language("MariaDB") String queryString = "UPDATE  guild SET logo = NULL WHERE guild_id = ?";

        deleteIdLong(connection, guildId, queryString);
    }

    @Override
    public InputStream findLogo(Connection connection, long guildID) {
        /* Create "queryString". */
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
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getDiscordIdFromLastFm(Connection connection, String lastFmName, long guildId) throws InstanceNotFoundException {
        @Language("MariaDB") String queryString = "SELECT a.discord_id " +
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
                throw new InstanceNotFoundException("Not found ");
            }

            /* Get results. */

            return resultSet.getLong(1);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public LastFMData findByLastFMId(Connection connection, String lastFmID) throws InstanceNotFoundException {
        @Language("MariaDB") String queryString = "SELECT a.discord_id, a.lastfm_id , a.role " +
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
            String string = resultSet.getString(2);
            Role role = Role.valueOf(resultSet.getString(3));

            /* Get results. */

            return new LastFMData(string, aLong, role);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<UsersWrapper> getAll(Connection connection) {
        String queryString = "SELECT a.discord_id, a.lastfm_id, a.role FROM user a ";
        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            /* Fill "preparedStatement". */

            /* Execute query. */
            return getUsersWrappers(preparedStatement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
