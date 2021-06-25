package dao;

import dao.entities.*;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

interface SQLQueriesDao {


    List<ScrobbledArtist> getRecommendations(Connection connection, long discordID, long giverDiscordId, boolean ignorePast, int limit);

    UniqueWrapper<ArtistPlays> getUniqueArtist(Connection connection, Long guildID, String lastFMID);


    UniqueWrapper<AlbumPlays> getUniqueAlbum(Connection connection, Long guildID, String lastfmId);

    UniqueWrapper<TrackPlays> getUniqueTracks(Connection connection, Long guildID, String lastfmId);

    ResultWrapper<UserArtistComparison> similar(Connection connection, List<String> lastfMNames, int limit);

    WrapperReturnNowPlaying knows(Connection connection, long artistId, long guildId, int limit);


    UniqueWrapper<ArtistPlays> getCrowns(Connection connection, String lastFmId, long guildID, int crownThreshold);

    ResultWrapper<ScrobbledArtist> getGuildTop(Connection connection, Long guildID, int limit, boolean doCount);

    int userPlays(Connection con, long artistId, String whom);

    List<LbEntry<Integer>> crownsLeaderboard(Connection con, long guildID, int threshold);

    List<LbEntry<Integer>> uniqueLeaderboard(Connection connection, long guildId);


    Optional<Rank<PrivacyUserCount>> getGlobalPosition(Connection connection, long discordId);

    List<PrivacyUserCount> getGlobalCommands(Connection connection);

    int userArtistCount(Connection con, String whom, int threshold);

    List<LbEntry<Integer>> artistLeaderboard(Connection con, long guildID, int threshold);

    double obscurity(Connection connection, String lastfmId);

    Optional<ObscurityStats> serverObscurityStats(Connection connection, long guildId);

    List<LbEntry<Double>> obscurityLeaderboard(Connection connection, long guildId);

    PresenceInfo getRandomArtistWithUrl(Connection connection);


    StolenCrownWrapper artistsBehind(Connection connection, String ogUser, String queriedUser,
                                     int threshold);

    StolenCrownWrapper getCrownsStolenBy(Connection connection, String ogUser, String queriedUser, long guildId, int threshold);


    UniqueWrapper<AlbumPlays> getUserAlbumCrowns(Connection connection, String lastfmId, int crownThreshold, long guildID);

    List<LbEntry<Integer>> albumCrownsLeaderboard(Connection con, long guildID, int threshold);

    ObscuritySummary getUserObscuritPoints(Connection connection, String lastfmid);

    int getRandomCount(Connection connection, Long userId);

    List<GlobalCrown> getGlobalKnows(Connection connection, long artistID, boolean includeBottedUsers, long ownerId);

    void getGlobalRank(Connection connection, String lastfmid);

    UniqueWrapper<ArtistPlays> getGlobalCrowns(Connection connection, String lastFmId, int threshold, boolean includeBottedUsers, long ownerId);

    UniqueWrapper<ArtistPlays> getGlobalUniques(Connection connection, String lastfmId);

    ResultWrapper<ArtistPlays> getArtistPlayCount(Connection connection, Long guildId);

    ResultWrapper<ArtistPlays> getArtistsFrequencies(Connection connection, Long guildId);

    List<TagPlays> getServerTags(Connection connection, Long guildId, boolean doCount);

    ResultWrapper<ArtistPlays> getGlobalArtistPlayCount(Connection connection);

    ResultWrapper<ArtistPlays> getGlobalArtistFrequencies(Connection connection);

    List<ScrobbledArtist> getAllUsersArtist(Connection connection, long discordId, Integer limit);

    List<LbEntry<Integer>> matchingArtistCount(Connection connection, long userId, long guildId, Long threshold);

    List<VotingEntity> getAllArtistImages(Connection connection, long artist_id);

    Boolean hasUserVotedImage(Connection connection, long url_id, long discord_id);


    List<String> getArtistAliases(Connection connection, long artistId);

    long getArtistPlays(Connection connection, Long guildID, long artistId);


    long getAlbumPlays(Connection connection, Long guildID, long albumId);

    long getSongPlays(Connection connection, Long guildID, long trackId);

    long getArtistFrequencies(Connection connection, Long guildID, long artistId);


    long getAlbumFrequencies(Connection connection, Long guildID, long albumId);

    long getSongFrequencies(Connection connection, Long guildID, long trackId);

