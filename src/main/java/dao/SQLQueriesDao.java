package dao;

import dao.entities.*;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

interface SQLQueriesDao {


    List<ScrobbledArtist> getRecommendations(Connection connection, long discordID, long giverDiscordId, boolean ignorePast, int limit);

    UniqueWrapper<ArtistPlays> getUniqueArtist(Connection connection, Long guildID, String lastFMID);


    ResultWrapper<UserArtistComparison> similar(Connection connection, List<String> lastfMNames, int limit);

    WrapperReturnNowPlaying knows(Connection connection, long artistId, long guildId, int limit);


    UniqueWrapper<ArtistPlays> getCrowns(Connection connection, String lastFmId, long guildID, int crownThreshold);

    ResultWrapper<ScrobbledArtist> getGuildTop(Connection connection, Long guildID, int limit, boolean doCount);

    int userPlays(Connection con, long artistId, String whom);

    List<LbEntry> crownsLeaderboard(Connection con, long guildID, int threshold);

    List<LbEntry> uniqueLeaderboard(Connection connection, long guildId);

    int userArtistCount(Connection con, String whom, int threshold);

    List<LbEntry> artistLeaderboard(Connection con, long guildID, int threshold);

    List<LbEntry> obscurityLeaderboard(Connection connection, long guildId);

    PresenceInfo getRandomArtistWithUrl(Connection connection);


    StolenCrownWrapper getCrownsStolenBy(Connection connection, String ogUser, String queriedUser, long guildId, int threshold);


    UniqueWrapper<AlbumPlays> getUserAlbumCrowns(Connection connection, String lastfmId, int crownThreshold, long guildID);

    List<LbEntry> albumCrownsLeaderboard(Connection con, long guildID, int threshold);

    ObscuritySummary getUserObscuritPoints(Connection connection, String lastfmid);

    int getRandomCount(Connection connection, Long userId);

    List<GlobalCrown> getGlobalKnows(Connection connection, long artistID, boolean includeBottedUsers, long ownerId);

    void getGlobalRank(Connection connection, String lastfmid);

    UniqueWrapper<ArtistPlays> getGlobalCrowns(Connection connection, String lastFmId, int threshold, boolean includeBottedUsers, long ownerId);

    UniqueWrapper<ArtistPlays> getGlobalUniques(Connection connection, String lastfmId);

    ResultWrapper<ArtistPlays> getArtistPlayCount(Connection connection, Long guildId);

    ResultWrapper<ArtistPlays> getArtistsFrequencies(Connection connection, Long guildId);

    ResultWrapper<ArtistPlays> getGlobalArtistPlayCount(Connection connection);

    ResultWrapper<ArtistPlays> getGlobalArtistFrequencies(Connection connection);

    List<ScrobbledArtist> getAllUsersArtist(Connection connection, long discordId);

    List<LbEntry> matchingArtistCount(Connection connection, long userId, long guildId, Long threshold);

    List<VotingEntity> getAllArtistImages(Connection connection, long artist_id);

    Boolean hasUserVotedImage(Connection connection, long url_id, long discord_id);


    List<String> getArtistAliases(Connection connection, long artistId);

    long getArtistPlays(Connection connection, Long guildID, long artistId);


    long getArtistFrequencies(Connection connection, Long guildID, long artistId);


    int getGuildCrownThreshold(Connection connection, long guildID);


    boolean getGuildConfigEmbed(Connection connection, long guildID);

    List<LbEntry> getScrobblesLeaderboard(Connection connection, long guildId);

    List<CrownableArtist> getCrownable(Connection connection, Long discordId, Long guildId, boolean skipCrowns, boolean onlySecond, int crownDistance);

    Map<Long, Float> getRateLimited(Connection connection);

    WrapperReturnNowPlaying getGlobalWhoKnows(Connection connection, long artistId, int limit, boolean includeBottedUsers, long ownerId);

    WrapperReturnNowPlaying whoKnowsAlbum(Connection con, long albumId, long guildId, int limit);


    WrapperReturnNowPlaying globalWhoKnowsAlbum(Connection con, long albumId, int limit, long ownerId, boolean includeBottedUsers);

    UniqueWrapper<AlbumPlays> albumUniques(Connection connection, long guildId, String lastfmId);

    BotStats getBotStats(Connection connection);

    long getUserAlbumCount(Connection connection, long discordId);

}
