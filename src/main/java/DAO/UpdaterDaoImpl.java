package DAO;

import DAO.Entities.ArtistData;
import DAO.Entities.ArtistInfo;
import DAO.Entities.UpdaterStatus;
import DAO.Entities.UsersWrapper;
import org.intellij.lang.annotations.Language;

import java.sql.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class UpdaterDaoImpl implements UpdaterDao {


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

	private void insertArtistInfo(Connection con, ArtistInfo artistInfo, String queryString) {
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
	public void addArtist(Connection con, ArtistData artistData) {
		/* Create "queryString". */
		String queryString = "INSERT INTO lastfm.artist"
				+ " ( lastFMID,artist_id,playNumber) " + " VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE playNumber=" + "?";

		insertArtistData(con, artistData, queryString);
	}

	@Override
	public UsersWrapper getLessUpdated(Connection connection) {
		@Language("MySQL") String queryString = "Select a.discordID, a.lastFmId,b.last_update FROM lastfm a LEFT JOIN updated b on a.lastFmId=b.discordID  order by  last_update asc LIMIT 1";
		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

			/* Fill "preparedStatement". */

			/* Execute query. */
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {

				String name = resultSet.getString("a.lastFmId");
				long discordID = resultSet.getLong("a.discordID");
				Timestamp timestamp = resultSet.getTimestamp("b.last_update");
				return new UsersWrapper(discordID, name, ((int) timestamp.toInstant().getEpochSecond()));
			}
			/* Return show. */

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return null;
	}


	@Override
	public void addUrl(Connection con, ArtistData artistData) {
		/* Create "queryString". */
		String queryString = "INSERT IGNORE INTO lastfm.artist_url"
				+ " ( artist_id,url) " + " VALUES (?, NULL)";

		try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

			/* Fill "preparedStatement". */
			int i = 1;
			preparedStatement.setString(i, artistData.getArtist());
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
	public void upsertArtist(Connection con, ArtistData artistData) {
		/* Create "queryString". */
		String queryString = "INSERT INTO lastfm.artist"
				+ " ( lastFMID,artist_id,playNumber) " + " VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE playNumber= playNumber + " + "?";

		insertArtistData(con, artistData, queryString);
	}


	@Override
	public void upsertUrl(Connection con, ArtistInfo artistInfo) {
		/* Create "queryString". */
		String queryString = "INSERT  INTO lastfm.artist_url"
				+ " ( artist_id,url) " + " VALUES (?, ?) ON DUPLICATE  KEY UPDATE url= ? ";

		insertArtistInfo(con, artistInfo, queryString);
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
	public Set<String> selectNullUrls(Connection connection, boolean doSpotifySearch) {
		Set<String> returnList = new HashSet<>();

		String queryString = doSpotifySearch ? "SELECT * FROM artist_url where url = \"\"  and  url_status = 1 or url is null limit 20" : "SELECT * FROM artist_url where url is null limit 20";
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
	public void upsertSpotify(Connection con, ArtistInfo artistInfo) {
		/* Create "queryString". */
		String queryString = "INSERT  INTO lastfm.artist_url"
				+ " ( artist_id,url,url_status) " + " VALUES (?, ?,0) ON DUPLICATE  KEY UPDATE url= ? , url_status = 0";

		insertArtistInfo(con, artistInfo, queryString);
	}

	@Override
	public UpdaterStatus getUpdaterStatus(Connection connection, String artist_id) {
		String queryString = "SELECT url,correction_status,b.correction FROM artist_url a " +
				" left join corrections b on a.artist_id = b.artist_id" +
				" where a.artist_id = ? ";


		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
			preparedStatement.setString(1, artist_id);
			ResultSet resultSet = preparedStatement.executeQuery();


			/* Get generated identifier. */

			if (resultSet.next()) {
				String url = resultSet.getString("url");
				boolean status = resultSet.getBoolean("correction_status");
				String correction = resultSet.getString("b.correction");
				return new UpdaterStatus(url, correction, status);
			}
			/* Return booking. */

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return null;
	}

	@Override
	public void insertCorrection(Connection connection, String artist, String correction) {
		String queryString = "INSERT INTO lastfm.corrections"
				+ " ( artist_id,correction) " + " VALUES (?, ?) ON DUPLICATE KEY UPDATE correction= ?";
		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

			/* Fill "preparedStatement". */
			int i = 1;
			preparedStatement.setString(i++, artist);

			preparedStatement.setString(i++, correction);
			preparedStatement.setString(i, correction);

			/* Execute query. */
			preparedStatement.executeUpdate();

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void updateStatusBit(Connection connection, String artist_id) {
		@Language("MySQL") String queryString = "Update  lastfm.artist_url set correction_status = 1 where artist_id = ?";
		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

			/* Fill "preparedStatement". */
			int i = 1;
			preparedStatement.setString(i, artist_id);
			/* Execute query. */
			preparedStatement.executeUpdate();

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
