package DAO;

import java.sql.Connection;

public interface SQLShowsDao {
	LastFMData create(Connection con, LastFMData show);

	LastFMData find(Connection con, Long showID);

	void update(Connection con, LastFMData show);

	void remove(Connection con, Long showID);

	ArtistData addArtist(Connection con, ArtistData artistData);
}
