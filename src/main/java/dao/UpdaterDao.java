package dao;

import core.exceptions.DuplicateInstanceException;
import core.exceptions.InstanceNotFoundException;
import dao.entities.*;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;

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

    void updateStatusBit(Connection connection, long artistId);

    String findCorrection(Connection connection, String artist);

    void updateMetric(Connection connection, int metricId, long value);

    void deleteAllArtists(Connection con, String id);

    boolean insertRandomUrl(Connection con, String url, long discordId, Long guildId);

    RandomUrlEntity getRandomUrl(Connection con);

    RandomUrlEntity findRandomUrlById(Connection con, String url);

    void insertAlbumCrown(Connection connection, long artistId, String album, long discordID, long guildId, int plays);

    Map<Long, Character> getGuildPrefixes(Connection connection);

    void upsertGuildPrefix(Connection connection, long guildID, Character prefix);

    void deleteAlbumCrown(Connection connection, String artist, String album, long discordID, long guildId);

    void truncateRandomPool(Connection connection);


    void fillIds(Connection connection, List<ScrobbledArtist> list);

    void insertArtistSad(Connection connection, ScrobbledArtist nonExistingId);

    void insertArtists(Connection connection, List<ScrobbledArtist> nonExistingId);

    long getArtistId(Connection connection, String artistId) throws InstanceNotFoundException;


    UpdaterUserWrapper getUserUpdateStatus(Connection connection, long discordId) throws InstanceNotFoundException;

    void addAlias(Connection connection, String alias, long toArtistId) throws DuplicateInstanceException;

    void queueAlias(Connection connection, String alias, long toArtistId, long whom);

    AliasEntity getNextInAliasQueue(Connection connection);

    ReportEntity getReportEntity(Connection connection, LocalDateTime localDateTime);

    void deleteAliasById(Connection connection, long aliasId) throws InstanceNotFoundException;

    void updateUrlStatus(Connection connection, long artistId);

    OptionalLong checkArtistUrlExists(Connection connection, long artistId, String urlParsed);

    void removeVote(Connection con, long urlId, long discordId);

    boolean castVote(Connection con, long urlId, long discordId, boolean isPositive);

    void reportImage(Connection connection, long urlId, long userIdLong);


    void removeImage(Connection connection, long altId);

    void logRemovedImage(Connection connection, long imageOwner, long modId);

    int getReportCount(Connection connection);

    void removeReport(Connection connection, long reportId);

    void insertPastRecommendation(Connection connection, long secondDiscordID, long firstDiscordID, long artistId);


}
