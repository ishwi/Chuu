package DAO;

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

}
