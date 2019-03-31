package DAO;

import DAO.Entities.ArtistData;
import DAO.Entities.LastFMData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Jdbc3CcSQLShowsDao extends AbstractSQLShowsDao {

	@Override
	public LastFMData create(Connection con, LastFMData lastFMData) {
		/* Create "queryString". */
		String queryString = "INSERT INTO lastfm.lastfm"
				+ " (lastFmId, discordID) " +  " VALUES (?, ?) ON DUPLICATE KEY UPDATE lastFmId=" + "?" ;

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
	public void knows(Connection connection, String artist) {
		String queryString = "SELECT username, count " +
				"FROM ";
	}

	@Override
	public ArtistData addArtist(Connection con, ArtistData artistData) {
		/* Create "queryString". */
		String queryString = "INSERT INTO lastfm.artist"
				+ " ( lastFMID,artist_id,playNumber) " + " VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE playNumber=" + "?";

		try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {

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
			return artistData;

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
