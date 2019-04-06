package DAO;

import DAO.Entities.LastFMData;
import DAO.Entities.ResultWrapper;
import DAO.Entities.Results;
import DAO.Entities.UsersWrapper;

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
	public UsersWrapper getLessUpdated(Connection connection) {
		String queryString = "Select a.discordID, a.lastFmId FROM lastfm a LEFT JOIN updated b on a.lastFmId=b.discordID" +
				"  order by  last_update asc LIMIT 1";
		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

			/* Fill "preparedStatement". */

			/* Execute query. */
			ResultSet resultSet = preparedStatement.executeQuery();
			List<UsersWrapper> returnList = new ArrayList<>();
			if (resultSet.next()) {

				String name = resultSet.getString("a.lastFmId");
				long discordID = resultSet.getLong("a.discordID");
				return new UsersWrapper(discordID, name);
			}
			/* Return show. */

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	@Override
	public List<UsersWrapper> getAll(Connection connection, long guildID) {
		String queryString = "Select a.discordID, a.lastFmId FROM lastfm a join (Select discordId from user_guild where guildId = ? ) b on a.discordID = b.discordId";
		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

			/* Fill "preparedStatement". */

			/* Execute query. */
			preparedStatement.setLong(1, guildID);
			ResultSet resultSet = preparedStatement.executeQuery();
			List<UsersWrapper> returnList = new ArrayList<>();
			while (resultSet.next()) {

				String name = resultSet.getString("a.lastFmId");
				long discordID = resultSet.getLong("a.discordID");
				returnList.add(new UsersWrapper(discordID, name));
			}
			/* Return show. */
			return returnList;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public ResultWrapper similar(Connection connection, List<String> lastfMNames) throws InstanceNotFoundException {
		int MAX_IN_DISPLAY = 10;
		String userA = lastfMNames.get(0);
		String userB = lastfMNames.get(1);

		String queryString = "SELECT a.artist_id ,a.lastFMID , a.playNumber, b.lastFMID,b.playNumber ,((a.playNumber + b.playNumber)/(abs(a.playNumber-b.playNumber)+1)* ((a.playNumber + b.playNumber))*2.5) media , c.url " +
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
			while (resultSet.next() && (j < MAX_IN_DISPLAY && j < rows)) {
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
