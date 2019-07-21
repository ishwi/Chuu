package DAO;

import DAO.Entities.*;

import java.sql.Connection;
import java.util.List;

public interface SQLQueriesDao {

	UniqueWrapper<UniqueData> getUniqueArtist(Connection connection, Long guildID, String lastFMID);


	ResultWrapper similar(Connection connection, List<String> lastfMNames);

	WrapperReturnNowPlaying knows(Connection connection, String artist, long guildId, int limit);


	UniqueWrapper<UniqueData> getCrowns(Connection connection, String lastFmId, long guildID);

	List<UrlCapsule> getGuildTop(Connection connection, Long guildID);

	int userPlays(Connection con, String artist, String whom);

	List<CrownsLbEntry> crownsLeaderboard(Connection con, long guildID);
}
