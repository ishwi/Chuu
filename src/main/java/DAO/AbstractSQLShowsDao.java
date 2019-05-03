package DAO;

import DAO.Entities.*;
import org.intellij.lang.annotations.Language;

import javax.management.InstanceNotFoundException;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * SELECT *
 * FROM  artist a
 * where 	 lastFMID="nonparadisi"
 * and playNumber > 0
 * AND  playNumber >= all (Select max(b.playNumber)
 * from
 * (Select in_A.lastFMID,in_A.artist_id,in_A.playNumber
 * from artist in_A
 * join
 * lastfm in_B
 * on in_A.lastFMID = in_B.lastFmid
 * join
 * user_guild in_C
 * on in_b.discordID = in_C.discordId
 * <p>
 * where guildId = 476779889102684160
 * ) as b
 * where b.artist_id = a.artist_id
 * group by artist_id
 * )
 *
 * @author Miguel
 */
public abstract class AbstractSQLShowsDao implements SQLShowsDao {
	@Override
	public List<UniqueData> getCrowns(Connection connection, String lastFmId, long guildID) {
		List<UniqueData> returnList = new ArrayList<>();
		String queryString = "SELECT artist_id, playNumber as orden" +
				" FROM  artist  a" +
				" where  lastFMID=?" +
				" and playNumber > 0" +
				" AND  playNumber >= all" +
				"       (Select max(b.playNumber) " +
				" from " +
				"(Select in_A.lastFMID,in_A.artist_id,in_A.playNumber" +
				" from artist in_A  " +
				" join " +
				" lastfm in_B" +
				" on in_A.lastFMID = in_B.lastFmid" +
				" join " +
				" user_guild in_C" +
				" on in_b.discordID = in_C.discordId" +
				" where guildId = ?" +
				"   ) as b" +
				" where b.artist_id = a.artist_id" +
				" group by artist_id)" +
				" order by orden DESC";
		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
			int i = 1;
			preparedStatement.setString(i++, lastFmId);
			preparedStatement.setLong(i++, guildID);

			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				String artist = resultSet.getString("artist_id");
				int plays = resultSet.getInt("orden");
				returnList.add(new UniqueData(artist, plays));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return returnList;
	}

	@Override
	public List<UrlCapsule> getGuildTop(Connection connection, Long guildID) {
		String queryString = "SELECT a.artist_id, sum(playNumber) as orden ,url  FROM lastfm.artist a" +
				" JOIN lastfm b" +
				" ON a.lastFMID = b.lastFmId" +
				" JOIN artist_url d " +
				" ON a.artist_id = d.artist_id" +
				" JOIN  user_guild c" +
				" On b.discordID=c.discordId" +
				" Where c.guildId = ?" +
				" group by artist_id,url" +
				" order BY orden DESC" +
				" LIMIT 25;";
		List<UrlCapsule> list = new LinkedList<>();

		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
			preparedStatement.setLong(1, guildID);

			ResultSet resultSet = preparedStatement.executeQuery();
			int count = 0;
			while (resultSet.next()) {
				String artist = resultSet.getString("a.artist_id");
				String url = resultSet.getString("url");

				int plays = resultSet.getInt("orden");

				UrlCapsule capsule = new UrlCapsule(url, count++, url, artist);
				capsule.setPlays(plays);
				list.add(capsule);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	@Override
	public UniqueWrapper<UniqueData> getUniqueArtist(Connection connection, Long guildID, String lastFmId) {
		@Language("MySQL") String queryString = "SELECT * " +
				"FROM(  " +
				"       select artist_id, playNumber, a.lastFMID ,b.discordID" +
				"       from artist a join lastfm b " +
				"       ON a.lastFMID = b.lastFmId " +
				"       JOIN user_guild c ON b.discordID = c.discordId " +
				"       where c.guildId = ? and a.playNumber > 2 " +
				"       group by a.artist_id " +
				"       having count( *) = 1) temp " +
				"Where temp.lastFMID = ? and temp.playNumber > 1 " +
				" order by temp.playNumber desc ";


		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
			int i = 1;
			preparedStatement.setLong(i++, guildID);
			preparedStatement.setString(i++, lastFmId);
			ResultSet resultSet = preparedStatement.executeQuery();

			if (!resultSet.next()) {
				return new UniqueWrapper<>(0, 0, lastFmId, new ArrayList<>());
			}

			List<UniqueData> returnList = new ArrayList<>();
			resultSet.last();
			int rows = resultSet.getRow();
			long discordId = resultSet.getLong("temp.discordID");


			resultSet.beforeFirst();
			/* Get results. */


			int j = 0;
			while (resultSet.next() && (j < 10 && j < rows)) {
				j++;
				String name = resultSet.getString("temp.artist_id");
				int count_a = resultSet.getInt("temp.playNumber");

				returnList.add(new UniqueData(name, count_a));

			}
			return new UniqueWrapper<>(rows, discordId, lastFmId, returnList);


		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public UsersWrapper getLessUpdated(Connection connection) {
		@Language("MySQL") String queryString = "Select a.discordID, a.lastFmId,b.last_update FROM lastfm a LEFT JOIN updated b on a.lastFmId=b.discordID  order by  last_update asc LIMIT 1";
		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

			/* Fill "preparedStatement". */

			/* Execute query. */
			ResultSet resultSet = preparedStatement.executeQuery();
			List<UsersWrapper> returnList = new ArrayList<>();
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

		@Language("MySQL") String queryString = "SELECT a.artist_id ,a.lastFMID , a.playNumber, b.lastFMID,b.playNumber ,((a.playNumber + b.playNumber)/(abs(a.playNumber-b.playNumber)+1)* ((a.playNumber + b.playNumber))*2.5) media , c.url " +
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
	public List<Long> guildList(Connection connection, long userId) {
		@Language("MySQL") String queryString = "Select discordId,guildId  FROM user_guild  WHERE discordID = ?";

		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

			/* Fill "preparedStatement". */
			int i = 1;
			preparedStatement.setLong(i++, userId);

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
	public void update(Connection connection, LastFMData lastFMData) {

		/* Create "queryString". */
		String queryString = "UPDATE lastfm SET lastFmId= ? WHERE discordID = ?";

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
		@Language("MySQL") String queryString = "DELETE FROM lastfm.lastfm WHERE" + " discordID = ?";

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
