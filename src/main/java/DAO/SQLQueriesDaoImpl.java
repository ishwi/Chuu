package DAO;

import DAO.Entities.*;
import org.intellij.lang.annotations.Language;

import javax.management.InstanceNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SQLQueriesDaoImpl implements SQLQueriesDao {


	@Override
	public List<CrownsLbEntry> crownsLeaderboard(Connection connection, long guildID) {
		@Language("MySQL") String queryString = "SELECT t2.lastFMID,t3.discordID,count(t2.lastFMID) ord\n" +
				"From\n" +
				"(\n" +
				"Select\n" +
				"        a.artist_id,max(a.playNumber) plays\n" +
				"    FROM\n" +
				"         artist a \n" +
				"    JOIN\n" +
				"        lastfm b \n" +
				"            ON a.lastFMID = b.lastFmId \n" +
				"    JOIN\n" +
				"        user_guild c \n" +
				"            ON b.discordID = c.discordId \n" +
				"    WHERE\n" +
				"        c.guildId = ? \n" +
				"    GROUP BY\n" +
				"        a.artist_id \n" +
				"  ) t\n" +
				"  JOIN artist t2 \n" +
				"  \n" +
				"  on t.plays = t2.playNumber and t.artist_id = t2.artist_id\n" +
				"  JOIN lastfm t3  ON t2.lastFMID = t3.lastFmId \n" +
				"    JOIN\n" +
				"        user_guild t4 \n" +
				"            ON t3.discordID = t4.discordId \n" +
				"    WHERE\n" +
				"        t4.guildId = ? \n" +
				"  group by t2.lastFMID,t3.discordID\n" +
				"  order by ord desc";

		List<CrownsLbEntry> returnedList = new ArrayList<>();
		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
			int i = 1;
			preparedStatement.setLong(i++, guildID);
			preparedStatement.setLong(i, guildID);

			ResultSet resultSet = preparedStatement.executeQuery();


			while (resultSet.next()) { //&& (j < 10 && j < rows)) {
				String lastFMId = resultSet.getString("lastFMID");
				long discordId = resultSet.getLong("discordID");
				int crowns = resultSet.getInt("ord");
				returnedList.add(new CrownsLbEntry(lastFMId, discordId, crowns));

			}
			return returnedList;

		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException((e));
		}


	}

	@Override
	public UniqueWrapper<UniqueData> getCrowns(Connection connection, String lastFmId, long guildID) {
		List<UniqueData> returnList = new ArrayList<>();
		long discordID;

		@Language("MariaDB") String queryString = "SELECT artist_id, b.discordID , playNumber as orden" +
				" FROM  artist  a" +
				" join lastfm b on a.lastFMID = b.lastFmId" +
				" where  a.lastFMID = ?" +
				" and playNumber > 0" +
				" AND  playNumber >= all" +
				"       (Select max(b.playNumber) " +
				" from " +
				"(Select in_A.lastFMID,in_A.artist_id,in_A.playNumber" +
				" from artist in_A  " +
				" join " +
				" lastfm in_B" +
				" on in_A.lastFMID = in_B.lastFmid" +
				" natural join " +
				" user_guild in_C" +
				" where guildId = ?" +
				"   ) as b" +
				" where b.artist_id = a.artist_id" +
				" group by artist_id)" +
				" order by orden DESC";


		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
			int i = 1;
			preparedStatement.setString(i++, lastFmId);
			preparedStatement.setLong(i, guildID);

			ResultSet resultSet = preparedStatement.executeQuery();
			int j = 0;

			if (!resultSet.next()) {
				return new UniqueWrapper<>(0, 0, lastFmId, returnList);

			} else {
				discordID = resultSet.getLong("b.discordID");
				resultSet.beforeFirst();
			}

			while (resultSet.next()) {

				String artist = resultSet.getString("artist_id");
				int plays = resultSet.getInt("orden");
				returnList.add(new UniqueData(artist, plays));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return new UniqueWrapper<>(returnList.size(), discordID, lastFmId, returnList);
	}

	@Override
	public List<UrlCapsule> getGuildTop(Connection connection, Long guildID) {
		String queryString = "SELECT a.artist_id, sum(playNumber) as orden ,url  FROM  artist a" +
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

				UrlCapsule capsule = new UrlCapsule(url, count++, url, artist, "");
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
			preparedStatement.setString(i, lastFmId);
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
			while (resultSet.next()) { //&& (j < 10 && j < rows)) {
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
			preparedStatement.setString(i, userB);


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

			return new ResultWrapper(rows, returnList);

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

	}


	@Override
	public WrapperReturnNowPlaying knows(Connection con, String artist, long guildId, int limit) {

		String queryString = "Select temp.artist_id, temp.lastFMID,temp.playNumber,b.url, c.discordID " +
				"FROM (SELECT a.artist_id, a.lastFMID, a.playNumber " +
				"FROM  artist a  " +
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
			while (resultSet.next() && (j < limit && j < rows)) {
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
	public int userPlays(Connection con, String artist, String whom) {
		String queryString = "Select a.playNumber " +
				"FROM artist a JOIN lastFM b on a.lastFMID=b.lastFmId " +
				"JOIN artist_url c on a.artist_id = c.artist_id " +
				"where a.lastFMID = ? and a.artist_id =?";
		try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {
			/* Fill "preparedStatement". */
			int i = 1;
			preparedStatement.setString(i++, whom);
			preparedStatement.setString(i, artist);




			/* Execute query. */
			ResultSet resultSet = preparedStatement.executeQuery();
			if (!resultSet.next())
				return 0;
			return resultSet.getInt("a.playNumber");


		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}


