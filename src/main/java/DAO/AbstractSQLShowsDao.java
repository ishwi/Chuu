package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * @author Miguel
 *
 */
public abstract class AbstractSQLShowsDao implements SQLShowsDao {

	@Override
	public LastFMData find(Connection connection, Long discordID)  {

		/* Create "queryString". */
		String queryString = "SELECT discordID, lastFmid FROM lastfm WHERE discordID = ?";

		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

			/* Fill "preparedStatement". */
			int i = 1;
			preparedStatement.setLong(i, discordID);


			/* Execute query. */
			ResultSet resultSet = preparedStatement.executeQuery();

			if (!resultSet.next()) {
				return null;
			}

			/* Get results. */
			i = 1;
			long resDiscordID = resultSet.getLong(i++);
			String lastFmID = resultSet.getString(i);

			/* Return show. */

			return new LastFMData(lastFmID, resDiscordID);

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}



	@Override
	public void update(Connection connection, LastFMData lastFMData)  {

		/* Create "queryString". */
		String queryString = "UPDATE lastfm"
				+ " SET lastFmId= ? WHERE discordID = ?";

		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

			/* Fill "preparedStatement". */
			int i = 1;
			preparedStatement.setString(i++, lastFMData.getName());

			preparedStatement.setLong(i, lastFMData.getShowID());

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
	public void remove(Connection connection, Long showID) {

		/* Create "queryString". */
		String queryString = "DELETE FROM lastfm.lastfm WHERE" + " discordID = ?";

		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

			/* Fill "preparedStatement". */
			int i = 1;
			preparedStatement.setLong(i, showID);

			/* Execute query. */
			int removedRows = preparedStatement.executeUpdate();

			if (removedRows == 0) {
				throw new RuntimeException("A");
			}

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

}
