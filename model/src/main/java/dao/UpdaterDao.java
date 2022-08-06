package dao;

import dao.entities.*;
import dao.exceptions.DuplicateInstanceException;
import dao.exceptions.InstanceNotFoundException;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.time.Instant;
import java.util.*;

interface UpdaterDao {

    void addSrobbledArtists(Connection con, List<ScrobbledArtist> scrobbledArtists);

    UpdaterUserWrapper getLessUpdated(Connection connection);

    void setUpdatedTime(Connection connection, String id, Integer timestamp, Integer timestampControl);

    void upsertArtist(Connection con, List<ScrobbledArtist> scrobbledArtist);


    long upsertUrl(Connection con, String url, long artistId, long discordId);

    void upsertArtistsDetails(Connection con, List<ScrobbledArtist> scrobbledArtists);

    String getArtistUrl(Connection connection, String artist);

    Set<ScrobbledArtist> selectNullUrls(Connection connection, boolean spotifyNull);


    void upsertSpotify(Connection con, String url, long artistId, long discordId);

    UpdaterStatus getUpdaterStatus(Connection connection, String artist) throws InstanceNotFoundException;

    void insertCorrection(Connection connection, long artistId, String correction);

    void updateStatusBit(Connection connection, long artistId, boolean statusBit, String url);

    String findCorrection(Connection connection, String artist);

    void updateMetric(Connection connection, int metricId, long value);

    void deleteAllArtists(Connection con, String id);

    boolean insertRandomUrl(Connection con, String url, long discordId, Long guildId);

    RandomUrlEntity getRandomUrl(Connection con, @Nullable RandomTarget randomTarget);

    RandomUrlEntity getRandomUrlFromServer(Connection con, long discordId, @Nullable RandomTarget randomTarget);

    void updateArtistRankingUnset(Connection connection);

    RandomUrlEntity findRandomUrlById(Connection con, String url);

    RandomUrlDetails randomUrlDetails(Connection con, String urlQ);

    void insertAlbumCrown(Connection connection, long artistId, String album, long discordID, long guildId, long plays);

    Map<Long, Character> getGuildPrefixes(Connection connection, char defaultPrefix);

    void upsertGuildPrefix(Connection connection, long guildID, Character prefix);

    void deleteAlbumCrown(Connection connection, String artist, String album, long discordID, long guildId);

    void truncateRandomPool(Connection connection);


    void fillIds(Connection connection, List<? extends ScrobbledArtist> list);

    void insertArtistSad(Connection connection, ScrobbledArtist nonExistingId);

    void insertArtists(Connection connection, List<ScrobbledArtist> nonExistingId);

    long getArtistId(Connection connection, String artistId) throws InstanceNotFoundException;


    long getAlbumIdByRYMId(Connection connection, Long rymId) throws InstanceNotFoundException;

    long getAlbumByName(Connection connection, String album, long artist_id) throws InstanceNotFoundException;

    UpdaterUserWrapper getUserUpdateStatus(Connection connection, long discordId) throws InstanceNotFoundException;

    void addAlias(Connection connection, String alias, long toArtistId) throws DuplicateInstanceException;

    void queueAlias(Connection connection, String alias, long toArtistId, long whom);

    AliasEntity getNextInAliasQueue(Connection connection);

    ReportEntity getReportEntity(Connection connection, long maxIdAllowed, Set<Long> skippedIds);

    void deleteAliasById(Connection connection, long aliasId) throws InstanceNotFoundException;

    void updateUrlStatus(Connection connection, long artistId, String spotifyId);

    OptionalLong checkArtistUrlExists(Connection connection, long artistId, String urlParsed);

    void removeVote(Connection con, long urlId, long discordId);

    boolean castVote(Connection con, long urlId, long discordId, boolean isPositive);

    void reportImage(Connection connection, long urlId, long userIdLong);


    void removeImage(Connection connection, long altId);

    void logRemovedImage(Connection connection, long imageOwner, long modId);

    int getReportCount(Connection connection);

    void removeReport(Connection connection, long reportId);

    void removeQueuedImage(Connection connection, long altId);

    void insertPastRecommendation(Connection connection, long secondDiscordID, long firstDiscordID, long artistId);


    void updateGuildCrownThreshold(Connection connection, long guildId, int newThreshold);


    ImageQueue getUrlQueue(Connection connection, long maxIdAllowed, Set<Long> skippedIds);

    void upsertQueueUrl(Connection connection, String url, long artistId, long discordId, Long guildId);

    OptionalLong checkQueuedUrlExists(Connection connection, long artistId, String urlParsed);

    int getQueueUrlCount(Connection connection);

    void deleteAllRatings(Connection connection, long userId);

    void fillALbumsByRYMID(Connection connection, List<RYMImportRating> list);

    void insertAlbumSad(Connection connection, RYMImportRating x);

    void insertCombo(Connection connection, StreakEntity combo, long discordID, long artistId, @Nullable Long albumId);

    void addUrlRating(Connection connection, long author, int rating, String url);

    UsersWrapper getRandomUser(Connection connection);

    void updateMbids(Connection connection, List<ScrobbledArtist> artistData);

    void updateAlbumImage(Connection connection, long albumId, String albumUrl);

    List<ScrobbledAlbum> fillAlbumsByMBID(Connection connection, List<AlbumInfo> albums);

    void insertAlbumTags(Connection connection, Map<Genre, List<ScrobbledAlbum>> genres, Map<String, String> correctedTags);

    void insertArtistTags(Connection connection, Map<Genre, List<ScrobbledArtist>> genres, Map<String, String> correctedTags);

    Map<String, String> validateTags(Connection connection, List<Genre> keySet);

    void addBannedTag(Connection connection, String contentRaw);

    void logBannedTag(Connection connection, String contentRaw, long discordId);

    void removeTagWholeArtist(Connection connection, String tag);

    void removeTagWholeAlbum(Connection connection, String tag);

    void removeTagWholeTrack(Connection connection, String tag);

    void addArtistBannedTag(Connection connection, String tag, long artistId);

    void removeTagArtist(Connection connection, String tag, long artistId);

    void removeTagAlbum(Connection connection, String tag, long artistId);

    void removeTagTrack(Connection connection, String tag, long artistId);

    void logCommand(Connection connection, long discordId, Long guildId, String commandName, long nanos, Instant utc, boolean success, boolean isNormalCommand);

    void updateTrackImage(Connection connection, long trackId, String imageUrl);

    void updateSpotifyInfo(Connection connection, long trackId, String spotifyId, int duration, String url, int popularity);

    void insertAudioFeatures(Connection connection, List<AudioFeatures> audioFeatures);


    void insertUserInfo(Connection connection, UserInfo userInfo);

    Optional<UserInfo> getUserInfo(Connection connection, String lastfmId);

    void storeToken(Connection connection, String authToken, String lastfm);

    void storeSession(Connection connection, String session, String lastfm);

    void clearSess(Connection connection, String lastfm);


    long storeRejected(Connection connection, String url, long artistId, long uploader);

    void banUserImage(Connection connection, long uploader);

    void addStrike(Connection connection, long uploader, long rejectedId);

    long userStrikes(Connection connection, long uploader);

    void deleteRandomUrl(Connection connection, String url);

    void insertTrackTags(Connection connection, Map<Genre, List<ScrobbledTrack>> genres, Map<String, String> correctedTags);


    RandomUrlEntity getRandomUrlFromUser(Connection connection, long userId, RandomTarget randomTarget);

    List<ImageQueue> getUrlQueue(Connection connection, boolean newFirst);


    void updateArtistRanking(Connection connection);


}
