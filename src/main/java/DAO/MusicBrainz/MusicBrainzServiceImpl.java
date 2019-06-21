package DAO.MusicBrainz;

import DAO.Entities.AlbumInfo;
import DAO.Entities.AlbumUserPlays;
import DAO.Entities.ArtistData;
import DAO.SimpleDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Year;
import java.util.List;

public class MusicBrainzServiceImpl implements MusicBrainzService {
	private final DataSource dataSource;
	private final MbizQueriesDao mbizQueriesDao;

	public MusicBrainzServiceImpl() {
		this.dataSource = new SimpleDataSource(false);
		mbizQueriesDao = new MbizQueriesDaoImpl();
	}


	@Override
	public List<AlbumInfo> listOfYearReleases(List<AlbumInfo> mbiz, Year year) {
		try (Connection connection = dataSource.getConnection()) {
			return mbizQueriesDao.getYearAlbums(connection, mbiz, year);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<AlbumInfo> listOfCurrentYear(List<AlbumInfo> mbiz) {
		return this.listOfYearReleases(mbiz, Year.now());
	}

	@Override
	public List<ArtistData> findArtistByName(String name) {
		return null;
	}

	@Override
	public boolean isReleaseFromCurrentYear(AlbumUserPlays albumUserPlays) {
		return false;
	}

	@Override
	public List<AlbumUserPlays> gindReleaseByMbiz(String mbiz) {
		return null;
	}

	@Override
	public List<AlbumUserPlays> findReleaseByName(String name) {
		return null;
	}
}
