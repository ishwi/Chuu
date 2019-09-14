package dao.musicbrainz;

import dao.entities.*;

import java.sql.Connection;
import java.time.Year;
import java.util.List;
import java.util.Map;

interface MbizQueriesDao {
	List<AlbumInfo> getYearAlbums(Connection con, List<AlbumInfo> mbizList, Year year);

	List<AlbumInfo> getYearAlbumsByReleaseName(Connection con, List<AlbumInfo> releaseInfo, Year year);

	Map<Genre, Integer> genreCount(Connection connection, List<AlbumInfo> releaseInfo);

	Map<Country, Integer> countryCount(Connection connection, List<ArtistInfo> releaseInfo);

	List<Track> getAlbumTrackList(Connection connection, String artist, String album);

	List<Track> getAlbumTrackListLower(Connection connection, String artist, String album);
}
