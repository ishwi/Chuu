package DAO;

import DAO.Entities.*;

import javax.management.InstanceNotFoundException;
import java.sql.Connection;
import java.util.List;

interface SQLShowsDao {
	LastFMData create(Connection con, LastFMData show);

	LastFMData find(Connection con, Long showID) throws InstanceNotFoundException;

	List<Long> guildList(Connection connection, long userId);

	void update(Connection con, LastFMData show);

	void remove(Connection con, Long showID);

	void addArtist(Connection con, ArtistData artistData);

	UniqueWrapper<UniqueData> getUniqueArtist(Connection connection, Long guildID, String lastFMID);

	UsersWrapper getLessUpdated(Connection connection);

	List<UsersWrapper> getAll(Connection connection, long guildId);

	ResultWrapper similar(Connection connection, List<String> lastfMNames) throws InstanceNotFoundException;

	void addUrl(Connection con, ArtistData artistData);

	WrapperReturnNowPlaying knows(Connection connection, String artist, long guildId);

	void addGuild(Connection con, long userId, long guildId);

	void setUpdatedTime(Connection connection, String id, Integer timestamp);

	List<UniqueData> getCrowns(Connection connection, String lastFmId, long guildID);

	List<UniqueData> getGuildTop(Connection connection, Long guildID);

	void upsertArtist(Connection con, ArtistData artistData);

	void upsertUrl(Connection con, ArtistData artistData);
}
