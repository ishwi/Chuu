package DAO;

import DAO.Entities.*;

import java.sql.Connection;
import java.util.List;

interface SQLQueriesDao {

	UniqueWrapper<UniqueData> getUniqueArtist(Connection connection, Long guildID, String lastFMID);


	ResultWrapper similar(Connection connection, List<String> lastfMNames);

	WrapperReturnNowPlaying knows(Connection connection, String artist, long guildId, int limit);


	UniqueWrapper<UniqueData> getCrowns(Connection connection, String lastFmId, long guildID);

	List<UrlCapsule> getGuildTop(Connection connection, Long guildID);

	int userPlays(Connection con, String artist, String whom);

	List<LbEntry> crownsLeaderboard(Connection con, long guildID);

	List<LbEntry> uniqueLeaderboard(Connection connection, long guildId);

	int userArtistCount(Connection con, String whom);

	List<LbEntry> artistLeaderboard(Connection con, long guildID);

}
