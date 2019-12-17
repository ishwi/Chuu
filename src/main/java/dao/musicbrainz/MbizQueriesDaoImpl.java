package dao.musicbrainz;

import dao.entities.*;
import core.Chuu;

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

		StringBuilder queryString = new StringBuilder("SELECT \n" +
				"a.name as albumname,a.gid as mbid,b.name artistName\n" +
				"FROM\n" +
				"    musicbrainz.release a\n" +
				"     join musicbrainz.artist_credit b ON a.artist_credit = b.id\n" +
				"       JOIN\n" +
				"    musicbrainz.release_group c ON a.release_group = c.id\n" +
				"        JOIN\n" +
				"    musicbrainz.release_group_meta d ON c.id = d.id" +
				" Where d.first_release_date_year = ? and " +
				"    a.gid in (");
		for (AlbumInfo albumInfo : albumInfos) {
			queryString.append(" ? ,");
		}
		queryString = new StringBuilder(queryString.substring(0, queryString.length() - 1) + ")");

		try (PreparedStatement preparedStatement = con.prepareStatement(queryString.toString())) {
			int i = 1;
			preparedStatement.setInt(i++, year.get(ChronoField.YEAR));

			for (AlbumInfo albumInfo : albumInfos) {
				preparedStatement.setObject(i++, java.util.UUID.fromString(albumInfo.getMbid()));
			}
			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {

				String mbid = resultSet.getString("mbid");
				String artist = resultSet.getString("artistName");
				String albumName = resultSet.getString("albumname");
				AlbumInfo ai = new AlbumInfo(mbid, albumName, artist);
				returnList.add(ai);
			}
		} catch (SQLException e) {
			Chuu.getLogger().warn(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return returnList;
	}

	@Override
	public List<AlbumInfo> getYearAlbumsByReleaseName(Connection con, List<AlbumInfo> releaseInfo, Year year) {
		String queryString = "SELECT DISTINCT\n" +
				"    (a.name) as artistname, b.name as albumname,  d.first_release_date_year as year \n" +
				"FROM\n" +
				"    musicbrainz.artist_credit a\n" +
				"        JOIN\n" +
				"    musicbrainz.release b ON a.id = b.artist_credit\n" +
				"        JOIN\n" +
				"    musicbrainz.release_group c ON b.release_group = c.id\n" +
				"        JOIN\n" +
				"    musicbrainz.release_group_meta d ON c.id = d.id ";
		String whereSentence;
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

				preparedStatement.setString(i, albumInfo.getName());
				preparedStatement.setString(i + releaseInfo.size(), albumInfo.getName());
				i++;
			}

			preparedStatement.setInt(1 + releaseInfo.size() * 2, year.get(ChronoField.YEAR));

			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {

				String artist = resultSet.getString("artistname");
				String album = resultSet.getString("albumname");

				AlbumInfo ai = new AlbumInfo("", album, artist);
				returnList.add(ai);
			}
		} catch (SQLException e) {
			Chuu.getLogger().warn(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return returnList;
	}

	@Override
	public Map<Genre, Integer> genreCount(Connection con, List<AlbumInfo> releaseInfo) {
		Map<Genre, Integer> returnMap = new HashMap<>();
		StringBuilder queryString = new StringBuilder("SELECT \n" +
				"       c.name as neim, count(*) as count\n \n" +
				" FROM\n" +
				" musicbrainz.release d join \n" +
				" musicbrainz.release_group a " +
				" on d.release_group = a.id " +
				"        JOIN\n" +
				"    musicbrainz.release_group_tag b ON a.id = b.release_group\n" +
				"        JOIN\n" +
				"    musicbrainz.tag c ON b.tag = c.id\n" +
				"WHERE\n" +
				"    d.gid in (");

		for (AlbumInfo ignored : releaseInfo) {
			queryString.append(" ? ,");
		}

		queryString = new StringBuilder(queryString.substring(0, queryString.length() - 1) + ")");
		queryString.append("\n Group by c.name");

		try (PreparedStatement preparedStatement = con.prepareStatement(queryString.toString())) {
			int i = 1;

			for (AlbumInfo albumInfo : releaseInfo) {
				preparedStatement.setObject(i++, java.util.UUID.fromString(albumInfo.getMbid()));
			}
			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {

				String mbid = resultSet.getString("neim");
				int count = resultSet.getInt("count");

				returnMap.put(new Genre(mbid, ""), count);
			}
		} catch (SQLException e) {
			Chuu.getLogger().warn(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return returnMap;

	}

	@Override
	public Map<Country, Integer> countryCount(Connection connection, List<ArtistInfo> releaseInfo) {
		Map<Country, Integer> returnMap = new HashMap<>();
		StringBuilder queryString = new StringBuilder("SELECT \n" +
				"       c.code as code, b.name as neim, count(*) as count\n \n" +
				" FROM\n" +
				" musicbrainz.artist a join \n" +
				" musicbrainz.area b" +
				" join musicbrainz.iso_3166_1 c  on b.id=c.area " +
				" on a.area = b.id" +
				"  WHERE b.type = 1" +
				"	 and " +
				"    a.gid in (");

		for (ArtistInfo ignored : releaseInfo) {
			queryString.append(" ? ,");
		}

		queryString = new StringBuilder(queryString.substring(0, queryString.length() - 1) + ")");
		queryString.append(" \n GROUP BY  b.name,c.code");

		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString.toString())) {
			int i = 1;

			for (ArtistInfo albumInfo : releaseInfo) {
				preparedStatement.setObject(i++, java.util.UUID.fromString(albumInfo.getMbid()));
			}
			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {

				String coutryName = resultSet.getString("neim");
				String code = resultSet.getString("code");

				int frequency = resultSet.getInt("count");

				returnMap.put(new Country(coutryName, code), frequency);
			}
		} catch (SQLException e) {
			Chuu.getLogger().warn(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return returnMap;

	}

	@Override
	public List<Track> getAlbumTrackList(Connection connection, String artist, String album) {
		List<Track> returnList = new ArrayList<>();

		String queryString = "SELECT distinct e.name , e.position\n" +
				"FROM \n" +
				"musicbrainz.artist_credit a\n" +
				"JOIN\n" +
				"musicbrainz.release b ON a.id = b.artist_credit\n" +
				"JOIN\n" +
				"musicbrainz.release_group c ON b.release_group = c.id\n" +
				"join \n" +
				"musicbrainz.medium d on b.id = d.release\n" +
				"join musicbrainz.track e on e.medium = d.id\n" +
				"where a.name = ? and b.name = ?\n" +
				"order by e.position;";

		return processTracks(connection, artist, album, returnList, queryString);
	}

	//Added these indexes in order to make it a little faster
	//CREATE INDEX idx_release_group_name_lower ON musicbrainz.release_group ((lower(name)))
	//CREATE INDEX idx_artist_name_lower ON musicbrainz.artist_credit ((lower(name)))
	@Override
	public List<Track> getAlbumTrackListLower(Connection connection, String artist, String album) {
		List<Track> returnList = new ArrayList<>();

		String queryString = "SELECT distinct lower(e.name) , e.name as name  , e.position\n" +
				"FROM \n" +
				"musicbrainz.artist_credit a\n" +
				"JOIN\n" +
				"musicbrainz.release b ON a.id = b.artist_credit\n" +
				"JOIN\n" +
				"musicbrainz.release_group c ON b.release_group = c.id\n" +
				"join \n" +
				"musicbrainz.medium d on b.id = d.release\n" +
				"join musicbrainz.track e on e.medium = d.id\n" +
				"where lower(a.name) = lower(?) and lower(b.name) = lower(?)\n" +
				"order by e.position;";

		return processTracks(connection, artist, album, returnList, queryString);
	}

	private List<Track> processTracks(Connection connection, String artist, String album, List<Track> returnList, String queryString) {
		try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {
			int i = 1;

			preparedStatement.setString(i++, artist);
			preparedStatement.setString(i, album);

			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				String trackName = resultSet.getString("name");
				int position = resultSet.getInt("position");

				Track t = new Track(artist, trackName, 0, false, 0);
				t.setPosition(position);
				returnList.add(t);
			}

		} catch (SQLException e) {
			Chuu.getLogger().warn(e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return returnList;

	}
}
