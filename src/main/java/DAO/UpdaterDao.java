package DAO;

import DAO.Entities.ArtistData;
import DAO.Entities.ArtistInfo;
import DAO.Entities.UpdaterStatus;
import DAO.Entities.UsersWrapper;

import java.sql.Connection;
import java.util.Set;

interface UpdaterDao {
	void addArtist(Connection con, ArtistData artistData);


	UsersWrapper getLessUpdated(Connection connection);

	void addUrl(Connection con, ArtistData artistData);

	void setUpdatedTime(Connection connection, String id, Integer timestamp);

	void upsertArtist(Connection con, ArtistData artistData);

	void upsertUrl(Connection con, ArtistInfo artistInfo);

	String getArtistUrl(Connection connection, String artist);

	Set<String> selectNullUrls(Connection connection, boolean spotifyNull);

	void upsertSpotify(Connection con, ArtistInfo artistInfo);

	UpdaterStatus getUpdaterStatus(Connection connection, String artist_id);

	void insertCorrection(Connection connection, String artist, String correction);
}
