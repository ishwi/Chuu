package DAO;

import main.ResultWrapper;
import main.Results;

import javax.management.InstanceNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Miguel
 */
public abstract class AbstractSQLShowsDao implements SQLShowsDao {

	@Override
	public ResultWrapper similar(Connection connection, List<String> lastfMNames) throws InstanceNotFoundException {

		String userA = lastfMNames.get(0);
		String userB = lastfMNames.get(1);

		String queryString = "SELECT a.artist_id ,a.lastFMID , a.playNumber, b.lastFMID,b.playNumber , abs(b.playNumber - a.playNumber) /2 media , c.url " +
				"FROM " +
				"(SELECT * " +
				"FROM artist " +
				"WHERE lastFMID = ? ) a " +
				"JOIN " +
				"(SELECT * " +
				"FRom artist " +
				"where  lastFMID = ? ) b " +
				"ON a.artist_id=b.artist_id " +
				"JOIN artist_url c " +
				"on c.artist_id=b.artist_id" +
				" order by media desc ";


		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

			/* Fill "preparedStatement". */
			int i = 1;
			preparedStatement.setString(i++, userA);
			preparedStatement.setString(i++, userB);


			/* Execute query. */
			ResultSet resultSet = preparedStatement.executeQuery();

			if (!resultSet.next()) {
				throw new InstanceNotFoundException("Not found ");
			}
			List<Results> returnList = new ArrayList<>();
			resultSet.last();
			int rows = resultSet.getRow();
			resultSet.beforeFirst();
			/* Get results. */
			int j = 0;
			while (resultSet.next() && (j < 40 && j < rows)) {
				j++;
				String name = resultSet.getString("a.artist_id");
				int count_a = resultSet.getInt("a.playNumber");
				int count_b = resultSet.getInt("b.playNumber");
				String url = resultSet.getString("c.url");
				returnList.add(new Results(count_a, count_b, name, userA, userB, url));

			}
			/* Return show. */

			return new ResultWrapper(rows, returnList);

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public LastFMData find(Connection connection, Long discordID) throws InstanceNotFoundException {

		/* Create "queryString". */
		String queryString = "SELECT discordID, lastFmid FROM lastfm WHERE discordID = ?";

		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

			/* Fill "preparedStatement". */
			int i = 1;
			preparedStatement.setLong(i, discordID);


			/* Execute query. */
			ResultSet resultSet = preparedStatement.executeQuery();

			if (!resultSet.next()) {
				throw new InstanceNotFoundException("Not found ");
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
	public void update(Connection connection, LastFMData lastFMData) {

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
