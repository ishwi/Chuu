package DAO;

import DAO.Entities.*;
import org.intellij.lang.annotations.Language;

import javax.imageio.ImageIO;
import javax.management.InstanceNotFoundException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Jdbc3CcSQLShowsDao extends AbstractSQLShowsDao {

	@Override
	public LastFMData create(Connection con, LastFMData lastFMData) {
		/* Create "queryString". */
		String queryString = "INSERT INTO lastfm.lastfm"
				+ " (lastFmId, discordID) " + " VALUES (?, ?) ON DUPLICATE KEY UPDATE lastFmId=" + "?";

		try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

			/* Fill "preparedStatement". */
			int i = 1;
			preparedStatement.setString(i++, lastFMData.getName());
			preparedStatement.setLong(i++, lastFMData.getShowID());
			preparedStatement.setString(i, lastFMData.getName());


			/* Execute query. */
			preparedStatement.executeUpdate();

			/* Get generated identifier. */

			/* Return booking. */
			return lastFMData;

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
	public InputStream findLogo(Connection connection, long guildID) throws InstanceNotFoundException {

		/* Create "queryString". */
		String queryString = "SELECT logo FROM guild_logo WHERE guildId = ?";

		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

			/* Fill "preparedStatement". */
			int i = 1;
			preparedStatement.setLong(i, guildID);


			/* Execute query. */
			ResultSet resultSet = preparedStatement.executeQuery();

			if (!resultSet.next()) {
				throw new InstanceNotFoundException("Not found ");
			}

			/* Get results. */
			return resultSet.getBinaryStream("logo");

			/* Return show. */

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void removeLogo(Connection connection, long guildId) {

		/* Create "queryString". */
		@Language("MySQL") String queryString = "DELETE FROM guild_logo WHERE" + " guildId = ?";

		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

			/* Fill "preparedStatement". */
			int i = 1;
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
	public void addUrl(Connection con, ArtistData artistData) {
		/* Create "queryString". */
		String queryString = "INSERT IGNORE INTO lastfm.artist_url"
				+ " ( artist_id,url) " + " VALUES (?, NULL)";

		try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

			/* Fill "preparedStatement". */
			int i = 1;
			preparedStatement.setString(i++, artistData.getArtist());
			//	preparedStatement.setString(i, artistData.getUrl());



			/* Execute query. */
			preparedStatement.executeUpdate();

			/* Get generated identifier. */

			/* Return booking. */

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void upsertUrl(Connection con, ArtistInfo artistInfo) {
		/* Create "queryString". */
		String queryString = "INSERT  INTO lastfm.artist_url"
				+ " ( artist_id,url) " + " VALUES (?, ?) ON DUPLICATE  KEY UPDATE url= ? ";

		try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

			/* Fill "preparedStatement". */
			int i = 1;
			preparedStatement.setString(i++, artistInfo.getArtistName());
			preparedStatement.setString(i++, artistInfo.getArtistUrl());
			preparedStatement.setString(i, artistInfo.getArtistUrl());


			/* Execute query. */
			preparedStatement.executeUpdate();

			/* Get generated identifier. */

			/* Return booking. */

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void addGuild(Connection con, long userId, long guildId) {
		/* Create "queryString". */
		String queryString = "INSERT IGNORE INTO lastfm.user_guild"
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
	public void setUpdatedTime(Connection connection, String id, Integer timestamp) {
		String queryString = "INSERT INTO lastfm.updated"
				+ " ( discordID,last_update) " + " VALUES (?, ?) ON DUPLICATE KEY UPDATE last_update=" + "?";
		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
			Timestamp timestamp1;
			if (timestamp == null) {
				timestamp1 = Timestamp.from(Instant.now());
			} else {
				timestamp1 = new Timestamp(timestamp.longValue() * 1000);
			}
			/* Fill "preparedStatement". */
			int i = 1;
			preparedStatement.setString(i++, id);
			preparedStatement.setTimestamp(i++, timestamp1);
			preparedStatement.setTimestamp(i, timestamp1);


			/* Execute query. */
			preparedStatement.executeUpdate();

			/* Get generated identifier. */

			/* Return booking. */
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public WrapperReturnNowPlaying knows(Connection con, String artist, long guildId) {

		String queryString = "Select temp.artist_id, temp.lastFMID,temp.playNumber,b.url, c.discordID " +
				"FROM (SELECT a.artist_id, a.lastFMID, a.playNumber " +
				"FROM lastfm.artist a  " +
				"where artist_id = ?" +
				"group by a.artist_id,a.lastFMID,a.playNumber " +
				"order by playNumber desc) temp " +
				"JOIN artist_url b ON temp.artist_id=b.artist_id " +
				"JOIN lastfm c on c.lastFmId = temp.lastFMID " +
				"JOIN user_guild d on c.discordID = d.discordId " +
				"where d.guildId = ? " +
				"ORDER BY temp.playNumber desc";
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
			int MAX_IN_DISPLAY = 10;
			while (resultSet.next() && (j < MAX_IN_DISPLAY && j < rows)) {
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
	public String getArtistUrl(Connection connection, String artist) {
		String queryString = "SELECT url FROM artist_url where artist_id = ? ";

		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
			preparedStatement.setString(1, artist);
			ResultSet resultSet = preparedStatement.executeQuery();


			/* Get generated identifier. */

			if (resultSet.next()) {
				return (resultSet.getString("url"));
			}
			/* Return booking. */

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return null;
	}


	@Override
	public Set<String> selectNullUrls(Connection connection) {
		Set<String> returnList = new HashSet<>();
		String queryString = "SELECT * FROM artist_url where url is null limit 30";
		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
			ResultSet resultSet = preparedStatement.executeQuery();


			/* Get generated identifier. */

			while (resultSet.next()) {
				returnList.add(resultSet.getString("artist_id"));
			}
			/* Return booking. */

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return returnList;
	}

	@Override
	public void addArtist(Connection con, ArtistData artistData) {
		/* Create "queryString". */
		String queryString = "INSERT INTO lastfm.artist"
				+ " ( lastFMID,artist_id,playNumber) " + " VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE playNumber=" + "?";

		try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {
			/* Fill "preparedStatement". */
			int i = 1;
			preparedStatement.setString(i++, artistData.getDiscordID());
			preparedStatement.setString(i++, artistData.getArtist());
			preparedStatement.setLong(i++, artistData.getCount());
			preparedStatement.setLong(i, artistData.getCount());


			/* Execute query. */
			preparedStatement.executeUpdate();

			/* Get generated identifier. */

			/* Return booking. */

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void upsertArtist(Connection con, ArtistData artistData) {
		/* Create "queryString". */
		String queryString = "INSERT INTO lastfm.artist"
				+ " ( lastFMID,artist_id,playNumber) " + " VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE playNumber= playNumber + " + "?";

		try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {
			/* Fill "preparedStatement". */
			int i = 1;
			preparedStatement.setString(i++, artistData.getDiscordID());
			preparedStatement.setString(i++, artistData.getArtist());
			preparedStatement.setLong(i++, artistData.getCount());
			preparedStatement.setLong(i, artistData.getCount());


			/* Execute query. */
			preparedStatement.executeUpdate();

			/* Get generated identifier. */

			/* Return booking. */

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}


}


