package DAO.MusicBrainz;

import DAO.Entities.AlbumInfo;

import java.sql.Connection;
import java.time.Year;
import java.util.List;

public interface MbizQueriesDao {
	List<AlbumInfo> getYearAlbums(Connection con, List<AlbumInfo> mbizList, Year year);
}