    int getGuildCrownThreshold(Connection connection, long guildID);


    boolean getGuildConfigEmbed(Connection connection, long guildID);

    List<LbEntry<Integer>> getScrobblesLeaderboard(Connection connection, long guildId);

    List<CrownableArtist> getCrownable(Connection connection, Long discordId, Long guildId, boolean skipCrowns, boolean onlySecond, int crownDistance);

    Map<Long, Float> getRateLimited(Connection connection);

    WrapperReturnNowPlaying getGlobalWhoKnows(Connection connection, long artistId, int limit, boolean includeBottedUsers, long ownerId, boolean hidePrivate);

    List<GlobalCrown> getGlobalRankingAlbum(Connection connection, long albumId, boolean includeBottedUsers, long ownerId);

    List<GlobalCrown> getGlobalRankingSong(Connection connection, long trackId, boolean includeBottedUsers, long ownerId);

    WrapperReturnNowPlaying whoKnowsAlbum(Connection con, long albumId, long guildId, int limit);


    WrapperReturnNowPlaying whoKnowsTrack(Connection con, long trackId, long guildId, int limit);

    WrapperReturnNowPlaying globalWhoKnowsAlbum(Connection con, long albumId, int limit, long ownerId, boolean includeBottedUsers, boolean hidePrivate);

    UniqueWrapper<AlbumPlays> albumUniques(Connection connection, long guildId, String lastfmId);

    BotStats getBotStats(Connection connection);

    long getUserAlbumCount(Connection connection, long discordId);

    List<StreakEntity> getUserStreaks(long discordId, Connection connection);

    List<GlobalStreakEntities> getTopStreaks(Connection connection, Long extraParam, Long guildId);

    String getReverseCorrection(Connection connection, String correction);


    List<GlobalStreakEntities> getArtistTopStreaks(Connection connection, Long extraParam, Long guildId, long artistId, Integer limit);

    List<StreakEntity> getUserArtistTopStreaks(Connection connection, long artistId, Integer limit, long discordId);


    List<ScoredAlbumRatings> getServerTopRandomUrls(Connection connection, long guildId);

    List<ScoredAlbumRatings> getTopUrlsRatedByUser(Connection connection, long discordId);

    List<ScoredAlbumRatings> getGlobalTopRandomUrls(Connection connection);

    List<ScoredAlbumRatings> getUserTopRatedUrlsByEveryoneElse(Connection connection, long discordId);

    Set<String> getPrivateLastfmIds(Connection connection);

    List<ScrobbledAlbum> getUserAlbums(Connection connection, String lastfmId, Integer limit);


    ResultWrapper<ScrobbledAlbum> getGuildTopAlbum(Connection connection, Long guildID, int limit, boolean doCount);


    ResultWrapper<ScrobbledAlbum> getCollectiveAOTY(Connection connection, Year year, @Nullable Long guildID, int limit, boolean doCount);

    List<ScrobbledArtist> getTopTag(Connection connection, String genre, @Nullable Long guildId, int limit);

    WrapperReturnNowPlaying whoKnowsTag(Connection connection, String genre, long guildId, int limit);

    Set<String> getBannedTags(Connection connection);

    List<ScrobbledAlbum> getUserAlbumsWithTag(Connection connection, long discordId, String tag, int limit);

    List<AlbumInfo> getAlbumsWithTag(Connection connection, List<Long> albums, long discordId, String tag);


    List<ScrobbledArtist> getUserArtistsWithMBID(Connection connection, String lastfmId);

    List<ScrobbledArtist> getUserArtistWithTag(Connection connection, long discordId, String genre, int limit);

    List<ArtistInfo> getArtistWithTag(Connection connection, List<ArtistInfo> artists, long discordId, String genre);

    Map<Genre, Integer> genreCountsByArtist(Connection connection, List<ArtistInfo> artistInfos);


    List<WrapperReturnNowPlaying> whoknowsSet(Connection connection, Set<String> artists, long guildId, int limit, @Nullable String user);

    WrapperReturnNowPlaying whoknowsTagsSet(Connection connection, Set<String> tags, long guildId, int limit, String user, SearchMode mode);

    List<ScrobbledArtist> getTopTagSet(Connection connection, Set<String> genre, Long guildId, int limit, SearchMode mode);

    Set<Pair<String, String>> getArtistBannedTags(Connection connection);

