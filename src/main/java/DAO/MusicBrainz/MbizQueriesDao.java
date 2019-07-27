package DAO.MusicBrainz;

import DAO.Entities.AlbumInfo;
import DAO.Entities.ArtistInfo;
import DAO.Entities.Country;
import DAO.Entities.Genre;

import java.sql.Connection;
import java.time.Year;
import java.util.List;
import java.util.Map;

public interface MbizQueriesDao {
	List<AlbumInfo> getYearAlbums(Connection con, List<AlbumInfo> mbizList, Year year);

	List<AlbumInfo> getYearAlbumsByReleaseName(Connection con, List<AlbumInfo> releaseInfo, Year year);

	Map<Genre, Integer> genreCount(Connection connection, List<AlbumInfo> releaseInfo);

	Map<Country, Integer> countryCount(Connection connection, List<ArtistInfo> releaseInfo);

}
