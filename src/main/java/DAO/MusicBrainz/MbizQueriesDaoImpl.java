package DAO.MusicBrainz;

import DAO.Entities.AlbumInfo;
import org.intellij.lang.annotations.Language;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

public class MbizQueriesDaoImpl implements MbizQueriesDao {
	@Override
	public List<AlbumInfo> getYearAlbums(Connection con, List<AlbumInfo> albumInfos, Year year) {
		List<AlbumInfo> returnList = new ArrayList<>();
		long discordID;

		@Language("MySQL") String queryString = "SELECT \n" +
				"    *\n" +
				"FROM\n" +
				"    mbiz.release a\n" +
				"        JOIN\n" +
				"    mbiz.release_group b ON a.release_group = b.id\n" +
				"    join mbiz.release_group_meta c\n" +
				"    on b.id=c.id\n" +
				"WHERE\n" +
				"c.first_release_date_year = ? and " +
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
				AlbumInfo ai = new AlbumInfo(mbid);
				ai.setName(artist);
				returnList.add(ai);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return returnList;
	}

}