    Set<String> getArtistTag(Connection connection, long artistId);

    WrapperReturnNowPlaying globalWhoKnowsTrack(Connection connection, long trackId, int limit, long ownerId, boolean includeBotted, boolean hidePrivate);

    UniqueWrapper<TrackPlays> getUserTrackCrowns(Connection connection, String lastfmId, int crownthreshold, long guildId);

    List<LbEntry<Integer>> trackCrownsLeaderboard(Connection connection, long guildId, int threshold);

    ResultWrapper<UserArtistComparison> similarAlbumes(Connection connection, long artistId, List<String> lastFmNames, int limit);

    ResultWrapper<UserArtistComparison> similarTracks(Connection connection, long artistId, List<String> lastFmNames, int limit);

    ResultWrapper<UserArtistComparison> similarAlbumTracks(Connection connection, long albumId, List<String> name, int limit);

    UniqueWrapper<ArtistPlays> getGlobalAlbumCrowns(Connection connection, String lastfmid, int threshold, boolean includeBottedUsers, long ownerId);

    UniqueWrapper<ArtistPlays> getGlobalTrackCrowns(Connection connection, String lastfmid, int threshold, boolean includeBottedUsers, long ownerId);

    UniqueWrapper<TrackPlays> getArtistGlobalTrackCrowns(Connection connection, String lastfmName, long artistId, int threshold, boolean bottedUsers);


    UniqueWrapper<TrackPlays> getUserArtistTrackCrowns(Connection connection, String lastfmId, int crownthreshold, long guildId, long artistId);


    Optional<String> findArtistUrlAbove(Connection connection, long artistId, int upvotes);

    Optional<Instant> getLastScrobbled(Connection connection, String lastfmId, long artistId, String song, boolean skipToday);

    Optional<Instant> getLastScrobbledArtist(Connection connection, String lastfmId, long artistId, boolean skipToday);


    int getCurrentCombo(Connection connection, String lastfm_id, long artistId);

    Optional<Instant> getFirstScrobbled(Connection connection, String lastfmId, long artistId, String song);

    Optional<Instant> getFirstScrobbledArtist(Connection connection, String lastfmId, long artistId);


    List<UserListened> getServerFirstScrobbledArtist(Connection connection, long artistId, long guildId, SQLQueriesDaoImpl.Order Order);


    List<ScrobbledArtist> regexArtist(Connection connection, String regex, long userId);

    List<ScrobbledAlbum> regexAlbum(Connection connection, String regex, long userId);

    List<ScrobbledTrack> regexTrack(Connection connection, String regex, long userId);


    ResultWrapper<ScrobbledAlbum> getCollectiveAOTD(Connection connection, Year year, int range, Long guildID, int limit, boolean doCount);

    Map<Year, Integer> getUserYears(Connection connection, String lastfmId, boolean isDecade);


    Map<Year, Integer> getCollectiveYears(Connection connection, @Nullable Long guildId, boolean isDecade);


    int userAlbumCount(Connection connection, String lastfmId, int threshold);

    int userTrackCount(Connection connection, String lastfmId, int threshold);

    List<LbEntry<Integer>> albumLeaderboard(Connection connection, long guildId, int threshold);

    List<LbEntry<Integer>> trackLeaderboard(Connection connection, long guildId, int threshold);

    ListValuedMap<CoverItem, String> getBannedCovers(Connection connection);

    UniqueWrapper<AlbumPlays> getGlobalAlbumUniques(Connection connection, String lastfmid);

    UniqueWrapper<TrackPlays> getGlobalTrackUniques(Connection connection, String lastfmid);

    List<LbEntry<Integer>> uniqueAlbumLeaderboard(Connection connection, long guildId);

    List<LbEntry<Integer>> uniqueSongLeaderboard(Connection connection, long guildId);

    List<ScrobbledTrack> getUserTracksWithTag(Connection connection, long discordId, String genre, int limit);

    List<TrackInfo> getTracksWithTag(Connection connection, List<Long> tracks, long discordId, String tag);

    List<CommandUsage> getUserCommands(Connection connection, long discordId);

    List<UserCount> getServerCommandsLb(Connection connection, long guildId);

    AudioFeatures userFeatures(Connection connection, String lastfmId);

    List<ScrobbledArtist> getServerArtistsByMbid(Connection connection, long guildId);


}
