package dao;

import dao.entities.LastFMData;
import dao.entities.UsersWrapper;
import core.exceptions.InstanceNotFoundException;
import org.apache.commons.collections4.map.MultiValueMap;
import org.intellij.lang.annotations.Language;

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
	public void insertUserData(Connection con, LastFMData lastFMData) {
		/* Create "queryString". */
		String queryString = "INSERT INTO  lastfm"
				+ " (lastFmId, discordID) " + " VALUES (?, ?) ON DUPLICATE KEY UPDATE lastFmId=" + "?";

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
		String queryString = "SELECT discordID, lastFmid FROM lastfm WHERE discordID = ?";

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
			String lastFmID = resultSet.getString(i);

			return new LastFMData(lastFmID, resDiscordID);

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<Long> guildList(Connection connection, long userId) {
		@Language("MySQL") String queryString = "Select discordId,guildId  FROM user_guild  WHERE discordID = ?";

		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

			/* Fill "preparedStatement". */
			int i = 1;
			preparedStatement.setLong(i, userId);

			List<Long> returnList = new LinkedList<>();
			/* Execute query. */
			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {

				long guildId = resultSet.getLong("guildId");

				returnList.add(guildId);

			}
			return returnList;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public MultiValueMap<Long, Long> getWholeUser_Guild(Connection connection) {
		@Language("MySQL") String queryString = "Select discordId,guildId  FROM user_guild ";

		MultiValueMap<Long, Long> map = new MultiValueMap<>();
		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {

				long guildId = resultSet.getLong("guildId");
				long discordId = resultSet.getLong("discordId");
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
		String queryString = "UPDATE lastfm SET lastFmId= ? WHERE discordID = ?";

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
		@Language("MySQL") String queryString = "DELETE FROM  lastfm WHERE" + " discordID = ?";

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
		@Language("MySQL") String queryString = "DELETE FROM  user_guild" + " where discordID = ? and guildId = ?";

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
		String queryString = "Select a.discordID, a.lastFmId FROM lastfm a join (Select discordId from user_guild where guildId = ? ) b on a.discordID = b.discordId";
		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

			/* Fill "preparedStatement". */

			/* Execute query. */
			preparedStatement.setLong(1, guildId);
			ResultSet resultSet = preparedStatement.executeQuery();
			List<UsersWrapper> returnList = new ArrayList<>();
			while (resultSet.next()) {

				String name = resultSet.getString("a.lastFmId");
				long discordID = resultSet.getLong("a.discordID");
				returnList.add(new UsersWrapper(discordID, name));
			}
			return returnList;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void addGuild(Connection con, long userId, long guildId) {
		/* Create "queryString". */
		String queryString = "INSERT IGNORE INTO  user_guild"
				+ " ( discordId,guildId) " + " VALUES (?, ?) ";

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
		@Language("MySQL") String queryString = "INSERT INTO guild_logo(guildId, logo) VALUES (?,?)" +
				" ON DUPLICATE KEY UPDATE logo = ?";
		try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

			/* Fill "preparedStatement". */
			int i = 1;
			preparedStatement.setLong(i++, guildID);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "png", baos);
			InputStream is = new ByteArrayInputStream(baos.toByteArray());
			ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
			ImageIO.write(image, "png", baos2);
			InputStream is2 = new ByteArrayInputStream(baos.toByteArray());
			preparedStatement.setBlob(i++, new BufferedInputStream(is));

			preparedStatement.setBlob(i, is2);
			preparedStatement.executeUpdate();


		} catch (SQLException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void removeLogo(Connection connection, long guildId) {
		/* Create "queryString". */
		@Language("MySQL") String queryString = "DELETE FROM guild_logo WHERE  guildId = ?";

		deleteIdLong(connection, guildId, queryString);
	}

	@Override
	public InputStream findLogo(Connection connection, long guildID) {
		/* Create "queryString". */
		String queryString = "SELECT logo FROM guild_logo WHERE guildId = ?";

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
		@Language("MySQL") String queryString = "Select a.discordID " +
				"from   lastfm a" +
				" join  user_guild  b " +
				"on a.discordID = b.discordId " +
				" where  a.lastFmId = ? and b.guildId = ? ";

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

}
