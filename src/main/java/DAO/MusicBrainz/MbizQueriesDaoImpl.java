package DAO.MusicBrainz;

import DAO.Entities.AlbumInfo;
import DAO.Entities.Genre;
import org.intellij.lang.annotations.Language;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MbizQueriesDaoImpl implements MbizQueriesDao {
	@Override
	public List<AlbumInfo> getYearAlbums(Connection con, List<AlbumInfo> albumInfos, Year year) {
		List<AlbumInfo> returnList = new ArrayList<>();
		long discordID;

		@Language("MySQL") String queryString = "SELECT \n" +
				"a.name,a.gid,b.name\n" +
				"FROM\n" +
				"    mbiz.release a\n" +
				"     join mbiz.artist_credit b ON a.artist_credit = b.id\n" +
				"       JOIN\n" +
				"    mbiz.release_group c ON a.release_group = c.id\n" +
				"        JOIN\n" +
				"    mbiz.release_group_meta d ON c.id = d.id" +
				" Where d.first_release_date_year = ? and " +
				"    a.gid in (";
		for (AlbumInfo albumInfo : albumInfos) {
			queryString += " ? ,";
		}
		queryString = queryString.substring(0, queryString.length() - 1) + ")";

		try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {
			int i = 1;
			preparedStatement.setInt(i++, year.get(ChronoField.YEAR));

			for (AlbumInfo albumInfo : albumInfos) {
				preparedStatement.setString(i++, albumInfo.getMbid());
			}
			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {

				String mbid = resultSet.getString("a.gid");
				String artist = resultSet.getString("a.name");
				String albumName = resultSet.getString("b.name");
				AlbumInfo ai = new AlbumInfo(mbid, albumName, artist);
				returnList.add(ai);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return returnList;
	}

	@Override
	public List<AlbumInfo> getYearAlbumsByReleaseName(Connection con, List<AlbumInfo> releaseInfo, Year year) {
		@Language("MySQL") String queryString = "SELECT DISTINCT\n" +
				"    (a.name), b.name,  d.first_release_date_year\n" +
				"FROM\n" +
				"    mbiz.artist_credit a\n" +
				"        JOIN\n" +
				"    mbiz.`release` b ON a.id = b.artist_credit\n" +
				"        JOIN\n" +
				"    mbiz.release_group c ON b.release_group = c.id\n" +
				"        JOIN\n" +
				"    mbiz.release_group_meta d ON c.id = d.id ";
		String whereSentence = "";
		StringBuilder artistWhere = new StringBuilder("where a.name in (");
		StringBuilder albumWhere = new StringBuilder("and b.name in (");
		for (AlbumInfo ignored : releaseInfo) {
			artistWhere.append(" ? ,");
			albumWhere.append(" ? ,");
		}
		whereSentence = artistWhere.toString().substring(0, artistWhere.length() - 1) + ") ";
		whereSentence += albumWhere.toString().substring(0, albumWhere.length() - 1) + ") ";
		whereSentence += "and d.first_release_date_year = ?";

		List<AlbumInfo> returnList = new ArrayList<>();
		try (PreparedStatement preparedStatement = con.prepareStatement(queryString + whereSentence)) {
			int i = 1;

			for (AlbumInfo albumInfo : releaseInfo) {

				preparedStatement.setString(i, albumInfo.getArtist());
				preparedStatement.setString(i + releaseInfo.size(), albumInfo.getName());
				i++;
			}

			preparedStatement.setInt(1 + releaseInfo.size() * 2, year.get(ChronoField.YEAR));

			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {

				String artist = resultSet.getString("a.name");
				String album = resultSet.getString("b.name");

				AlbumInfo ai = new AlbumInfo("", album, artist);
				returnList.add(ai);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return returnList;
	}

	@Override
	public Map<Genre, Integer> genreCount(Connection con, List<AlbumInfo> releaseInfo) {
		Map<Genre, Integer> returnMap = new HashMap<>();
		@Language("MySQL") String queryString = "SELECT \n" +
				"       d.name, count(*) as count, max(b.name) as representative\n \n" +
				"FROM\n" +
				"    mbiz.release a\n" +
				"        JOIN\n" +
				"    mbiz.release_group b ON a.release_group = b.id\n" +
				"        JOIN\n" +
				"    mbiz.release_group_tag c ON b.id = c.release_group\n" +
				"        JOIN\n" +
				"    mbiz.tag d ON c.tag = d.id\n" +
				"WHERE\n" +
				"    a.gid in (";

		for (AlbumInfo ignored : releaseInfo) {
			queryString += " ? ,";
		}

		queryString = queryString.substring(0, queryString.length() - 1) + ")";
		queryString += "\n Group by d.name";

		try (PreparedStatement preparedStatement = con.prepareStatement(queryString)) {
			int i = 1;

			for (AlbumInfo albumInfo : releaseInfo) {
				preparedStatement.setString(i++, albumInfo.getMbid());
			}
			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {

				String mbid = resultSet.getString("d.name");
				int count = resultSet.getInt("count");
				String representative = resultSet.getString("representative");

				returnMap.put(new Genre(mbid, representative), count);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return returnMap;

	}

}
