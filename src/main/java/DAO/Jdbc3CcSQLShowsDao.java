package DAO;

import DAO.Entities.ArtistData;
import DAO.Entities.LastFMData;
import DAO.Entities.ReturnNowPlaying;
import DAO.Entities.WrapperReturnNowPlaying;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
			preparedStatement.setString(i++, lastFMData.getName());


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
	public void addUrl(Connection con, ArtistData artistData) {
		/* Create "queryString". */
		String queryString = "INSERT IGNORE INTO lastfm.artist_url"
				+ " ( artist_id,url) " + " VALUES (?, ?) ";

		try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

			/* Fill "preparedStatement". */
			int i = 1;
			preparedStatement.setString(i++, artistData.getArtist());
			preparedStatement.setString(i++, artistData.getUrl());


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
			preparedStatement.setLong(i++, guildId);


			/* Execute query. */
			preparedStatement.executeUpdate();

			/* Get generated identifier. */

			/* Return booking. */

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void setUpdatedTime(Connection connection, String id) {
		String queryString = "INSERT INTO lastfm.updated"
				+ " ( discordID,last_update) " + " VALUES (?, ?) ON DUPLICATE KEY UPDATE last_update=" + "?";
		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

			/* Fill "preparedStatement". */
			int i = 1;
			preparedStatement.setString(i++, id);
			preparedStatement.setTimestamp(i++, Timestamp.from(Instant.now()));
			preparedStatement.setTimestamp(i++, Timestamp.from(Instant.now()));


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
	public void addArtist(Connection con, ArtistData artistData) {
		/* Create "queryString". */
		String queryString = "INSERT INTO lastfm.artist"
				+ " ( lastFMID,artist_id,playNumber) " + " VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE playNumber=" + "?";

		try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {
			System.out.println(artistData.getArtist() + "\n");
			/* Fill "preparedStatement". */
			int i = 1;
			preparedStatement.setString(i++, artistData.getDiscordID());
			preparedStatement.setString(i++, artistData.getArtist());
			preparedStatement.setLong(i++, artistData.getCount());
			preparedStatement.setLong(i++, artistData.getCount());


			/* Execute query. */
			preparedStatement.executeUpdate();

			/* Get generated identifier. */

			/* Return booking. */

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}


