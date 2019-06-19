package DAO;

import DAO.Entities.*;
import org.apache.commons.collections4.map.MultiValueMap;

import javax.management.InstanceNotFoundException;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.sql.Connection;
import java.util.List;
import java.util.Set;

interface SQLShowsDao {
	LastFMData create(Connection con, LastFMData show);

	LastFMData find(Connection con, Long showID) throws InstanceNotFoundException;

	List<Long> guildList(Connection connection, long userId);

	MultiValueMap<Long, Long> getWholeUser_Guild(Connection connection);

	void update(Connection con, LastFMData show);

	void removeUser(Connection con, Long showID);

	void addArtist(Connection con, ArtistData artistData);

	UniqueWrapper<UniqueData> getUniqueArtist(Connection connection, Long guildID, String lastFMID);

	UsersWrapper getLessUpdated(Connection connection);

	List<UsersWrapper> getAll(Connection connection, long guildId);

	ResultWrapper similar(Connection connection, List<String> lastfMNames) throws InstanceNotFoundException;

	void addUrl(Connection con, ArtistData artistData);

	WrapperReturnNowPlaying knows(Connection connection, String artist, long guildId, int limit);

	void addGuild(Connection con, long userId, long guildId);

	void setUpdatedTime(Connection connection, String id, Integer timestamp);

	UniqueWrapper<UniqueData> getCrowns(Connection connection, String lastFmId, long guildID);

	List<UrlCapsule> getGuildTop(Connection connection, Long guildID);

	void upsertArtist(Connection con, ArtistData artistData);

	void upsertUrl(Connection con, ArtistInfo artistInfo);

	String getArtistUrl(Connection connection, String artist);

	Set<String> selectNullUrls(Connection connection, boolean spotifyNull);

	void addLogo(Connection con, long guildID, BufferedImage image);

	void removeLogo(Connection connection, long guildId);

	InputStream findLogo(Connection connection, long guildID);

	long getDiscordIdFromLastfm(Connection connection, String lastFmName, long guildId);

	void upsertSpotify(Connection con, ArtistInfo artistInfo);

	int userPlays(Connection con, String artist, String whom);

	List<CrownsLbEntry> crownsLeaderboard(Connection con, long guildID);
}
