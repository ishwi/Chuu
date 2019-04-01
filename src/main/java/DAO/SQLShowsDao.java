package DAO;

import DAO.Entities.*;

import javax.management.InstanceNotFoundException;
import java.sql.Connection;
import java.util.List;

public interface SQLShowsDao {
	LastFMData create(Connection con, LastFMData show);

	LastFMData find(Connection con, Long showID) throws InstanceNotFoundException;

	void update(Connection con, LastFMData show);

	void remove(Connection con, Long showID);

	ArtistData addArtist(Connection con, ArtistData artistData);

	UsersWrapper getLessUpdated(Connection connection);

	ResultWrapper similar(Connection connection, List<String> lastfMNames) throws InstanceNotFoundException;

	void addUrl(Connection con, ArtistData artistData);

	WrapperReturnNowPlaying knows(Connection connection, String artist, long guildId);

	void addGuild(Connection con, long userId, long guildId);

	void setUpdatedTime(Connection connection, String id);
}
