package DAO;

import DAO.Entities.*;
import main.Chuu;
import org.intellij.lang.annotations.Language;

import java.sql.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class UpdaterDaoImpl implements UpdaterDao {


	@Override
	public void addArtist(Connection con, ArtistData artistData) {
		/* Create "queryString". */
		String queryString = "INSERT INTO  artist"
				+ " ( lastFMID,artist_id,playNumber) " + " VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE playNumber=" + "?";

		insertArtistData(con, artistData, queryString);
	}

	private void insertArtistData(Connection con, ArtistData artistData, String queryString) {
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
	public UpdaterUserWrapper getLessUpdated(Connection connection) {
		@Language("MariaDB") String queryString = "Select a.discordID, a.lastFmId,b.last_update,b.control_timestamp FROM lastfm a LEFT JOIN updated b on a.lastFmId=b.discordID  order by  control_timestamp LIMIT 1";
		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

			/* Fill "preparedStatement". */

			/* Execute query. */
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {

				String name = resultSet.getString("a.lastFmId");
				long discordID = resultSet.getLong("a.discordID");
				Timestamp timestamp = resultSet.getTimestamp("b.last_update");
				Timestamp controlTimestamp = resultSet.getTimestamp("b.control_timestamp");

				return new UpdaterUserWrapper(discordID, name, ((int) timestamp.toInstant()
						.getEpochSecond()), ((int) controlTimestamp.toInstant().getEpochSecond()));
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	@Override
	public void addUrl(Connection con, ArtistData artistData) {
		/* Create "queryString". */
		String queryString = "INSERT IGNORE INTO  artist_url"
				+ " ( artist_id,url) " + " VALUES (?, NULL)";

		try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

			/* Fill "preparedStatement". */
			int i = 1;
			preparedStatement.setString(i, artistData.getArtist());
			//	preparedStatement.setString(i, artistData.getUrl());



			/* Execute query. */
			preparedStatement.executeUpdate();

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void setUpdatedTime(Connection connection, String id, Integer timestamp, Integer timestampControl) {
		String queryString = "INSERT INTO  updated"
				+ " ( discordID,last_update,control_timestamp) " + " VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE last_update= ?, control_timestamp = ?";
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
			preparedStatement.setString(i++, id);
			preparedStatement.setTimestamp(i++, timestamp1);
			preparedStatement.setTimestamp(i++, timestamp2);
			preparedStatement.setTimestamp(i++, timestamp1);
			preparedStatement.setTimestamp(i, timestamp2);


			/* Execute query. */
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void upsertArtist(Connection con, ArtistData artistData) {
		/* Create "queryString". */
		String queryString = "INSERT INTO  artist"
				+ " ( lastFMID,artist_id,playNumber) " + " VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE playNumber= playNumber + " + "?";

		insertArtistData(con, artistData, queryString);
	}

	@Override
	public void upsertUrl(Connection con, ArtistInfo artistInfo) {
		/* Create "queryString". */
		String queryString = "INSERT  INTO  artist_url"
				+ " ( artist_id,url) " + " VALUES (?, ?) ON DUPLICATE  KEY UPDATE url= ? ";

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
			preparedStatement.executeUpdate();

			/* Get generated identifier. */

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void upsertUrlBitMask(Connection con, ArtistInfo artistInfo, boolean bit) {
		/* Create "queryString". */
		String queryString = "INSERT  INTO  artist_url"
				+ " ( artist_id,url,correction_status) " + " VALUES (?, ?,?) ON DUPLICATE  KEY UPDATE url= ? ,correction_status = ?";

		try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

			/* Fill "preparedStatement". */
			int i = 1;
			preparedStatement.setString(i++, artistInfo.getArtist());
			preparedStatement.setString(i++, artistInfo.getArtistUrl());
			preparedStatement.setBoolean(i++, bit);

			preparedStatement.setString(i++, artistInfo.getArtistUrl());
			preparedStatement.setBoolean(i, bit);


			/* Execute query. */
			preparedStatement.executeUpdate();

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

		} catch (SQLException e) {
			Chuu.getLogger().warn(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return null;
	}

	@Override
	public Set<String> selectNullUrls(Connection connection, boolean doSpotifySearch) {
		Set<String> returnList = new HashSet<>();

		String queryString = doSpotifySearch ? "SELECT * FROM artist_url where url = \"\"  and  url_status = 1 or url is null limit 20" : "SELECT * FROM artist_url where url is null limit 20";
		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
			ResultSet resultSet = preparedStatement.executeQuery();


			/* Get generated identifier. */

			while (resultSet.next()) {
				returnList.add(resultSet.getString("artist_id"));
			}

		} catch (SQLException e) {
			Chuu.getLogger().warn(e.getMessage(), e);
		}
		return returnList;
	}

	@Override
	public void upsertSpotify(Connection con, ArtistInfo artistInfo) {
		/* Create "queryString". */
		String queryString = "INSERT  INTO  artist_url"
				+ " ( artist_id,url,url_status) " + " VALUES (?, ?,0) ON DUPLICATE  KEY UPDATE url= ? , url_status = 0";

		insertArtistInfo(con, artistInfo, queryString);
	}

	@Override
	public UpdaterStatus getUpdaterStatus(Connection connection, String artist_id) {
		String queryString = "SELECT url,correction_status,b.correction FROM artist_url a " +
				" left join corrections b on b.correction = a.artist_id" +
				" where a.artist_id = ? ";

		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
			preparedStatement.setString(1, artist_id);
			ResultSet resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {
				String url = resultSet.getString("url");
				boolean status = resultSet.getBoolean("correction_status");
				String correction = resultSet.getString("b.correction");
				return new UpdaterStatus(url, correction, status);
			}

		} catch (SQLException e) {
			Chuu.getLogger().warn(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return null;
	}


	@Override
	public void insertCorrection(Connection connection, String artist, String correction) {
		String queryString = "INSERT INTO  corrections"
				+ " ( artist_id,correction) " + " VALUES (?, ?) ON DUPLICATE KEY UPDATE correction= ?";
		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

			/* Fill "preparedStatement". */
			int i = 1;
			preparedStatement.setString(i++, artist);

			preparedStatement.setString(i++, correction);
			preparedStatement.setString(i, correction);

			preparedStatement.executeUpdate();

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void updateStatusBit(Connection connection, String artist_id) {
		@Language("MySQL") String queryString = "Update   artist_url set correction_status = 1 where artist_id = ?";
		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

			/* Fill "preparedStatement". */
			int i = 1;
			preparedStatement.setString(i, artist_id);
			preparedStatement.executeUpdate();

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String findCorrection(Connection connection, String artist) {
		String queryString = "SELECT  correction  FROM corrections where artist_id= ? ";

		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
			preparedStatement.setString(1, artist);
			ResultSet resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {
				return (resultSet.getString("correction"));
			}

		} catch (SQLException e) {
			Chuu.getLogger().warn(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return null;
	}

	@Override
	public void updateMetric(Connection connection, int metricId, long value) {
		@Language("MySQL") String queryString = "Update   metrics set value = value + ?  where id = ?";
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
		@Language("MySQL") String queryString = "delete   from artist where lastFMID = ? ";
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
	public boolean insertRandomUrl(Connection con, String url, long discordId, long guildId) {
		String queryString = "INSERT IGNORE INTO  randomLinks"
				+ " ( discordId,url,guildId) " + " VALUES (?,  ?, ?)";
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
		String queryString = "\n" +
				"\n" +
				"SELECT * FROM randomlinks WHERE url IN \n" +
				"    (SELECT url FROM (SELECT url FROM randomlinks ORDER BY RAND() LIMIT 1) random)\n" +
				"        ";
		try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

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
}

