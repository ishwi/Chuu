package DAO;

import DAO.Entities.ArtistData;
import DAO.Entities.LastFMData;
import DAO.Entities.ResultWrapper;

import javax.management.InstanceNotFoundException;
import java.sql.Connection;
import java.util.List;

public interface SQLShowsDao {
	LastFMData create(Connection con, LastFMData show);

	LastFMData find(Connection con, Long showID) throws InstanceNotFoundException;

	void update(Connection con, LastFMData show);

	void remove(Connection con, Long showID);

	ArtistData addArtist(Connection con, ArtistData artistData);

	ResultWrapper similar(Connection connection, List<String> lastfMNames) throws InstanceNotFoundException;

	void addUrl(Connection con, ArtistData artistData);

	void knows(Connection connection, String artist);
}
