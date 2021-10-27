package dao;

import dao.entities.*;
import dao.everynoise.*;
import dao.exceptions.ChuuServiceException;
import dao.exceptions.DuplicateInstanceException;
import dao.exceptions.InstanceNotFoundException;
import dao.utils.Order;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.sql.Date;
import java.sql.*;
import java.text.Normalizer;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChuuService implements EveryNoiseService {
    private final Logger logger = LoggerFactory.getLogger(ChuuService.class);
    private final CommonDatasource dataSource;
    private final SQLQueriesDao queriesDao;
    private final AffinityDao affinityDao;

    private final UpdaterDao updaterDao;
    private final SQLRYMDao rymDao;
    private final UserGuildDao userGuildDao;
    private final AlbumDao albumDao;
    private final TrackDao trackDao;

    private final BillboardDao billboardDao;
    private final DiscoveralDao discoveralDao;
    private final MusicDao musicDao;
    private final EveryNoiseService everyNoiseService;
    private final FriendDAO friendDAO;

    public ChuuService(CommonDatasource dataSource) {
        this.dataSource = dataSource;
        this.albumDao = new AlbumDaoImpl();
        this.queriesDao = new SQLQueriesDaoImpl();
        this.userGuildDao = new UserGuildDaoImpl();
        this.affinityDao = new AffinityDaoImpl();
        this.trackDao = new TrackDaoImpl();
        this.rymDao = new SQLRYMDaoImpl();
        this.updaterDao = new UpdaterDaoImpl();
        this.billboardDao = new BillboardDaoImpl();
        this.discoveralDao = new DiscoveralDaoImpl();
        musicDao = new MusidDaoImpl();
        this.everyNoiseService = new EveryNoiseServiceImpl(dataSource);
        this.friendDAO = new FriendDAOImpl();
    }


    public void updateUserTimeStamp(String lastFmName, Integer timestamp, Integer timestampControl) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.setUpdatedTime(connection, lastFmName, timestamp, timestampControl);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    private void insertArtistDataListConnection(List<ScrobbledArtist> list, String id, Connection connection) {
        try {

            if (!list.isEmpty()) {
                //delete everything first to have a clean start
                /* Do work. */
                connection.setAutoCommit(true);

                updaterDao.fillIds(connection, list);

                Map<Boolean, List<ScrobbledArtist>> map = list.stream().peek(x -> x.setDiscordID(id)).collect(Collectors.partitioningBy(scrobbledArtist -> scrobbledArtist.getArtistId() == -1));
                List<ScrobbledArtist> nonExistingId = map.get(true);
                if (!nonExistingId.isEmpty()) {
                    nonExistingId.forEach(x -> updaterDao.insertArtistSad(connection, x));
                    //updaterDao.insertArtists(connection, nonExistingId);
                }
                List<ScrobbledArtist> scrobbledArtists = map.get(false);
                scrobbledArtists.addAll(nonExistingId);
                connection.setAutoCommit(false);
                updaterDao.deleteAllArtists(connection, id);
                updaterDao.addSrobbledArtists(connection, scrobbledArtists);
                connection.commit();
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void insertArtistDataList(List<ScrobbledArtist> list, String id) {
        try (Connection connection = dataSource.getConnection()) {
            insertArtistDataListConnection(list, id, connection);
            updaterDao.setUpdatedTime(connection, id, null, null);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void incrementalUpdate(TimestampWrapper<List<ScrobbledArtist>> wrapper, String
            id, List<ScrobbledAlbum> albumData, List<TrackWithArtistId> trackWithArtistIds) {
        try (Connection connection = dataSource.getConnection()) {
            try {
                List<ScrobbledArtist> artistData = wrapper.getWrapped().stream().peek(x -> x.setDiscordID(id)).toList();
                albumData = albumData.stream().filter(x -> x.getAlbum() != null && !x.getAlbum().isBlank()).toList();
                connection.setAutoCommit(true);
                if (!artistData.isEmpty())
                    updaterDao.upsertArtist(connection, artistData);
                updaterDao.fillIds(connection, artistData);
                Map<String, Long> artistIds = artistData.stream().collect(Collectors.toMap(ScrobbledArtist::getArtist, ScrobbledArtist::getArtistId, (a, b) -> {
                    assert a.equals(b);
                    return a;
                }));

                Pattern compile = Pattern.compile("\\p{M}");
                albumData.forEach(x -> {
                    Long artistId = artistIds.get(x.getArtist());
                    if (artistId == null) {
                        String normalizeArtistName = compile.matcher(
                                Normalizer.normalize(x.getArtist(), Normalizer.Form.NFKD)
                        ).replaceAll("");
                        artistId = artistIds.get(normalizeArtistName);
                        artistId = handleNonExistingArtistFromAlbum(connection, x, artistId);
                    }
                    x.setArtistId(artistId);
                });
                connection.setAutoCommit(false);
                connection.commit();

                insertAlbums(albumData, id, connection, false);
                Map<AlbumInfo, ScrobbledAlbum> albumInfoes = albumData.stream()
                        .filter(x -> x.getAlbum() != null && !x.getAlbum().isBlank())
                        .collect(Collectors.toMap(x -> new AlbumInfoIgnoreMbid(x.getAlbumMbid(), x.getAlbum(), x.getArtist()), x -> x, (x, y) -> x));
                trackWithArtistIds.forEach(x -> {
                    if (x.getAlbum() != null && !x.getAlbum().isBlank()) {
                        ScrobbledAlbum scrobbledAlbum = albumInfoes.get(new AlbumInfo(x.getAlbumMbid(), x.getName(), x.getArtist()));
                        if (scrobbledAlbum != null) {
                            x.setAlbumId(scrobbledAlbum.getAlbumId());
                        }
                    }
                });
                doInsertUserData(connection, id, trackWithArtistIds);
                connection.commit();
                updaterDao.setUpdatedTime(connection, id, wrapper.getTimestamp(), wrapper.getTimestamp());
                connection.commit();


            } catch (SQLException e) {
                throw new ChuuServiceException(e);
            }

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Nonnull
    private <T extends ScrobbledArtist> Long handleNonExistingArtistFromAlbum(Connection connection, T x, Long artistId) {
        if (artistId == null) {
            String correction = updaterDao.findCorrection(connection, x.getArtist());
            try {
                artistId = updaterDao.getArtistId(connection, correction);
            } catch (InstanceNotFoundException exception) {
                ScrobbledArtist nonExistingId = new ScrobbledArtist(x.getArtist(), 0, null);
                nonExistingId.setArtistMbid(x.getArtistMbid());
                updaterDao.insertArtistSad(connection, nonExistingId);
                artistId = nonExistingId.getArtistId();
            }
        }
        return artistId;
    }


    public void addGuildUser(long userID, long guildID) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.addGuild(connection, userID, guildID);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public boolean isFlagged(String lastfmId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return userGuildDao.isFlagged(connection, lastfmId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void insertNewUser(LastFMData data) {
        try (Connection connection = dataSource.getConnection()) {

            try {
                connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
                connection.setAutoCommit(false);

                userGuildDao.insertUserData(connection, data);
                if (data.getGuildID() > 0 && !userGuildDao.isUserServerBanned(connection, data.getDiscordId(), data.getGuildID())) {
                    userGuildDao.addGuild(connection, data.getDiscordId(), data.getGuildID());
                }
                connection.commit();
                String name = data.getName();
                boolean flagged = userGuildDao.isFlagged(connection, name);
                if (flagged) {
                    userGuildDao.flagBottedDB(name, connection);
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new ChuuServiceException(e);
            } catch (RuntimeException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    @Deprecated
    public void updateLastFmData(long discordID, String lastFMID) {

        try (Connection connection = dataSource.getConnection()) {

            try {

                connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                connection.setAutoCommit(false);

                LastFMData lastFmData = userGuildDao.findLastFmData(connection, discordID);

                lastFmData.setName(lastFMID);

                userGuildDao.updateLastFmData(connection, lastFmData);
                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                throw new ChuuServiceException(e);
            } catch (RuntimeException | Error e) {
                connection.rollback();
                throw e;
            } catch (InstanceNotFoundException e) {
                logger.warn(e.getMessage(), e);
            }

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public LastFMData findLastFMData(long discordID) throws InstanceNotFoundException {
        try (Connection connection = dataSource.getConnection()) {
            return userGuildDao.findLastFmData(connection, discordID);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public LastFMData computeLastFmData(long discordID, long guildId) throws InstanceNotFoundException {
        try (Connection connection = dataSource.getConnection()) {
            return userGuildDao.findLastFmData(connection, discordID, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public ResultWrapper<UserArtistComparison> getSimilarities(List<String> lastFmNames, int limit) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.similar(connection, lastFmNames, limit);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public ResultWrapper<UserArtistComparison> getSimilaritiesAlbum(List<String> lastFmNames, long artistId, int limit) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.similarAlbumes(connection, artistId, lastFmNames, limit);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public ResultWrapper<UserArtistComparison> getSimilaritiesTracks(List<String> lastFmNames, long artistId, int limit) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.similarTracks(connection, artistId, lastFmNames, limit);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public ResultWrapper<UserArtistComparison> getSimilarities(List<String> lastfMNames) {
        return getSimilarities(lastfMNames, 10);
    }


    public WrapperReturnNowPlaying globalWhoKnows(long artistId, boolean includeBottedUsers, long ownerId, boolean hidePrivate) {
        return globalWhoKnows(artistId, 10, includeBottedUsers, ownerId, hidePrivate);
    }

    public WrapperReturnNowPlaying globalWhoKnows(long artistId, int limit, boolean includeBottedUsers,
                                                  long ownerId, boolean hidePrivate) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            if (limit < 1)
                limit = 10;
            return queriesDao.getGlobalWhoKnows(connection, artistId, limit, includeBottedUsers, ownerId, hidePrivate);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public WrapperReturnNowPlaying whoKnows(long artistId, long guildId) {
        return whoKnows(artistId, guildId, 10);
    }

    public WrapperReturnNowPlaying whoKnows(long artistId, long guildId, int limit) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            if (limit < 1)
                limit = 10;
            return queriesDao.knows(connection, artistId, guildId, limit);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public UpdaterUserWrapper getLessUpdated() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return updaterDao.getLessUpdated(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<UsersWrapper> getAllObscurifyPending(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return userGuildDao.getAllNotObscurify(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<UsersWrapper> getAll(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return userGuildDao.getAll(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<LastFMData> getAllData(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return userGuildDao.getAllData(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<UsersWrapper> getAllNonPrivate(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return userGuildDao.getAllNonPrivate(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public List<UsersWrapper> getAllALL() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return userGuildDao.getAll(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public UniqueWrapper<ArtistPlays> getUniqueArtist(Long guildID, String lastFmId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getUniqueArtist(connection, guildID, lastFmId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public UniqueWrapper<AlbumPlays> getUniquAlbums(Long guildID, String lastFmId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getUniqueAlbum(connection, guildID, lastFmId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public UniqueWrapper<TrackPlays> getUniqueTracks(Long guildID, String lastFmId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getUniqueTracks(connection, guildID, lastFmId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<Long> getGuildList(long userId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return userGuildDao.guildsFromUser(connection, userId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public ResultWrapper<ScrobbledArtist> getGuildTop(Long guildID, int limit, boolean doCount) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getGuildTop(connection, guildID, limit, doCount);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public UniqueWrapper<ArtistPlays> getCrowns(String lastFmID, long guildID, int threshold) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getCrowns(connection, lastFmID, guildID, threshold);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public int getGuildCrownThreshold(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getGuildCrownThreshold(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public VoiceAnnouncement getGuildVoiceAnnouncement(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return userGuildDao.getGuildVoiceAnnouncement(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void setGuildVoiceAnnouncement(long guildId, VoiceAnnouncement voiceAnnouncement) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setGuildVoiceAnnouncement(connection, guildId, voiceAnnouncement);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public boolean getGuildEmbedConfig(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getGuildConfigEmbed(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public Set<ScrobbledArtist> getNullUrls() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return updaterDao.selectNullUrls(connection, false);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public Set<ScrobbledArtist> getSpotifyNulledUrls() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return updaterDao.selectNullUrls(connection, true);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public String getArtistUrl(String artist) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return updaterDao.getArtistUrl(connection, artist);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }
    }

    public void upsertUrl(String url, long artistId) {
        upsertUrl(url, artistId, 537353774205894676L);
    }

    public void userInsertUrl(String url, long artistId, long discordId) {
        try (Connection connection = dataSource.getConnection()) {
            long urlId = updaterDao.upsertUrl(connection, url, artistId, discordId);
            updaterDao.castVote(connection, urlId, discordId, true);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }

    }

    public void userInsertQueueUrl(String url, long artistId, long discordId) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.upsertQueueUrl(connection, url, artistId, discordId, null);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }

    }

    public void userInsertQueueUrlForServer(String url, long artistId, long discordId, long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.upsertQueueUrl(connection, url, artistId, discordId, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }

    }

    private void upsertUrl(String url, long artistId, long discordId) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.upsertUrl(connection, url, artistId, discordId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }

    }

    public void upsertSpotify(String url, long artistId, long discordId, String spotifyId) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.updateUrlStatus(connection, artistId, spotifyId);
            if (url != null && !url.isBlank()) {
                updaterDao.upsertSpotify(connection, url, artistId, discordId);
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void upsertSpotify(String url, long artistId, String spotifyId) {
        this.upsertSpotify(url, artistId, 537353774205894676L, spotifyId);
    }

    public void addLogo(long guildId, BufferedImage in) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.addLogo(connection, guildId, in);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }

    }

    public void removeLogo(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.removeLogo(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }

    }

    public InputStream findLogo(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            return userGuildDao.findLogo(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }

    }

    public long getDiscordIdFromLastfm(String fmName, long guildId) throws InstanceNotFoundException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return userGuildDao.getDiscordIdFromLastFm(connection, fmName, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }
    }

    public long getDiscordIdFromLastfm(String lasFmName) throws InstanceNotFoundException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return userGuildDao.getDiscordIdFromLastFm(connection, lasFmName);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }
    }

    public int getArtistPlays(long artistId, String whom) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.userPlays(connection, artistId, whom);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }
    }

    public List<LbEntry<Integer>> getGuildCrownLb(long guildId, int threshold) {

        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.crownsLeaderboard(connection, guildId, threshold);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }
    }

    public void removeUserFromOneGuildConsequent(long discordID, long guildID) {
        removeFromGuild(discordID, guildID);
        try (Connection connection = dataSource.getConnection()) {
            List<Long> servers = userGuildDao.guildsFromUser(connection, discordID);
            if (servers.isEmpty()) {
                removeUserCompletely(discordID);
                logger.info("No longer sharing any server with user {}:  removing user", discordID);
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void removeUserCompletely(Long discordID) {

        try (Connection connection = dataSource.getConnection()) {
            try {
                /* Prepare connection. */

                connection.setAutoCommit(false);
                /* Do work. */
                userGuildDao.removeUser(connection, discordID);
                /* Commit. */
                connection.commit();
                logger.info("Deleted User {} :At  {} ", discordID, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
            } catch (SQLException e) {
                connection.rollback();
                throw new ChuuServiceException(e);
            } catch (RuntimeException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    private void removeFromGuild(Long discordID, long guildID) {

        try (Connection connection = dataSource.getConnection()) {
            try {
                /* Prepare connection. */

                connection.setAutoCommit(false);
                /* Do work. */
                userGuildDao.removeUserGuild(connection, discordID, guildID);
                /* Commit. */
                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                throw new ChuuServiceException(e);
            } catch (RuntimeException | Error e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<Long> getUserGuilds(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return userGuildDao.guildsFromUser(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }
    }

    public MultiValuedMap<Long, Long> getMapGuildUsers() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return userGuildDao.getWholeUserGuild(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }
    }


    public UpdaterStatus getUpdaterStatusByName(String artist) throws InstanceNotFoundException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return updaterDao.getUpdaterStatus(connection, artist);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }

    }

    public void updateImageStatus(long artistId, String url, boolean updateBir) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.updateStatusBit(connection, artistId, updateBir, url);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public void insertCorrection(long artistId, String correction) {

        try (Connection connection = dataSource.getConnection()) {
            updaterDao.insertCorrection(connection, artistId, correction);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public String findCorrection(String artist_id) {
        try (Connection connection = dataSource.getConnection()) {
            return updaterDao.findCorrection(connection, artist_id);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }
    }


    public void updateMetric(Metrics metrics, long incrementalNewValue) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.updateMetric(connection, metrics.getMetricId(), incrementalNewValue);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }
    }

    public void updateMetrics(long value1, long value2, long value3, long value4) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.updateMetric(connection, Metrics.AOTY_DISCOGS.getMetricId(), value1);
            updaterDao.updateMetric(connection, Metrics.AOTY_MB_NAME.getMetricId(), value2);
            updaterDao.updateMetric(connection, Metrics.AOTY_TOTAL.getMetricId(), value3);
            updaterDao.updateMetric(connection, Metrics.REQUESTED.getMetricId(), value4);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }
    }

    public List<LbEntry<Integer>> getUniqueLeaderboard(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.uniqueLeaderboard(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }
    }

    public List<LbEntry<Integer>> getUniqueAlbumLeaderboard(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.uniqueAlbumLeaderboard(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }
    }

    public List<LbEntry<Integer>> getUniqueSongLeaderboard(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.uniqueSongLeaderboard(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }
    }

    public int getUserArtistCount(String lastfmId, int threshold) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.userArtistCount(connection, lastfmId, threshold);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public int getUserAlbumCount(String lastfmId, int threshold) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.userAlbumCount(connection, lastfmId, threshold);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public int getUserTrackCount(String lastfmId, int threshold) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.userTrackCount(connection, lastfmId, threshold);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<LbEntry<Integer>> getArtistLeaderboard(long guildId, int threshold) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.artistLeaderboard(connection, guildId, threshold);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }
    }

    public List<LbEntry<Integer>> getAlbumLeaderboard(long guildId, int threshold) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.albumLeaderboard(connection, guildId, threshold);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }
    }

    public List<LbEntry<Integer>> getTrackLeaderboard(long guildId, int threshold) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.trackLeaderboard(connection, guildId, threshold);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }
    }

    public List<LbEntry<Double>> getObscurityRankings(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.obscurityLeaderboard(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public PresenceInfo getRandomArtistWithUrl() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getRandomArtistWithUrl(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public int randomCount(@Nullable Long userId) {
        try (Connection connection = dataSource.getConnection()) {
            return (queriesDao.getRandomCount(connection, userId));
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public @Nullable
    RandomUrlEntity findRandomUrl(String url) {
        try (Connection connection = dataSource.getConnection()) {
            return updaterDao.findRandomUrlById(connection, url);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public @Nullable
    RandomUrlDetails findRandomUrlDetails(String url) {
        try (Connection connection = dataSource.getConnection()) {
            return updaterDao.randomUrlDetails(connection, url);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public boolean randomUrlExists(String url) {
        try (Connection connection = dataSource.getConnection()) {
            return (updaterDao.findRandomUrlById(connection, url) != null);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public boolean addToRandomPool(RandomUrlEntity randomUrlEntity) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            if (updaterDao.findRandomUrlById(connection, randomUrlEntity.url()) == null) {
                updaterDao.insertRandomUrl(connection, randomUrlEntity.url(), randomUrlEntity
                        .discordId(), -1L);
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public RandomUrlEntity getRandomUrl(@Nullable RandomTarget randomTarget) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return updaterDao.getRandomUrl(connection, randomTarget);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public RandomUrlEntity getRandomUrlFromServer(long guildId, @Nullable RandomTarget randomTarget) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return updaterDao.getRandomUrlFromServer(connection, guildId, randomTarget);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public StolenCrownWrapper getCrownsStolenBy(String ogUser, String queriedUser, long guildId, int threshold) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getCrownsStolenBy(connection, ogUser, queriedUser, guildId, threshold);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public StolenCrownWrapper getArtistsBehind(String ogUser, String queriedUser, int threshold) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.artistsBehind(connection, ogUser, queriedUser, threshold);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public UniqueWrapper<AlbumPlays> getUserAlbumCrowns(String lastfmId, long guildId, int crownthreshold) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getUserAlbumCrowns(connection, lastfmId, crownthreshold, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public UniqueWrapper<TrackPlays> getUserTrackCrowns(String lastfmId, long guildId, int crownthreshold) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getUserTrackCrowns(connection, lastfmId, crownthreshold, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<LbEntry<Integer>> albumCrownsLeaderboard(long guildId, int threshold) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.albumCrownsLeaderboard(connection, guildId, threshold);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<LbEntry<Integer>> trackCrownsLeaderboard(long guildId, int threshold) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.trackCrownsLeaderboard(connection, guildId, threshold);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void insertAlbumCrown(long artistId, String album, long discordID, long guildId, int plays) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.insertAlbumCrown(connection, artistId, album, discordID, guildId, plays);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void deleteAlbumCrown(String artist, String album, long discordID, long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.deleteAlbumCrown(connection, artist, album, discordID, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public Map<Long, Character> getGuildPrefixes(Character defaultPrefix) {
        try (Connection connection = dataSource.getConnection()) {
            return updaterDao.getGuildPrefixes(connection, defaultPrefix);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void addGuildPrefix(Map<Long, Character> prefixMap, long guildID, Character prefix) {
        try (Connection connection = dataSource.getConnection()) {
            if (prefixMap.containsKey(guildID)) {
                userGuildDao.createGuild(connection, guildID);
            }
            updaterDao.upsertGuildPrefix(connection, guildID, prefix);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public ObscuritySummary getObscuritySummary(String lastfmid) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getUserObscuritPoints(connection, lastfmid);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public void truncateRandomPool() {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.truncateRandomPool(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void deleteRandomUrl(String url) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.deleteRandomUrl(connection, url);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public List<GlobalCrown> getGlobalArtistRanking(Long artistId, boolean includeBottedUsers, long ownerId) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getGlobalKnows(connection, artistId, includeBottedUsers, ownerId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<GlobalCrown> getGlobalAlbumRanking(Long albumId, boolean includeBottedUsers, long ownerId) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getGlobalRankingAlbum(connection, albumId, includeBottedUsers, ownerId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<GlobalCrown> getGlobalTrackRanking(Long trackId, boolean includeBottedUsers, long ownerId) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getGlobalRankingSong(connection, trackId, includeBottedUsers, ownerId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public UniqueWrapper<ArtistPlays> getGlobalUniques(String lastfmid) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getGlobalUniques(connection, lastfmid);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public UniqueWrapper<AlbumPlays> getGlobalAlbumUniques(String lastfmid) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getGlobalAlbumUniques(connection, lastfmid);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public ScrobbledTrack getTrackInfo(String lastfmid, long trackId) {
        try (Connection connection = dataSource.getConnection()) {
            return trackDao.getUserTrackInfo(connection, lastfmid, trackId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public UniqueWrapper<TrackPlays> getGlobalTrackUniques(String lastfmid) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getGlobalTrackUniques(connection, lastfmid);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public UniqueWrapper<ArtistPlays> getGlobalCrowns(String lastfmid, int threshold, boolean includeBottedUsers,
                                                      long ownerId) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getGlobalCrowns(connection, lastfmid, threshold, includeBottedUsers, ownerId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public UniqueWrapper<ArtistPlays> getGlobalAlbumCrowns(String lastfmid, int threshold, boolean includeBottedUsers,
                                                           long ownerId) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getGlobalAlbumCrowns(connection, lastfmid, threshold, includeBottedUsers, ownerId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public UniqueWrapper<ArtistPlays> getGlobalTrackCrowns(String lastfmid, int threshold, boolean includeBottedUsers, long ownerId) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getGlobalTrackCrowns(connection, lastfmid, threshold, includeBottedUsers, ownerId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public void createGuild(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.createGuild(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public long findByNameConsequent(String artistName) {
        try (Connection connection = dataSource.getConnection()) {
            try {
                return updaterDao.getArtistId(connection, artistName);
            } catch (InstanceNotFoundException e) {
                ScrobbledArtist scrobbledArtist = new ScrobbledArtist(artistName, 0, null);
                updaterDao.insertArtistSad(connection, (scrobbledArtist));
                return scrobbledArtist.getArtistId();
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public UpdaterUserWrapper getUserUpdateStatus(long discordId) throws InstanceNotFoundException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return updaterDao.getUserUpdateStatus(connection, discordId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public ResultWrapper<ArtistPlays> getArtistsFrequencies(long guildID) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getArtistsFrequencies(connection, guildID);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<TagPlays> getServerTags(long guildID, boolean doCount) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getServerTags(connection, guildID, doCount);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public long getArtistFrequencies(long guildID, long artistId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getArtistFrequencies(connection, guildID, artistId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public long getGlobalArtistFrequencies(long artistId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getArtistFrequencies(connection, null, artistId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public long getAlbumFrequencies(long guildID, long albumId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getAlbumFrequencies(connection, guildID, albumId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public long getSongFrequencies(long guildID, long trackid) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getSongFrequencies(connection, guildID, trackid);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public ResultWrapper<ArtistPlays> getArtistsFrequenciesGlobal() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getGlobalArtistFrequencies((connection));
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public long getGlobalArtistPlays(long artistId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getArtistPlays(connection, null, artistId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public long getServerArtistPlays(long guildID, long artistId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getArtistPlays(connection, guildID, artistId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public long getServerAlbumPlays(long guildID, long albumId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getAlbumPlays(connection, guildID, albumId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public long getServerTrackPlays(long guildID, long trackId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getSongPlays(connection, guildID, trackId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public ResultWrapper<ArtistPlays> getServerArtistsPlays(long guildID) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getArtistPlayCount(connection, guildID);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public ResultWrapper<ArtistPlays> getArtistPlayCountGlobal() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getGlobalArtistPlayCount((connection));
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<ScrobbledArtist> getAllUserArtist(long discordId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getAllUsersArtist(connection, discordId, null);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<ScrobbledArtist> getAllUserArtist(long discordId, int limit) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getAllUsersArtist(connection, discordId, limit);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void changeLastFMName(long userId, String lastFmID) throws
            DuplicateInstanceException, InstanceNotFoundException {
        try (Connection connection = dataSource.getConnection()) {
            try {
                LastFMData lastFMData = userGuildDao.findByLastFMId(connection, lastFmID);
                throw new DuplicateInstanceException(lastFmID);
            } catch (InstanceNotFoundException ignored) {
            }
            connection.setAutoCommit(true);
            updaterDao.deleteAllArtists(connection, lastFmID);
            updaterDao.clearSess(connection, lastFmID);
            LastFMData lastFmData = userGuildDao.findLastFmData(connection, userId);
            lastFmData.setName(lastFmID);
            userGuildDao.updateLastFmData(connection, lastFmData);
            String name = lastFmData.getName();
            boolean flagged = userGuildDao.isFlagged(connection, name);
            if (flagged) {
                userGuildDao.flagBottedDB(lastFmID, connection);
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public void changeDiscordId(long userId, String lastFmID) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.changeDiscordId(connection, userId, lastFmID);
            boolean flagged = userGuildDao.isFlagged(connection, lastFmID);
            if (flagged) {
                userGuildDao.flagBottedDB(lastFmID, connection);
            }

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public LastFMData findByLastfmName(String lastFmID) throws InstanceNotFoundException {
        try (Connection connection = dataSource.getConnection()) {
            return userGuildDao.findByLastFMId(connection, lastFmID);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<LbEntry<Integer>> matchingArtistsCount(String lastFmId, long guildId, int extraParam) {
        try (Connection connection = dataSource.getConnection()) {
            affinityDao.setServerTempTable(connection, guildId, lastFmId, extraParam);
            List<LbEntry<Integer>> matchingCount = affinityDao.getMatchingCount(connection);
            matchingCount.sort(Comparator.comparingInt((ToIntFunction<LbEntry<Integer>>) LbEntry::getEntryCount).reversed());
            affinityDao.cleanUp(connection, true);
            return matchingCount;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<ArtistLbGlobalEntry> globalMatchings(String lastFmId, Long guildId, int theshold) {
        try (Connection connection = dataSource.getConnection()) {
            affinityDao.setGlobalTable(connection, lastFmId, theshold);
            List<ArtistLbGlobalEntry> matchingCount = affinityDao.getGlobalMatchingCount(connection);
            matchingCount.sort(Comparator.comparingInt((ToIntFunction<LbEntry<Integer>>) LbEntry::getEntryCount).reversed());
            affinityDao.cleanUpGlobal(connection, true);
            return matchingCount;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void addAlias(String alias, long toArtist) throws DuplicateInstanceException {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.addAlias(connection, alias, toArtist);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void enqueAlias(String alias, long toArtist, long whom) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.queueAlias(connection, alias, toArtist, whom);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public AliasEntity getNextInAliasQueue() {
        try (Connection connection = dataSource.getConnection()) {
            return updaterDao.getNextInAliasQueue(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void deleteAliasById(long aliasId) throws InstanceNotFoundException {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.deleteAliasById(connection, aliasId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public long getArtistId(String artist) throws InstanceNotFoundException {
        try (Connection connection = dataSource.getConnection()) {
            return updaterDao.getArtistId(connection, artist);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public OptionalLong checkArtistUrlExists(long artistId, String urlParsed) {
        try (Connection connection = dataSource.getConnection()) {
            return updaterDao.checkArtistUrlExists(connection, artistId, urlParsed);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public OptionalLong checkQueuedUrlExists(long artistId, String urlParsed) {
        try (Connection connection = dataSource.getConnection()) {
            return updaterDao.checkQueuedUrlExists(connection, artistId, urlParsed);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public VoteStatus castVote(long urlId, long discordId, boolean isPositive) {
        try (Connection connection = dataSource.getConnection()) {
            Boolean hasVotedBefore = queriesDao.hasUserVotedImage(connection, urlId, discordId);
            updaterDao.castVote(connection, urlId, discordId, isPositive);
            if (hasVotedBefore == null) {
                return VoteStatus.NEW_VOTE;
            }
            return hasVotedBefore == isPositive ? VoteStatus.SAME_VALUE : VoteStatus.CHANGE_VALUE;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<VotingEntity> getAllArtistImages(long artist_id) {

        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getAllArtistImages(connection, artist_id);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void report(long urlId, long userIdLong) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.reportImage(connection, urlId, userIdLong);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<String> getArtistAliases(long artistId) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getArtistAliases(connection, artistId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void upsertArtistSad(ScrobbledArtist scrobbledArtist) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.insertArtistSad(connection, scrobbledArtist);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public void removeReportedImage(long alt_id, long image_owner, long mod_id) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.removeImage(connection, alt_id);
            updaterDao.logRemovedImage(connection, image_owner, mod_id);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public int getReportCount() {
        try (Connection connection = dataSource.getConnection()) {
            return updaterDao.getReportCount(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public int getQueueUrlCount() {
        try (Connection connection = dataSource.getConnection()) {
            return updaterDao.getQueueUrlCount(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void ignoreReportedImage(long altId) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.removeReport(connection, altId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void rejectQueuedImage(long queuedId, ImageQueue reportEntity) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.storeRejected(connection, reportEntity);
            updaterDao.removeQueuedImage(connection, queuedId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public ReportEntity getNextReport(Long maxId, Set<Long> skippedIds) {
        try (Connection connection = dataSource.getConnection()) {
            return updaterDao.getReportEntity(connection, maxId, skippedIds);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public List<ImageQueue> getNextQueue() {
        try (Connection connection = dataSource.getConnection()) {
            return updaterDao.getUrlQueue(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public ImageQueue getNextQueue(Long maxId, Set<Long> skippedIds) {
        try (Connection connection = dataSource.getConnection()) {
            return updaterDao.getUrlQueue(connection, maxId, skippedIds);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public Affinity getAffinity(String ogLastFmID, String receiverLastfmID, long threshold) {
        try (Connection connection = dataSource.getConnection()) {
            affinityDao.initTempTable(connection, ogLastFmID, receiverLastfmID, threshold);
            Affinity affinity = affinityDao.getPercentageStats(connection, ogLastFmID, receiverLastfmID, threshold);
            String[] recs = affinityDao.doRecommendations(connection, ogLastFmID, receiverLastfmID);
            affinity.setReceivingRec(recs[0]);
            affinity.setOgRec(recs[1]);
            ResultWrapper<UserArtistComparison> similar = queriesDao.similar(connection, List.of(ogLastFmID, receiverLastfmID), 5);
            affinity.addMatchings(similar.getResultList());
            affinityDao.cleanUp(connection, false);
            return affinity;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }

    }

    public List<Affinity> getServerAffinity(String ogLastFmID, long guildId, long threshold) {
        try (Connection connection = dataSource.getConnection()) {
            affinityDao.setServerTempTable(connection, guildId, ogLastFmID, threshold);
            List<Affinity> affinityList = affinityDao.doServerAffinity(connection, ogLastFmID, threshold);
            affinityDao.cleanUp(connection, true);
            return affinityList;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }
    }

    public List<GlobalAffinity> getGlobalAffinity(String ogLastFmID, int threshold) {
        try (Connection connection = dataSource.getConnection()) {
            affinityDao.setGlobalTable(connection, ogLastFmID, threshold);
            List<GlobalAffinity> affinityList = affinityDao.doGlobalAffinity(connection, ogLastFmID, threshold);
            affinityDao.cleanUpGlobal(connection, true);
            return affinityList;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }
    }

    public List<ScrobbledArtist> getRecommendation(long giverDiscordId, long receiverDiscordId,
                                                   boolean ignorePast, int limit) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getRecommendations(connection, giverDiscordId, receiverDiscordId, ignorePast, limit);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }
    }

    public void insertRecommendation(long secondDiscordID, long firstDiscordID, long artistId) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.insertPastRecommendation(connection, secondDiscordID, firstDiscordID, artistId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);

        }

    }

    public void updateGuildCrownThreshold(long guildId, int newThreshold) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.updateGuildCrownThreshold(connection, guildId, newThreshold);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public List<LbEntry<Integer>> getScrobblesLeaderboard(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getScrobblesLeaderboard(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<CrownableArtist> getCrownable(Long discordId, Long guildId, boolean skipCrownws, boolean onlySecond,
                                              int crownDistance) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getCrownable(connection, discordId, guildId, skipCrownws, onlySecond, crownDistance);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public Map<Long, Float> getRateLimited() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getRateLimited(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void removeRateLimit(long discordId) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.removeRateLimit(connection, discordId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void addRateLimit(long discordId, float queries_second) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.upsertRateLimit(connection, discordId, queries_second);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void setPrivateUpdate(long discordId, boolean privateUpdate) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setUserProperty(connection, discordId, "private_update", privateUpdate);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void setOwnTags(long discordId, boolean ownTags) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setUserProperty(connection, discordId, "own_tags", ownTags);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void setEmbedColor(long discordId, @Nullable EmbedColor embedColor) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setUserProperty(connection, discordId, "color", embedColor == null ? null : embedColor.toString());
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void setImageNotify(long discordId, boolean imageNotify) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setUserProperty(connection, discordId, "notify_image", imageNotify);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void setRatingNotify(long discordId, boolean ratingNotify) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setUserProperty(connection, discordId, "notify_rating", ratingNotify);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void setChartEmbed(long discordId, @Nonnull ChartMode chartMode) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setUserProperty(connection, discordId, "chart_mode", chartMode);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void insertServerDisabled(long discordId, String commandName) {
        try (Connection connection = dataSource.getConnection()) {
            this.userGuildDao.insertServerDisabled(connection, discordId, commandName);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void insertChannelCommandStatus(long discordId, long channelId, String commandName, boolean enabled) {
        try (Connection connection = dataSource.getConnection()) {
            this.userGuildDao.insertChannelCommandStatus(connection, discordId, channelId, commandName, enabled);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void deleteServerCommandStatus(long discordId, String commandName) {
        try (Connection connection = dataSource.getConnection()) {
            this.userGuildDao.deleteServerCommandStatus(connection, discordId, commandName);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void deleteChannelCommandStatus(long discordId, long channelId, String commandName) {
        try (Connection connection = dataSource.getConnection()) {
            this.userGuildDao.deleteChannelCommandStatus(connection, discordId, channelId, commandName);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public MultiValuedMap<Long, String> initServerCommandStatuses() {
        try (Connection connection = dataSource.getConnection()) {
            return this.userGuildDao.initServerCommandStatuses(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public MultiValuedMap<Pair<Long, Long>, String> initServerChannelsCommandStatuses(boolean enabled) {
        try (Connection connection = dataSource.getConnection()) {
            return this.userGuildDao.initServerChannelsCommandStatuses(connection, enabled);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public long acceptImageQueue(long queuedId, String url, long artistId, long discordId) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.removeQueuedImage(connection, queuedId);
            long urlId = updaterDao.upsertUrl(connection, url, artistId, discordId);
            updaterDao.castVote(connection, urlId, discordId, true);
            return urlId;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public GuildProperties getGuildProperties(long guildId) throws InstanceNotFoundException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return userGuildDao.getGuild(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public void setWhoknowsMode(long discordId, @Nonnull WhoKnowsMode whoKnowsMode) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setUserProperty(connection, discordId, "whoknows_mode", whoKnowsMode);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public void setRemainingImagesMode(long discordId, @Nonnull RemainingImagesMode remainingImagesMode) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setUserProperty(connection, discordId, "remaining_mode", remainingImagesMode);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public void setPrivacyMode(long discordId, @Nonnull PrivacyMode privacyMode) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setUserProperty(connection, discordId, "privacy_mode", privacyMode);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public void setRemainingImagesModeServer(long guildId, @Nullable RemainingImagesMode remainingImagesMode) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setGuildProperty(connection, guildId, "remaining_mode", remainingImagesMode);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void setServerWhoknowMode(long guildId, @Nullable WhoKnowsMode images) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setGuildProperty(connection, guildId, "whoknows_mode", images);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void setServerColorMode(long guildId, @Nullable EmbedColor color) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setGuildProperty(connection, guildId, "color", color == null ? null : color.toString());
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void setServerChartMode(long guildId, @Nullable ChartMode chartMode) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setGuildProperty(connection, guildId, "chart_mode", chartMode);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void setServerDeleteMessage(long guildId, boolean deleteMode) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setGuildProperty(connection, guildId, "delete_message", deleteMode);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void setSetOnJoin(long guildId, boolean setOnJoin) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setGuildProperty(connection, guildId, "set_on_join", setOnJoin);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void setServerShowDisabledWarning(long guildId, boolean disabledWarning) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setGuildProperty(connection, guildId, "disabled_warning", disabledWarning);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void setServerAllowCovers(long guildId, boolean allowCovers) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setGuildProperty(connection, guildId, "allow_covers", allowCovers);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void setServerAllowReactions(long guildId, boolean allowReactions) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setGuildProperty(connection, guildId, "allow_reactions", allowReactions);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void setServerOverrideReactions(long guildId, OverrideMode overrideReactions) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setGuildProperty(connection, guildId, "override_reactions", overrideReactions);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void setServerColorOverride(long guildId, OverrideColorMode overrideReactions) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setGuildProperty(connection, guildId, "override_color", overrideReactions);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void setServer(long guildId, boolean deleteMode) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setGuildProperty(connection, guildId, "delete_message", deleteMode);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void setChartDefaults(int x, int y, long discordId) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setChartDefaults(connection, discordId, x, y);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void insertRatings(long userId, List<RYMImportRating> ratings) {
        try (Connection connection = dataSource.getConnection()) {


            /* Prepare connection. */

            connection.setAutoCommit(false);
            if (!ratings.isEmpty()) {
                //connection.prepareStatement("set foreign_key_checks  = 0").execute();
                rymDao.cleanUp(connection);
                //delete everything first to have a clean start
                /* Do work. */
                updaterDao.fillALbumsByRYMID(connection, ratings);
                Map<Boolean, List<RYMImportRating>> map = ratings.stream().collect(Collectors.partitioningBy(albumRating -> albumRating.getId() == -1L));
                List<RYMImportRating> knownAlbums = map.get(false);
                List<RYMImportRating> unknownAlbums = map.get(true);
                if (!unknownAlbums.isEmpty()) {
                    rymDao.setServerTempTable(connection, unknownAlbums);
                    //Returns a map with rym_id -> artist_id


                    Map<Long, Long> artists = new HashMap<>(ratings.size());

                    Map<Long, Long> artistsByLocalizedJoinedNames = rymDao.findArtistsByLocalizedJoinedNames(connection);
                    addToMap(connection, artists, artistsByLocalizedJoinedNames);

                    Map<Long, Long> artistsByLocalizedNames = rymDao.findArtistsByLocalizedNames(connection);
                    addToMap(connection, artists, artistsByLocalizedNames);

                    Map<Long, Long> artistsByJoinedNames = rymDao.findArtistsByJoinedNames(connection);
                    addToMap(connection, artists, artistsByJoinedNames);

                    Map<Long, Long> artistsByNames = rymDao.findArtistsByNames(connection);
                    addToMap(connection, artists, artistsByNames);


                    Map<Boolean, List<RYMImportRating>> map1 = unknownAlbums.stream().collect(Collectors.partitioningBy(x -> {
                        Long aLong = artists.get(x.getRYMid());
                        return aLong != null && aLong > 0;
                    }));

                    // List of artist that were found in last step
                    List<RYMImportRating> ratingsWithKnownArtist = map1.get(true);
                    //We set the found id
                    for (RYMImportRating RYMImportRating : ratingsWithKnownArtist) {
                        Long aLong = artists.get(RYMImportRating.getRYMid());
                        RYMImportRating.setArtist_id(aLong);
                    }
                    //This were not found
                    List<RYMImportRating> ratingsWithUnknownArtist = map1.get(false);

                    //This were the ids that were found, we reduce a bit the size of the table
                    Collection<Long> rymIdsToDelete = artists.keySet();
                    rymDao.deletePartialTempTable(connection, Set.copyOf(rymIdsToDelete));

                    //Over the remaining items we do an auxiliar search
                    Map<Long, Long> artistsAuxiliar = rymDao.findArtistsAuxiliar(connection);
                    map1 = ratingsWithUnknownArtist.stream().collect(Collectors.partitioningBy(x -> {
                        Long aLong = artistsAuxiliar.get(x.getRYMid());
                        return aLong != null && aLong > 0;
                    }));

                    //These were found on the auxiliar search
                    List<RYMImportRating> auxiliarFoundArtists = map1.get(true);
                    List<RYMImportRating> notFoundAuxiiliar = map1.get(false);
                    for (RYMImportRating RYMImportRating : auxiliarFoundArtists) {
                        Long aLong = artistsAuxiliar.get(RYMImportRating.getRYMid());
                        RYMImportRating.setArtist_id(aLong);
                    }

                    ratingsWithUnknownArtist.addAll(notFoundAuxiiliar);
                    ratingsWithKnownArtist.addAll(auxiliarFoundArtists);


                    for (RYMImportRating RYMImportRating : ratingsWithUnknownArtist) {
                        ScrobbledArtist scrobbledArtist = new ScrobbledArtist(RYMImportRating.getFirstName() + " " + RYMImportRating.getLastName(), 0, null);
                        updaterDao.insertArtistSad(connection, scrobbledArtist);
                        RYMImportRating.setArtist_id(scrobbledArtist.getArtistId());
                    }
                    //KnownAlbumvs vs Ratings WithKnownArtist
                    // Now we have on ratingsw with known artists all ratings with unknown album
                    ratingsWithKnownArtist.addAll(ratingsWithUnknownArtist);

                    for (RYMImportRating RYMImportRating : ratingsWithKnownArtist) {
                        updaterDao.insertAlbumSad(connection, RYMImportRating);
                    }

                    knownAlbums.addAll(ratingsWithKnownArtist);
                    knownAlbums = knownAlbums.stream()
                            .collect(Collectors.groupingBy(RYMImportRating::getId, Collectors.toList())).values().stream()
                            .map(rymImportRatings -> rymImportRatings.stream().max(Comparator.comparingInt(RYMImportRating::getRating)).orElse(null))
                            .filter(Objects::nonNull).toList();
                }

                updaterDao.deleteAllRatings(connection, userId);
                Savepoint savepoint = connection.setSavepoint();
                try {
                    rymDao.insertRatings(connection, knownAlbums, userId);
                    connection.commit();
                } catch (SQLTransactionRollbackException exception) {
                    connection.rollback(savepoint);
                    rymDao.insertRatings(connection, knownAlbums, userId);

                    connection.commit();
                }
                //connection.rollback();

            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    private void addToMap(Connection connection, Map<Long, Long> artists, Map<Long, Long> artistsByLocalizedNames) {
        artists.putAll(artistsByLocalizedNames.entrySet().stream().filter(x -> x.getValue() != 1).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        rymDao.deletePartialTempTable(connection, artistsByLocalizedNames.entrySet().stream().filter(x -> x.getValue() != 1).map(Map.Entry::getKey).collect(Collectors.toSet()));
    }

    public AlbumRatings getRatingsByName(long guildId, String album, long artistId) {

        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return rymDao.getRatingsByName(connection, guildId, album, artistId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public Collection<AlbumRatings> getArtistRatings(long artistId, long guildId) {

        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return rymDao.getArtistRatings(connection, guildId, artistId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<ScoredAlbumRatings> getGlobalTopRatings() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return rymDao.getGlobalTopRatings(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<ScoredAlbumRatings> getServerTopRatings(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return rymDao.getServerTopRatings(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<ScoredAlbumRatings> getSelfRatingsScore(long discordId, Short ratingNumber) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return rymDao.getSelfRatingsScore(connection, ratingNumber, discordId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public RymStats getUserRymStatms(long discordId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return rymDao.getUserRymStatms(connection, discordId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public RymStats getRYMServerStats(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return rymDao.getServerStats(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public RYMAlbumStats getServerAlbumStats(long guildId, long artistId, long albumId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return rymDao.getServerAlbumStats(connection, guildId, artistId, albumId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public RymStats getRYMBotStats() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return rymDao.getRYMBotStats(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void insertAlbum(ScrobbledAlbum scrobbledAlbum) {
        try (Connection connection = dataSource.getConnection()) {
            albumDao.insertLastFmAlbum(connection, scrobbledAlbum);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void albumUpdate(List<ScrobbledAlbum> list, List<ScrobbledArtist> artistData, String id) {
        try (Connection connection = dataSource.getConnection()) {

            try {
                /* Prepare connection. */
                connection.setAutoCommit(true);

                if (!list.isEmpty()) {
                    updaterDao.fillIds(connection, artistData);
                    Map<String, Long> artistIds = artistData.stream().collect(Collectors.toMap(ScrobbledArtist::getArtist, ScrobbledArtist::getArtistId, (a, b) -> {
                        assert a.equals(b);
                        return a;
                    }));
                    list.forEach(x -> {
                        Long artistId = artistIds.get(x.getArtist());
                        artistId = handleNonExistingArtistFromAlbum(connection, x, artistId);
                        x.setArtistId(artistId);
                    });
                    connection.setAutoCommit(false);
                    list = list.stream().filter(x -> x.getAlbum() != null && !x.getAlbum().isBlank()).toList();

                    insertAlbums(list, id, connection);
                }
                updaterDao.setUpdatedTime(connection, id, null, null);
                connection.commit();
            } catch (SQLException e) {
                throw new ChuuServiceException(e);
            }
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    private void insertAlbums(List<ScrobbledAlbum> list, String id, Connection connection) throws SQLException {
        insertAlbums(list, id, connection, true);
    }

    private void insertAlbums(List<ScrobbledAlbum> list, String id, Connection connection, boolean doDeletion) throws SQLException {
        if (list.isEmpty()) {
            return;
        }
        albumDao.fillIds(connection, list);

        Map<Boolean, List<ScrobbledAlbum>> map = list.stream().peek(x -> x.setDiscordID(id)).collect(Collectors.partitioningBy(scrobbledArtist -> scrobbledArtist.getAlbumId() == -1));
        List<ScrobbledAlbum> nonExistingId = map.get(true);
        connection.setAutoCommit(true);
        if (!nonExistingId.isEmpty()) {
            nonExistingId.forEach(x -> {
                albumDao.insertLastFmAlbum(connection, x);
                if (x.getAlbumId() == -1) {
                    handleNonExistingArtistFromAlbum(connection, x, null);
                }
            });
            //updaterDao.insertArtists(connection, nonExistingId);
        }
        List<ScrobbledAlbum> scrobbledAlbums = map.get(false);
        scrobbledAlbums.addAll(nonExistingId);
        connection.setAutoCommit(false);
        connection.commit();
        if (doDeletion) {
            albumDao.deleteAllUserAlbums(connection, id);
        }
        Map<Boolean, List<ScrobbledAlbum>> whyDoesThisNotHaveAnId = scrobbledAlbums.stream().collect(Collectors.partitioningBy(x -> x.getAlbumId() >= 1 && x.getArtistId() >= 1));
        whyDoesThisNotHaveAnId.get(false).forEach(x -> logger.warn(String.format("%s %s caused a foreign key for user %s", x.getAlbum(), x.getArtist(), id)));
        List<ScrobbledAlbum> finalTruer = whyDoesThisNotHaveAnId.get(true);
        if (!finalTruer.isEmpty()) {
            albumDao.addSrobbledAlbums(connection, finalTruer);
        }
    }

    private void insertTracks(List<ScrobbledTrack> list, String id, Connection connection) throws SQLException {
        insertTracks(list, id, connection, true);
    }

    private void insertTracks(List<ScrobbledTrack> list, String id, Connection connection, boolean doDeletion) throws SQLException {
        if (list.isEmpty()) {
            return;
        }

//        trackDao.fillIdsMbids(connection, list.stream().filter(x -> x.getMbid() != null && !x.getMbid().isBlank()).collect(Collectors.toList()));

        trackDao.fillIds(connection, list.stream().filter(x -> x.getTrackId() == -1L).toList());

        Map<Boolean, List<ScrobbledTrack>> map = list.stream().peek(x -> x.setDiscordID(id)).collect(Collectors.partitioningBy(scrobbledArtist -> scrobbledArtist.getTrackId() == -1));
        List<ScrobbledTrack> nonExistingId = map.get(true);
        connection.setAutoCommit(true);
        if (!nonExistingId.isEmpty()) {
            nonExistingId.forEach(x -> {
                trackDao.insertTrack(connection, x);
                if (x.getTrackId() == -1) {
                    handleNonExistingArtistFromAlbum(connection, x, null);
                }
            });
            //updaterDao.insertArtists(connection, nonExistingId);
        }
        List<ScrobbledTrack> scrobbledTracks = map.get(false);
        scrobbledTracks.addAll(nonExistingId);
        connection.setAutoCommit(false);
        connection.commit();
        if (doDeletion) {
            trackDao.deleteAllUserTracks(connection, id);
        }
        Map<Boolean, List<ScrobbledTrack>> whyDoesThisNotHaveAnId = scrobbledTracks.stream().collect(Collectors.partitioningBy(x -> x.getTrackId() >= 1 && x.getArtistId() >= 1));
        whyDoesThisNotHaveAnId.get(false).forEach(x -> logger.warn(String.format("%s %s caused a foreign key for user %s", x.getAlbum(), x.getArtist(), id)));
        List<ScrobbledTrack> finalTruer = whyDoesThisNotHaveAnId.get(true);
        if (!finalTruer.isEmpty())
            trackDao.addSrobbledTracks(connection, finalTruer);
        connection.commit();
    }

    public WrapperReturnNowPlaying getWhoKnowsAlbums(int limit, long albumId, long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.whoKnowsAlbum(connection, albumId, guildId, limit);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public WrapperReturnNowPlaying getWhoKnowsTrack(int limit, long trackId, long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.whoKnowsTrack(connection, trackId, guildId, limit);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public WrapperReturnNowPlaying getGlobalWhoKnowsAlbum(int limit, long albumId, long ownerId, boolean includeBotted, boolean hidePrivate) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.globalWhoKnowsAlbum(connection, albumId, limit, ownerId, includeBotted, hidePrivate);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public WrapperReturnNowPlaying getGlobalWhoKnowsTrack(int limit, long trackId, long ownerId, boolean includeBotted, boolean hidePrivate) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.globalWhoKnowsTrack(connection, trackId, limit, ownerId, includeBotted, hidePrivate);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public UniqueWrapper<AlbumPlays> getUniqueAlbums(long guildId, String lastfmid) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.albumUniques(connection, guildId, lastfmid);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<AlbumPlays> getUnratedAlbums(long discordId) {
        try (Connection connection = dataSource.getConnection()) {
            return rymDao.unratedAlbums(connection, discordId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public List<AlbumUserPlays> getUserTopArtistAlbums(int limit, long artistId, long discord_id) {
        try (Connection connection = dataSource.getConnection()) {
            return albumDao.getUserTopArtistAlbums(connection, discord_id, artistId, limit);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<AlbumUserPlays> getServerTopArtistAlbums(int limit, long artistId, long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            return albumDao.getServerTopArtistAlbums(connection, guildId, artistId, limit);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<AlbumUserPlays> getGlobalTopArtistAlbums(int limit, long artistId) {
        try (Connection connection = dataSource.getConnection()) {
            return albumDao.getGlobalTopArtistAlbums(connection, artistId, limit);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public Album getAlbum(long artistId, String name) throws InstanceNotFoundException {
        try (Connection connection = dataSource.getConnection()) {
            return albumDao.getAlbumByName(connection, name, artistId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public long findAlbumIdByName(long artistId, String name) throws InstanceNotFoundException {
        try (Connection connection = dataSource.getConnection()) {
            return albumDao.getAlbumIdByName(connection, name, artistId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public String findAlbumUrlByName(long artistId, String name) throws InstanceNotFoundException {
        try (Connection connection = dataSource.getConnection()) {
            return albumDao.getAlbumUrlByName(connection, name, artistId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public BotStats getBotStats() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getBotStats(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public ServerStats getServerStats(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return userGuildDao.getServerStats(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public long getUserAlbumCount(long discordId) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getUserAlbumCount(connection, discordId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public Week getCurrentWeekId() {
        try (Connection connection = dataSource.getConnection()) {
            return billboardDao.getCurrentWeekId(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<BillboardEntity> getBillboard(int week_id, long guildId, boolean doListeners) {
        try (Connection connection = dataSource.getConnection()) {
            return billboardDao.getBillboard(connection, week_id, guildId, doListeners);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<BillboardEntity> getArtistBillboard(int week_id, long guildId, boolean doListeners) {
        try (Connection connection = dataSource.getConnection()) {
            return billboardDao.getArtistBillboard(connection, week_id, guildId, doListeners);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<BillboardEntity> getAlbumBillboard(int week_id, long guildId, boolean doListeners) {
        try (Connection connection = dataSource.getConnection()) {
            return billboardDao.getAlbumBillboard(connection, week_id, guildId, doListeners);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void insertBillboardData(int week_id, long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            billboardDao.insertBillboardDataListeners(connection, week_id, guildId);
            billboardDao.insertBillboardDataScrobbles(connection, week_id, guildId);
            billboardDao.insertBillboardDataListenersByArtist(connection, week_id, guildId);
            billboardDao.insertBillboardDataScrobblesByArtist(connection, week_id, guildId);
            billboardDao.insertBillboardDataListenersByAlbum(connection, week_id, guildId);
            billboardDao.insertBillboardDataScrobblesByAlbum(connection, week_id, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public void filldArtistIds(List<ScrobbledArtist> scrobbledArtists) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.fillIds(connection, scrobbledArtists);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<PreBillboardUserData> getUserData(int week_id, String lastfmId) {
        try (Connection connection = dataSource.getConnection()) {
            return billboardDao.getUserData(connection, lastfmId, week_id);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<PreBillboardUserDataTimestamped> getUngroupedUserData(int week_id, String lastfmId) {
        try (Connection connection = dataSource.getConnection()) {
            return billboardDao.getUngroupedUserData(connection, lastfmId, week_id);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public void insertUserDataGuessWeek(String lastfmId, List<TrackWithArtistId> list) {

        try (Connection connection = dataSource.getConnection()) {
            doInsertUserData(connection, lastfmId, list);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    private void doInsertUserData(Connection connection, String lastfmId, List<TrackWithArtistId> list) throws SQLException {
        Week currentWeekId = billboardDao.getCurrentWeekId(connection);
        Date weekStart = currentWeekId.getWeekStart();
        Map<Integer, List<TrackWithArtistId>> dayToData = list.stream().collect(Collectors.groupingBy(x -> {
            LocalDate from = LocalDate.ofInstant(Instant.ofEpochSecond(x.getUtc()), ZoneOffset.UTC);
            int week = from.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            int weekYear = from.get(IsoFields.WEEK_BASED_YEAR);
            return weekYear * 100 + week;
        }, Collectors.toList()));

        LocalDate localDate = weekStart.toLocalDate().minus(1, ChronoUnit.WEEKS);
        int id = currentWeekId.getId();
        int week = localDate.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int weekYear = localDate.get(IsoFields.WEEK_BASED_YEAR);
        int i;
        if (week == 52) {
            i = (weekYear + 1) * 100 + 1;
        } else {
            i = weekYear * 100 + week + 1;
        }
        connection.setAutoCommit(true);
        dayToData.entrySet().stream().sorted(Map.Entry.<Integer, List<TrackWithArtistId>>comparingByKey().reversed()).forEach(x -> {
            int currentIndex = i;
            int numberOfDecreses = -1;
            while (x.getKey() < currentIndex) {
                if (currentIndex % 100 == 1) {
                    int i1 = currentIndex / 100;
                    currentIndex = (i1 - 1) * 100 + 52;
                } else {
                    currentIndex--;
                }
                numberOfDecreses++;
            }
            billboardDao.insertUserData(connection, x.getValue(), lastfmId, id - numberOfDecreses);
        });
        try {
            List<ScrobbledTrack> groupedTracks = list.stream().map(t -> {
                ScrobbledTrack scrobbledTrack = new ScrobbledTrack(t.getArtist(), t.getName(), t.getPlays(), t.isLoved(), t.getDuration(), t.getImageUrl(), t.getArtistMbid(), t.getMbid());
                scrobbledTrack.setArtistId(t.getArtistId());
                return scrobbledTrack;
            }).collect(Collectors.collectingAndThen(Collectors.groupingBy(x -> x, Collectors.reducing(0, e -> 1, Integer::sum)), x -> x.entrySet().stream().map(t -> {
                ScrobbledTrack key = t.getKey();
                key.setCount(t.getValue());
                return key;
            }))).toList();
            insertTracks(groupedTracks, lastfmId, connection, false);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    public List<BillboardEntity> getGlobalBillboard(int weekId, boolean doListeners) {
        try (Connection connection = dataSource.getConnection()) {
            return billboardDao.getGlobalBillboard(connection, weekId, doListeners);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public List<BillboardEntity> getGlobalArtistBillboard(int weekId, boolean doListeners) {
        try (Connection connection = dataSource.getConnection()) {
            return billboardDao.getGlobalArtistBillboard(connection, weekId, doListeners);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public List<BillboardEntity> getGlobalAlbumBillboard(int weekId, boolean doListeners) {
        try (Connection connection = dataSource.getConnection()) {
            return billboardDao.getGlobalAlbumBillboard(connection, weekId, doListeners);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public void insertGlobalBillboardData(int week_id) {
        try (Connection connection = dataSource.getConnection()) {
            billboardDao.insertGlobalBillboardDataListeners(connection, week_id);
            billboardDao.insertGlobalBillboardDataScrobbles(connection, week_id);
            billboardDao.insertGlobalBillboardDataListenersByArtist(connection, week_id);
            billboardDao.insertGlobalBillboardDataScrobblesByArtist(connection, week_id);
            billboardDao.insertGlobalBillboardDataListenersByAlbum(connection, week_id);
            billboardDao.insertGlobalBillboardDataScrobblesByAlbum(connection, week_id);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void insertCombo(StreakEntity combo, long discordID, long artistId, @Nullable Long albumId) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.insertCombo(connection, combo, discordID, artistId, albumId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<StreakEntity> getUserStreaks(long discordId) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getUserStreaks(discordId, connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<GlobalStreakEntities> getTopStreaks(@Nullable Long extraParam, @Nullable Long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getTopStreaks(connection, extraParam, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public String getReverseCorrection(String correction) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getReverseCorrection(connection, correction);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<GlobalStreakEntities> getArtistTopStreaks(@Nullable Long threshold, @Nullable Long guildId, long artistId) {
        return getArtistTopStreaks(threshold, guildId, artistId, null);
    }


    public List<GlobalStreakEntities> getArtistTopStreaks(@Nullable Long threshold, @Nullable Long guildId, long artistId, Integer limit) {
        try (Connection connection = dataSource.getConnection()) {

            return queriesDao.getArtistTopStreaks(connection, threshold, guildId, artistId, limit);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<StreakEntity> getUserArtistTopStreaks(long discordID, long artistId, Integer limit) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getUserArtistTopStreaks(connection, artistId, limit, discordID);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void addUrlRating(long author, int rating, String url) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.addUrlRating(connection, author, rating, url);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public List<ScoredAlbumRatings> getServerTopUrl(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getServerTopRandomUrls(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<ScoredAlbumRatings> getGlobalTopUrl() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getGlobalTopRandomUrls(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<ScoredAlbumRatings> getByUserTopRatedUrls(long discordId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getTopUrlsRatedByUser(connection, discordId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<ScoredAlbumRatings> getUserTopRatedUrlsByEveryoneElse(long discordId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getUserTopRatedUrlsByEveryoneElse(connection, discordId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public Set<String> getPrivateLastfmIds() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getPrivateLastfmIds(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public void setPrivateLastfm(long discordId, boolean privateLastfmId) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setUserProperty(connection, discordId, "private_lastfm", privateLastfmId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void setShowBotted(long discordId, boolean show_botted) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setUserProperty(connection, discordId, "show_botted", show_botted);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public UsersWrapper getRandomUser() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return updaterDao.getRandomUser(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void updateMbids(List<ScrobbledArtist> artistData) {

        try (Connection connection = dataSource.getConnection()) {
            updaterDao.updateMbids(connection, artistData);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<ScrobbledAlbum> getUserAlbums(String lastfmId) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getUserAlbums(connection, lastfmId, null);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<ScrobbledAlbum> getUserAlbums(String lastfmId, int limit) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getUserAlbums(connection, lastfmId, limit);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public ResultWrapper<ScrobbledAlbum> getGuildAlbumTop(Long guildID, int limit, boolean doCount) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getGuildTopAlbum(connection, guildID, limit, doCount);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public ResultWrapper<ScrobbledAlbum> getCollectiveAOTY(@Nullable Long guildID, int limit, boolean doCount, Year year) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getCollectiveAOTY(connection, year, guildID, limit, doCount);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public ResultWrapper<ScrobbledAlbum> getCollectiveAOTD(@Nullable Long guildID, int limit, boolean doCount, Year year, int range) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getCollectiveAOTD(connection, year, range, guildID, limit, doCount);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public ResultWrapper<ScrobbledTrack> getGuildTrackTop(Long guildID, int limit, boolean doCount) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return trackDao.getGuildTopTracks(connection, guildID, limit, doCount);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void updateAlbumImage(long albumId, String albumUrl) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.updateAlbumImage(connection, albumId, albumUrl);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }


    }

    public void serverBlock(long discordId, long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            removeFromGuild(discordId, guildId);
            userGuildDao.serverBlock(connection, discordId, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public void serverUnblock(long discordId, long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.serverUnblock(connection, discordId, guildId);
            userGuildDao.addGuild(connection, discordId, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public boolean isUserServerBanned(long userId, long guildID) {

        try (Connection connection = dataSource.getConnection()) {
            return userGuildDao.isUserServerBanned(connection, userId, guildID);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<ScrobbledAlbum> getDiscoveredAlbums(List<ScrobbledAlbum> scrobbledArtists, String lastfmId) {

        try (Connection connection = dataSource.getConnection()) {
            try {
                updaterDao.fillIds(connection, scrobbledArtists);

                albumDao.fillIds(connection, scrobbledArtists);
                scrobbledArtists = scrobbledArtists.stream().filter(z -> z.getAlbumId() != -1).toList();
                discoveralDao.setDiscoveredAlbumTempTable(connection, scrobbledArtists, lastfmId);

                Map<AlbumInfo, ScrobbledAlbum> scrobbledAlbumLookup = scrobbledArtists.stream().collect(Collectors.toMap(x -> new AlbumInfo(x.getAlbum(), x.getArtist()), x -> x, (x, y) -> {
                    x.setCount(x.getCount() + y.getCount());
                    return x;
                }));
                List<AlbumInfo> discoveredAlbums = discoveralDao.calculateDiscoveryFromAlbumTemp(connection, lastfmId);
                return scrobbledAlbumLookup.entrySet().stream().filter(x -> discoveredAlbums.contains(x.getKey())).map(Map.Entry::getValue).toList();
            } finally {
                discoveralDao.deleteDiscoveryAlbumTempTable(connection);
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<ScrobbledArtist> getDiscoveredArtists(Collection<ScrobbledArtist> scrobbledArtists, String lastfmId) {

        try (Connection connection = dataSource.getConnection()) {
            discoveralDao.setDiscoveredArtistTempTable(connection, scrobbledArtists, lastfmId);
            Map<ArtistInfo, ScrobbledArtist> lookupScrobbleArtists = scrobbledArtists.stream().collect(Collectors.toMap(x -> new ArtistInfo(null, x.getArtist()), x -> x, (x, y) -> {
                x.setCount(x.getCount() + y.getCount());
                return x;
            }));
            Set<ArtistInfo> discoveredArtists = discoveralDao.calculateDiscoveryFromArtistTemp(connection, lastfmId);

            Map<ArtistInfo, ArtistInfo> urlLookup = discoveredArtists.stream().collect(Collectors.toMap(x -> x, x -> x));


            discoveralDao.deleteDiscoveryArtistTable(connection);

            return lookupScrobbleArtists.entrySet().stream().filter(x -> {
                boolean contains = discoveredArtists.contains(x.getKey());
                if (contains) {
                    x.getValue().setUrl(urlLookup.get(x.getKey()).getArtistUrl());
                }
                return contains;
            }).map(Map.Entry::getValue).toList();

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public EnumSet<NPMode> getNPModes(long discordId) {
        try (Connection connection = dataSource.getConnection()) {
            long rawModes = userGuildDao.getNPRaw(connection, discordId);
            return NPMode.getNPMode(rawModes);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }


    }

    public void changeNpMode(long discordId, EnumSet<NPMode> modes) {

        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setNpRaw(connection, discordId, NPMode.getRaw(modes.toArray(NPMode[]::new)));
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public void changeChartMode(long discordId, EnumSet<ChartOptions> modes) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setChartOptionsRaw(connection, discordId, ChartOptions.getRaw(modes.toArray(ChartOptions[]::new)));
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public Rating getUserAlbumRating(long discordId, long albumId, long artistId) {
        try (Connection connection = dataSource.getConnection()) {
            return rymDao.getUserAlbumRating(connection, discordId, albumId, artistId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void setServerNPModes(long guildId, EnumSet<NPMode> modes) {
        try (Connection connection = dataSource.getConnection()) {
            if (modes.size() == 1 && modes.contains(NPMode.UNKNOWN)) {
                userGuildDao.setServerNpRaw(connection, guildId, -1L);
            } else {
                userGuildDao.setServerNpRaw(connection, guildId, NPMode.getRaw(modes.toArray(NPMode[]::new)));
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public EnumSet<NPMode> getServerNPModes(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            long rawModes = userGuildDao.getServerNPRaw(connection, guildId);
            return NPMode.getNPMode(rawModes);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public void setTimezoneUser(TimeZone timeZone, long idLong) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setTimezoneUser(connection, timeZone, idLong);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public TimeZone getUserTimezone(long userId) {
        try (Connection connection = dataSource.getConnection()) {
            return userGuildDao.getTimezone(connection, userId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public List<ScrobbledAlbum> fillAlbumIdsByMBID(List<AlbumInfo> albums) {

        try (Connection connection = dataSource.getConnection()) {
            return updaterDao.fillAlbumsByMBID(connection, albums);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void insertAlbumTags(Map<Genre, List<ScrobbledAlbum>> genres) {
        try (Connection connection = dataSource.getConnection()) {
            Map<String, String> correctedTags = updaterDao.validateTags(connection, new ArrayList<>(genres.keySet()));
            if (genres.values().stream().mapToLong(List::size).sum() != 0) {
                updaterDao.insertAlbumTags(connection, genres, correctedTags);
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void insertTrackTags(Map<Genre, List<ScrobbledTrack>> genres) {
        try (Connection connection = dataSource.getConnection()) {
            Map<String, String> correctedTags = updaterDao.validateTags(connection, new ArrayList<>(genres.keySet()));
            if (genres.values().stream().mapToLong(List::size).sum() != 0) {
                updaterDao.insertTrackTags(connection, genres, correctedTags);
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void insertArtistTags(Map<Genre, List<ScrobbledArtist>> genres) {
        try (Connection connection = dataSource.getConnection()) {
            Map<String, String> correctedTags = updaterDao.validateTags(connection, new ArrayList<>(genres.keySet()));
            if (genres.values().stream().mapToLong(List::size).sum() != 0) {
                updaterDao.insertArtistTags(connection, genres, correctedTags);
            }
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public WrapperReturnNowPlaying whoKnowsGenre(String genre, long guildId, int limit) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.whoKnowsTag(connection, genre, guildId, limit);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public WrapperReturnNowPlaying whoKnowsGenre(String genre, long guildId) {
        return this.whoKnowsGenre(genre, guildId, 10);
    }

    public List<ScrobbledArtist> getTopInTag(String genre, Long guildId, int limit) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getTopTag(connection, genre, guildId, limit);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<ScrobbledArtist> getTopInTag(Set<String> genre, Long guildId, int limit, SearchMode mode) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getTopTagSet(connection, genre, guildId, limit, mode);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public Optional<ScrobbledArtist> getTopInTag(String genre, Long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            List<ScrobbledArtist> topTag = queriesDao.getTopTag(connection, genre, guildId, 1);
            if (topTag.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(topTag.get(0));
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public Optional<ScrobbledArtist> getTopInTag(Set<String> genre, Long guildId, SearchMode mode) {
        try (Connection connection = dataSource.getConnection()) {
            List<ScrobbledArtist> topTag = queriesDao.getTopTagSet(connection, genre, guildId, 1, mode);
            if (topTag.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(topTag.get(0));
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public void addBannedTag(String tag, long discordId) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.addBannedTag(connection, tag);
            updaterDao.removeTagWholeArtist(connection, tag);
            updaterDao.removeTagWholeAlbum(connection, tag);
            updaterDao.removeTagWholeTrack(connection, tag);
            updaterDao.logBannedTag(connection, tag, discordId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }


    }

    public void addArtistBannedTag(String tag, long artistId, long discordId) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.addArtistBannedTag(connection, tag, artistId);
            updaterDao.removeTagArtist(connection, tag, artistId);
            updaterDao.removeTagAlbum(connection, tag, artistId);
            updaterDao.removeTagTrack(connection, tag, artistId);
            updaterDao.logBannedTag(connection, tag + ": " + discordId, discordId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }


    }

    public Set<Genre> getBannedTags() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getBannedTags(connection).stream().map(Genre::new).collect(Collectors.toSet());
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<AlbumInfo> getAlbumsWithTags(List<AlbumInfo> albums, long discordId, String tag) {
        try (Connection connection = dataSource.getConnection()) {
            List<ScrobbledAlbum> list = albums.stream().map(z -> new ScrobbledAlbum(z.getName(), z.getArtist(), null, null)).toList();
            updaterDao.fillIds(connection, list);
            albumDao.fillIds(connection, list);
            return queriesDao.getAlbumsWithTag(connection, list.stream().map(ScrobbledAlbum::getAlbumId).toList(), discordId, tag);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public List<TrackInfo> getTrackWithTags(List<TrackInfo> tracks, long discordId, String tag) {
        try (Connection connection = dataSource.getConnection()) {
            List<ScrobbledTrack> list = tracks.stream().map(z -> new ScrobbledTrack(z.getArtist(), z.getTrack(), 0, false, 0, null, null, null)).toList();
            updaterDao.fillIds(connection, list);
            trackDao.fillIds(connection, list);
            return queriesDao.getTracksWithTag(connection, list.stream().map(ScrobbledTrack::getTrackId).toList(), discordId, tag);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<ScrobbledArtist> getUserArtistByMbid(String lastfmId) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getUserArtistsWithMBID(connection, lastfmId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<ScrobbledArtist> getServerArtistsByMbid(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getServerArtistsByMbid(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<ScrobbledArtist> getUserArtistWithTag(long discordId, String genre, int limit) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getUserArtistWithTag(connection, discordId, genre, limit);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public List<ScrobbledAlbum> getUserAlbumWithTag(long discordId, String genre, int limit) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getUserAlbumsWithTag(connection, discordId, genre, limit);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public List<ScrobbledTrack> getUserTracksWithTag(long discordId, String genre, int limit) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getUserTracksWithTag(connection, discordId, genre, limit);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public List<ArtistInfo> getArtistWithTag(List<ArtistInfo> artists, long discordId, String genre) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getArtistWithTag(connection, artists, discordId, genre);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public Set<Long> getServersWithDeletableMessages() {
        try (Connection connection = dataSource.getConnection()) {
            return userGuildDao.getGuildsWithDeletableMessages(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }


    }

    public Set<Long> getServersDontRespondOnErrros() {
        try (Connection connection = dataSource.getConnection()) {
            return userGuildDao.getGuildsDontRespondOnErrros(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }


    public Map<Genre, Integer> genreCountsByArtist(List<ArtistInfo> artistInfos) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.genreCountsByArtist(connection, artistInfos);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public Map<Genre, Integer> genreCountsByAlbum(List<AlbumInfo> albumInfos) {
        try (Connection connection = dataSource.getConnection()) {
            return albumDao.genreCountsByAlbum(connection, albumInfos);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<WrapperReturnNowPlaying> getWhoKnowsArtistSet(Set<String> artists, long guildId, int limit, @Nullable String user) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.whoknowsSet(connection, artists, guildId, limit, user);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public WrapperReturnNowPlaying getWhoKnowsTagSet(Set<String> tags, long guildId, int limit, @Nullable String user, SearchMode searchMode) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.whoknowsTagsSet(connection, tags, guildId, limit, user, searchMode);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public Set<Pair<String, String>> getArtistBannedTags() {

        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getArtistBannedTags(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public Set<String> getArtistTag(long artistId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getArtistTag(connection, artistId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void logCommand(long discordId, Long guildId, String commandName, long nanos, Instant utc, boolean success, boolean isNormalCommand) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.logCommand(connection, discordId, guildId, commandName, nanos, utc, success, isNormalCommand);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void trackUpdate(List<ScrobbledTrack> trackData, List<ScrobbledArtist> artistData, String id) {
        try (Connection connection = dataSource.getConnection()) {
            try {
                /* Prepare connection. */
                connection.setAutoCommit(true);
                if (!trackData.isEmpty()) {
                    updaterDao.fillIds(connection, artistData);
                    Map<String, Long> artistIds = artistData.stream().collect(Collectors.toMap(ScrobbledArtist::getArtist, ScrobbledArtist::getArtistId, (a, b) -> {
                        assert a.equals(b);
                        return a;
                    }));
                    trackData.forEach(x -> {
                        Long artistId = artistIds.get(x.getArtist());
                        artistId = handleNonExistingArtistFromAlbum(connection, x, artistId);
                        x.setArtistId(artistId);
                    });
                    trackData = trackData.stream().filter(x -> x.getName() != null && !x.getName().isBlank()).toList();

                    insertTracks(trackData, id, connection);
                }
                updaterDao.setUpdatedTime(connection, id, null, null);
                connection.setAutoCommit(false);
                connection.commit();
            } catch (SQLException e) {
                throw new ChuuServiceException(e);
            }
        } catch (
                SQLException e) {
            throw new ChuuServiceException(e);
        }


    }

    public long findTrackIdByName(long artistId, String track) throws InstanceNotFoundException {
        try (Connection connection = dataSource.getConnection()) {
            return trackDao.getTrackIdByName(connection, track, artistId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public Pair<Long, Track> findTrackByName(long artistId, String track) throws InstanceNotFoundException {
        try (Connection connection = dataSource.getConnection()) {
            return trackDao.findTrackByName(connection, track, artistId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public void insertTrack(ScrobbledTrack scrobbledTrack) {
        try (Connection connection = dataSource.getConnection()) {
            trackDao.insertTrack(connection, scrobbledTrack);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void updateTrackImage(long trackId, String imageUrl) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.updateTrackImage(connection, trackId, imageUrl);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void updateSpotifyInfo(long trackId, String spotifyId, int duration, int popularity) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.updateSpotifyInfo(connection, trackId, spotifyId, duration, null, popularity);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public Optional<FullAlbumEntity> getAlbumTrackList(long albumId, String lastfmId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return trackDao.getAlbumTrackList(connection, albumId, lastfmId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }


    }

    public List<ScrobbledTrack> getTopTracks(String lastfmId) {
        return getTopTracks(lastfmId, null);
    }

    public List<ScrobbledTrack> getTopTracks(String lastfmId, Integer limit) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return trackDao.getUserTopTracks(connection, lastfmId, limit);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<ScrobbledTrack> getTopTracksNoSpotifyId(String lastfmId, int limit) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return trackDao.getUserTopTracksNoSpotifyId(connection, lastfmId, limit);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<ScrobbledTrack> getTopSpotifyTracksIds(String lastfmId, int limit) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return trackDao.getTopSpotifyTracksIds(connection, lastfmId, limit);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public AudioFeatures getUserFeatures(String lastfmid) {

        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.userFeatures(connection, lastfmid);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void insertAudioFeatures(List<AudioFeatures> audioFeaturesStream) {

        try (Connection connection = dataSource.getConnection()) {
            updaterDao.insertAudioFeatures(connection, audioFeaturesStream);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public Optional<UserInfo> getUserInfo(String lastfmId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return updaterDao.getUserInfo(connection, lastfmId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void insertUserInfo(UserInfo userInfo) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.insertUserInfo(connection, userInfo);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public ResultWrapper<UserArtistComparison> getSimilaritiesAlbumTracks(List<String> name, long albumId, int limit) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.similarAlbumTracks(connection, albumId, name, limit);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void storeTrackList(long albumId, long artistId, Set<Track> trackList) {
        try (Connection connection = dataSource.getConnection()) {
            List<ScrobbledTrack> tracks = trackList.stream().map(x -> {
                ScrobbledTrack scrobbledTrack = new ScrobbledTrack(x.getArtist(), x.getName(), 0, false, x.getDuration(), x.getImageUrl(), null, x.getMbid());
                scrobbledTrack.setAlbumId(albumId);
                scrobbledTrack.setArtistId(artistId);
                scrobbledTrack.setPosition(x.getPosition());
                return scrobbledTrack;
            }).toList();
            trackDao.insertTracks(connection, tracks);
            trackDao.storeTrackList(connection, albumId, tracks);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public void prepareBillboardWeek(String lastfmId, int weekId) {
        try (Connection connection = dataSource.getConnection()) {
            billboardDao.cleanUserData(connection, lastfmId, weekId);
            billboardDao.groupUserData(connection, lastfmId, weekId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }


    public List<Track> getTopArtistTracks(String lastFmName, long artistId, int limit) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return trackDao.getUserTopArtistTracks(connection, lastFmName, artistId, limit);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<Track> getTopArtistTracksDuration(String lastFmName, long artistId, int limit) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return trackDao.getUserTopArtistTracksDuration(connection, lastFmName, artistId, limit);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    /**
     * @return ALbumUserPlays misses url and artist name
     */
    public List<AlbumUserPlays> getServerTopArtistTracks(long guildId, long artistId, int limit) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return trackDao.getServerTopArtistTracks(connection, guildId, artistId, limit);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    /**
     * @return ALbumUserPlays misses url and artist name
     */
    public List<AlbumUserPlays> getGlboalTopArtistTracks(long artistId, int limit) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return trackDao.getGlobalTopArtistTracks(connection, artistId, limit);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public UniqueWrapper<TrackPlays> getArtistGlobalTrackCrowns(String lastfmName, long artistId, int threshold, boolean bottedUsers) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getArtistGlobalTrackCrowns(connection, lastfmName, artistId, threshold, bottedUsers);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public UniqueWrapper<TrackPlays> getUserArtistTrackCrowns(String lastfmId, long artistId, long guildId, int crownthreshold) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getUserArtistTrackCrowns(connection, lastfmId, crownthreshold, guildId, artistId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<AlbumInfo> albumsOfYear(List<AlbumInfo> getYearReleaes, Year year) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return albumDao.get(connection, getYearReleaes, year);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void insertAlbumOfYear(AlbumInfo foundByYear, Year year) {
        try (Connection connection = dataSource.getConnection()) {
            albumDao.insertAlbumOfYear(connection, foundByYear, year);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void insertAlbumsOfYear(List<AlbumInfo> foundByYear, Year year) {
        try (Connection connection = dataSource.getConnection()) {
            albumDao.insertAlbumsOfYear(connection, foundByYear, year);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public Optional<FullAlbumEntity> getServerAlbumTrackList(long albumId, long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return trackDao.getServerAlbumTrackList(connection, albumId, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }


    }

    public Optional<FullAlbumEntity> getGlobalAlbumTrackList(long albumId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return trackDao.getGlobalAlbumTrackList(connection, albumId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public Map<Integer, Integer> getUserCurve(long discordId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return rymDao.getUserCurve(connection, discordId);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }


    }

    public Optional<String> findArtistUrlAbove(long artistId, int upvotes) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.findArtistUrlAbove(connection, artistId, upvotes);

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }


    }

    public CompletableFuture<Object> getCustomPlaylist(long authorId, String name) {
        return null;
    }

    public List<ScrobbledAlbum> getUserAlbumsOfYear(String username, Year year) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return albumDao.getUserAlbumsOfYear(connection, username, year);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<ScrobbledAlbum> getUserAlbumsWithNoYear(String username, int limit) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return albumDao.getUserAlbumsWithNoYear(connection, username, limit);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public Optional<Instant> getLastScrobbled(long artistId, String song, String lastfmId, boolean skipToday) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getLastScrobbled(connection, lastfmId, artistId, song, skipToday);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public Optional<Instant> getLastScrobbledArtist(long artistId, String lastfmId, boolean skipToday) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getLastScrobbledArtist(connection, lastfmId, artistId, skipToday);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public Optional<Instant> getFirstScrobbledArtist(long artistId, String lastfmId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getFirstScrobbledArtist(connection, lastfmId, artistId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public Optional<Instant> getFirstScrobbled(long artistId, String song, String lastfmId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getFirstScrobbled(connection, lastfmId, artistId, song);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public List<UserListened> getServerFirstScrobbledArtist(long artistId, long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getServerFirstScrobbledArtist(connection, artistId, guildId, Order.ASC);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<UserListened> getServerLastScrobbledArtist(long artistId, long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getServerFirstScrobbledArtist(connection, artistId, guildId, Order.DESC);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public void storeToken(String authToken, String lastfm) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.storeToken(connection, authToken, lastfm);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }


    }


    public void storeSess(String session, String lastfm) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.storeSession(connection, session, lastfm);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void clearSess(String lastfm) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.clearSess(connection, lastfm);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public void insertTempUser(long discordId, String token) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.insertTempUser(connection, discordId, token);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public CommandStats getCommandStats(long discordId) {
        try (Connection connection = dataSource.getConnection()) {
            return userGuildDao.getCommandStats(discordId, connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public Set<LastFMData> findScrobbleableUsers(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            return userGuildDao.findScrobbleableUsers(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void storeMetadata(String identifier, Metadata metadata) {

        try (Connection connection = dataSource.getConnection()) {
            musicDao.storeMetadata(connection, identifier, metadata);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public Optional<Metadata> getMetadata(String identifier) {

        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);

            return musicDao.getMetadata(connection, identifier);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void changeScrobbling(long discordId, boolean b) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setUserProperty(connection, discordId, "scrobbling", b);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public int getCurrentCombo(long artistId, String lastfmId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);

            return queriesDao.getCurrentCombo(connection, lastfmId, artistId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public void insertServerReactions(long guildId, List<String> reactions) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.clearServerReactions(connection, guildId);
            if (!reactions.isEmpty())
                userGuildDao.insertServerReactions(connection, guildId, reactions);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void clearServerReacts(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.clearServerReactions(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<String> getServerReactions(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);

            return userGuildDao.getServerReactions(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void insertUserReactions(long userId, List<String> reactions) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.clearUserReacts(connection, userId);
            if (!reactions.isEmpty())
                userGuildDao.insertUserReactions(connection, userId, reactions);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void clearUserReacts(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.clearUserReacts(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<String> getUserReacts(long userId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);

            return userGuildDao.getUserReacts(connection, userId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<ScrobbledArtist> regexArtist(long userId, String regex) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.regexArtist(connection, regex, userId);
        } catch (ChuuServiceException exception) {
            if (exception.getCause() instanceof SQLSyntaxErrorException)
                return regexArtist(userId, "\\Q" + regex + "\\E");
            throw exception;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<ScrobbledAlbum> regexAlbum(long userId, String regex) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.regexAlbum(connection, regex, userId);
        } catch (ChuuServiceException exception) {
            if (exception.getCause() instanceof SQLSyntaxErrorException)
                return regexAlbum(userId, "\\Q" + regex + "\\E");
            throw exception;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<ScrobbledTrack> regexTrack(long userId, String regex) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.regexTrack(connection, regex, userId);
        } catch (ChuuServiceException exception) {
            if (exception.getCause() instanceof SQLSyntaxErrorException)
                return regexTrack(userId, "\\Q" + regex + "\\E");
            throw exception;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public Map<Long, Color[]> getServerWithPalette() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return userGuildDao.getServerWithPalette(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public Map<Long, Color[]> getUsersWithPalette() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return userGuildDao.getUsersWithPalette(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public Map<Long, EmbedColor.EmbedColorType> getUserColorTypes() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return userGuildDao.getUserColorTypes(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public Map<Long, EmbedColor.EmbedColorType> getServerColorTypes() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return userGuildDao.getServerColorTypes(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public Map<Year, Integer> getUserYears(String lastfmId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getUserYears(connection, lastfmId, false);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public Map<Year, Integer> getUserDecades(String lastfmId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getUserYears(connection, lastfmId, true);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public Map<Year, Integer> getGuildYears(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getCollectiveYears(connection, guildId, false);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public Map<Year, Integer> getGuildDecades(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getCollectiveYears(connection, guildId, true);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public Map<Year, Integer> getGlobalYears() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getCollectiveYears(connection, null, false);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public Map<Year, Integer> getGlobalDecades() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getCollectiveYears(connection, null, true);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public Map<Year, Integer> getUserYearsFromList(String lastfmId, List<AlbumInfo> albums) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return albumDao.countByYears(connection, lastfmId, albums);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public Map<Year, Integer> getUserDecadesFromList(String lastfmId, List<AlbumInfo> albums) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return albumDao.countByDecades(connection, lastfmId, albums);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public boolean strikeQueue(long queuedId, ImageQueue image) {
        try (Connection connection = dataSource.getConnection()) {
            long rejectedId = updaterDao.storeRejected(connection, image);
            updaterDao.removeQueuedImage(connection, queuedId);
            updaterDao.addStrike(connection, image.uploader(), rejectedId);
            long count = updaterDao.userStrikes(connection, image.uploader());
            if (count >= 5) {
                try {
                    LastFMData lastFmData = userGuildDao.findLastFmData(connection, image.uploader());
                    if (lastFmData.getRole() == Role.USER) {
                        updaterDao.banUserImage(connection, image.uploader());
                        return true;
                    }
                } catch (InstanceNotFoundException exception) {
                    throw new ChuuServiceException(exception);
                }
            }
            return false;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void flagAsBotted(String lastfmId) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.flagBotted(lastfmId, connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public void unflagAsBotted(String lastfmId) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.unflagAsBotted(lastfmId, connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public ListValuedMap<CoverItem, String> getBannedCovers() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getBannedCovers(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public void insertBannedCover(long albumId, String cover) {
        try (Connection connection = dataSource.getConnection()) {

            userGuildDao.insertBannedCover(connection, albumId, cover);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void removeBannedCover(long albumId, String cover) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.removeBannedCover(connection, albumId, cover);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public Set<Long> getGuildsAcceptingCovers() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return userGuildDao.getGuildsWithCoversOn(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }


    }

    public void insertServerCustomUrl(long altId, long guildId, long artistId) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.insertServerCustomUrl(connection, altId, guildId, artistId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public Set<Long> getGuildsWithEmptyColorOverride() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return userGuildDao.getGuildsWithEmptyColorOverride(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }


    }

    public Set<String> getAlbumTags(long albumId) {

        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return albumDao.getAlbumTags(connection, albumId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public Collection<String> getTrackTags(Long trackId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return trackDao.getTrackTags(connection, trackId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public Set<Long> getServerBlocked(long guildId) {

        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return userGuildDao.getServerBlocked(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }


    }

    public List<CommandUsage> getUserCommands(long discordId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getUserCommands(connection, discordId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<UserCount> getServerCommandsLb(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getServerCommandsLb(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public Optional<Rank<PrivacyUserCount>> getUserPosition(long discordId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getGlobalPosition(connection, discordId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<PrivacyUserCount> getGlobalCommandLb(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getGlobalCommands(connection);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public RandomUrlEntity getRandomUrlFromUser(long userId, @Nullable RandomTarget randomTarget) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return updaterDao.getRandomUrlFromUser(connection, userId, randomTarget);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }

    }

    public void setArtistThreshold(long idLong, int threshold) {

        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.setUserProperty(connection, idLong, "artist_threshold", threshold);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public Optional<Album> findAlbumByTrackId(long trackId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return trackDao.findAlbumFromTrack(connection, trackId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<RoleColour> getRoles(long idLong) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return userGuildDao.getRoles(connection, idLong);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void addRole(long guildId, int first, int second, String rest, Color color, long roleId) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.addRole(connection, guildId, first, second, rest, color, roleId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public List<NoiseGenre> findMatchingGenre(String genre) {
        return everyNoiseService.findMatchingGenre(genre);
    }

    @Override
    public Optional<NoiseGenre> findExactMatch(String genre) {
        return everyNoiseService.findExactMatch(genre);
    }

    @Override
    public List<Release> releasesOfGenre(NoiseGenre genre) {
        return everyNoiseService.releasesOfGenre(genre);
    }

    @Override
    public List<Release> allValidReleases() {
        return everyNoiseService.allValidReleases();
    }

    @Override
    public Map<NoiseGenreReleases, Integer> releasesByCount() {
        return everyNoiseService.releasesByCount();
    }


    @Override
    public List<NoiseGenre> listAllGenres() {
        return everyNoiseService.listAllGenres();
    }

    public Optional<ObscurityStats> getServerObscurityStats(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.serverObscurityStats(connection, guildId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public OptionalDouble obtainObscurity(String lastfmId) {
        try (Connection connection = dataSource.getConnection()) {
            return userGuildDao.obtainObscurity(connection, lastfmId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    private double genObscurity(String lastfmId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.obscurity(connection, lastfmId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public double processObscurity(String lastfmId) {
        double v = genObscurity(lastfmId);
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.insertObscurity(connection, lastfmId, v);
            return v;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public void insertReleases(List<ReleaseWithGenres> release, LocalDate week) {
        everyNoiseService.insertReleases(release, week);
    }

    @Override
    public void insertGenres(List<NoiseGenre> genres) {
        everyNoiseService.insertGenres(genres);
    }


    public Map<ScrobbledArtist, Long> topArtistsByDuration(String lastfmId, int limit) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return trackDao.getUserTopArtistByDuration(connection, lastfmId, limit);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }


    public void updateLovedSongs(String lastfm, List<ScrobbledTrack> loved) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.fillIds(connection, loved);
            trackDao.fillIds(connection, loved);
            Set<Long> ids = loved.stream().map(ScrobbledTrack::getTrackId).filter(trackId -> trackId > 0).collect(Collectors.toSet());
            trackDao.resetLovedSongs(connection, lastfm);
            trackDao.updateLovedSongs(connection, ids, true, lastfm);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void loveSong(String lastfm, long trackId, boolean loved) {
        try (Connection connection = dataSource.getConnection()) {
            trackDao.updateLovedSongs(connection, Set.of(trackId), loved, lastfm);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<LbEntry<Float>> getServerAudioLeadearboard(AudioStats element, long guildId, Order order) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.audioLb(connection, element, guildId, order);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void removeQueuedPictures(long uploader) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            userGuildDao.removeQueuedPictures(connection, uploader);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void deleteTracklist(long albumId) {
        try (Connection connection = dataSource.getConnection()) {
            trackDao.deleteTracklist(connection, albumId);
        } catch (SQLException e) {
            throw new ChuuServiceException();
        }
    }

    public List<Friend> getUserFriends(long discordId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return friendDAO.getUserFriends(connection, discordId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<Friend> getIncomingFriendRequests(long discordId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return friendDAO.getIncomingRequests(connection, discordId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public List<Friend> getFriendPendingRequests(long discordId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return friendDAO.getPendingRequests(connection, discordId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public boolean acceptRequest(long discordId, long requesterId) {
        try (Connection connection = dataSource.getConnection()) {
            return friendDAO.acceptRequest(connection, discordId, requesterId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void createRequest(long discordId, long receiverId) {
        try (Connection connection = dataSource.getConnection()) {
            friendDAO.createRequest(connection, discordId, receiverId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public void rejectRequest(long discordId, long requesterId) {
        try (Connection connection = dataSource.getConnection()) {
            friendDAO.rejectRequest(connection, discordId, requesterId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public Optional<Friend> areFriends(long discordId, long requesterId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return friendDAO.areFriends(connection, discordId, requesterId);
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public WrapperReturnNowPlaying friendsWhoKnows(long artistId, long discordId, int limit) {
        try (Connection connection = dataSource.getConnection()) {
            List<Long> userFriends = friendDAO.getUserFriendsIds(connection, discordId);
            Set<Long> friendIds = Stream.concat(userFriends.stream(), Stream.of(discordId)).collect(Collectors.toSet());
            return executeInHiddenServer(friendIds,
                    (hiddenConnection, guildId) -> queriesDao.knows(connection, artistId, guildId, limit));
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public WrapperReturnNowPlaying friendsWhoKnowsSong(long trackId, long discordId, int limit) {
        try (Connection connection = dataSource.getConnection()) {
            List<Long> userFriends = friendDAO.getUserFriendsIds(connection, discordId);
            Set<Long> friendIds = Stream.concat(userFriends.stream(), Stream.of(discordId)).collect(Collectors.toSet());
            return executeInHiddenServer(friendIds,
                    (hiddenConnection, guildId) -> queriesDao.whoKnowsTrack(connection, trackId, guildId, limit));
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    public WrapperReturnNowPlaying friendsWhoKnowsAlbum(long albumId, long discordId, int limit) {
        try (Connection connection = dataSource.getConnection()) {
            List<Long> userFriends = friendDAO.getUserFriendsIds(connection, discordId);
            Set<Long> friendIds = Stream.concat(userFriends.stream(), Stream.of(discordId)).collect(Collectors.toSet());
            return executeInHiddenServer(friendIds,
                    (hiddenConnection, guildId) -> queriesDao.whoKnowsAlbum(connection, albumId, guildId, limit));
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    private <T> T executeInHiddenServer(Set<Long> userIds, HiddenRunnable<T> runnable) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            long hiddenServer = userGuildDao.createHiddenServer(connection, userIds);
            T result = runnable.executeInHiddenServer(connection, hiddenServer);
            connection.rollback();
            return result;
        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    private interface HiddenRunnable<T> {
        T executeInHiddenServer(Connection connection, long guildId) throws SQLException;
    }
}

