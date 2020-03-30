package dao;

import core.Chuu;
import core.exceptions.DuplicateInstanceException;
import core.exceptions.InstanceNotFoundException;
import dao.entities.*;
import org.apache.commons.collections4.MultiValuedMap;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import java.util.stream.Collectors;

public class ChuuService {
    private final SimpleDataSource dataSource;
    private final SQLQueriesDao queriesDao;
    private final UpdaterDao updaterDao;
    private final UserGuildDao userGuildDao;

    public ChuuService(SimpleDataSource dataSource) {

        this.dataSource = dataSource;
        this.queriesDao = new SQLQueriesDaoImpl();
        this.userGuildDao = new UserGuildDaoImpl();

        this.updaterDao = new UpdaterDaoImpl();
    }

    public ChuuService() {

        this.dataSource = new SimpleDataSource(true);
        this.queriesDao = new SQLQueriesDaoImpl();
        this.userGuildDao = new UserGuildDaoImpl();
        this.updaterDao = new UpdaterDaoImpl();

    }

    public void updateUserTimeStamp(String lastFmName, Integer timestamp, Integer timestampControl) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.setUpdatedTime(connection, lastFmName, timestamp, timestampControl);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void insertArtistDataList(List<ScrobbledArtist> list, String id) {
        try (Connection connection = dataSource.getConnection()) {

            try {

                /* Prepare connection. */

                connection.setAutoCommit(false);
                if (!list.isEmpty()) {
                    //delete everything first to have a clean start
                    updaterDao.deleteAllArtists(connection, id);
                    /* Do work. */
                    updaterDao.fillIds(connection, list);

                    Map<Boolean, List<ScrobbledArtist>> map = list.stream().peek(x -> x.setDiscordID(id)).collect(Collectors.partitioningBy(scrobbledArtist -> scrobbledArtist.getArtistId() == -1));
                    List<ScrobbledArtist> nonExistingId = map.get(true);
                    if (nonExistingId.size() > 0) {
                        nonExistingId.forEach(x -> updaterDao.insertArtistSad(connection, x));
                        //updaterDao.insertArtists(connection, nonExistingId);
                        //updaterDao.insertArtists(connection, nonExistingId);
                    }
                    List<ScrobbledArtist> scrobbledArtists = map.get(false);
                    scrobbledArtists.addAll(nonExistingId);
                    updaterDao.addSrobbledArtists(connection, scrobbledArtists);
                }
                updaterDao.setUpdatedTime(connection, id, null, null);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException(e);
            } catch (RuntimeException | Error e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void incrementalUpdate(TimestampWrapper<List<ScrobbledArtist>> wrapper, String id) {
        try (Connection connection = dataSource.getConnection()) {

            try {

                /* Prepare connection. */
                connection.setAutoCommit(false);
                List<ScrobbledArtist> peeked = wrapper.getWrapped().stream().peek(x -> x.setDiscordID(id)).collect(Collectors.toList());
                updaterDao.upsertArtist(connection, peeked);
                updaterDao.setUpdatedTime(connection, id, wrapper.getTimestamp(), wrapper.getTimestamp());

                connection.commit();


            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException(e);
            } catch (RuntimeException | Error e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addGuildUser(long userID, long guildID) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.addGuild(connection, userID, guildID);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void insertNewUser(LastFMData data) {
        try (Connection connection = dataSource.getConnection()) {

            try {
                connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                connection.setAutoCommit(false);

                userGuildDao.insertUserData(connection, data);
                userGuildDao.addGuild(connection, data.getDiscordId(), data.getGuildID());
                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException(e);
            } catch (RuntimeException | Error e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
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
                throw new RuntimeException(e);
            } catch (RuntimeException | Error e) {
                connection.rollback();
                throw e;
            } catch (InstanceNotFoundException e) {
                Chuu.getLogger().warn(e.getMessage(), e);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public LastFMData findLastFMData(long discordID) throws InstanceNotFoundException {
        try (Connection connection = dataSource.getConnection()) {
            return userGuildDao.findLastFmData(connection, discordID);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ResultWrapper<UserArtistComparison> getSimilarities(List<String> lastFmNames) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.similar(connection, lastFmNames);
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }
    }

    public UpdaterUserWrapper getLessUpdated() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return updaterDao.getLessUpdated(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<UsersWrapper> getAll(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return userGuildDao.getAll(connection, guildId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<UsersWrapper> getAllALL() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return userGuildDao.getAll(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public UniqueWrapper<ArtistPlays> getUniqueArtist(Long guildID, String lastFmId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getUniqueArtist(connection, guildID, lastFmId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Long> getGuildList(long userId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return userGuildDao.guildsFromUser(connection, userId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<UrlCapsule> getGuildTop(Long guildID, int limit) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getGuildTop(connection, guildID, limit);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public UniqueWrapper<ArtistPlays> getCrowns(String lastFmID, long guildID) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getCrowns(connection, lastFmID, guildID);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<ScrobbledArtist> getNullUrls() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return updaterDao.selectNullUrls(connection, false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<ScrobbledArtist> getSpotifyNulledUrls() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return updaterDao.selectNullUrls(connection, true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getArtistUrl(String url) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return updaterDao.getArtistUrl(connection, url);
        } catch (SQLException e) {
            throw new RuntimeException(e);

        }
    }

    public void upsertUrl(String url, long artist_id) {
        upsertUrl(url, artist_id, Chuu.getPresence().getJDA().getSelfUser().getIdLong());
    }

    public void userInsertUrl(String url, long artist_id, long discord_id) {
        try (Connection connection = dataSource.getConnection()) {
            long url_id = updaterDao.upsertUrl(connection, url, artist_id, discord_id);
            updaterDao.castVote(connection, url_id, discord_id, true);
        } catch (SQLException e) {
            throw new RuntimeException(e);

        }

    }

    private void upsertUrl(String url, long artist_id, long discord_id) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.upsertUrl(connection, url, artist_id, discord_id);
        } catch (SQLException e) {
            throw new RuntimeException(e);

        }

    }

    public void upsertSpotify(String url, long artist_Id, long discord_id) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.updateUrlStatus(connection, artist_Id);
            if (url != null && !url.isBlank()) {
                updaterDao.upsertSpotify(connection, url, artist_Id, discord_id);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void upsertSpotify(String url, long artist_id) {
        this.upsertSpotify(url, artist_id, Chuu.getPresence().getJDA().getSelfUser().getIdLong());
    }

    public void addLogo(long guildId, BufferedImage in) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.addLogo(connection, guildId, in);
        } catch (SQLException e) {
            throw new RuntimeException(e);

        }

    }

    public void removeLogo(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.removeLogo(connection, guildId);
        } catch (SQLException e) {
            throw new RuntimeException(e);

        }

    }

    public InputStream findLogo(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            return userGuildDao.findLogo(connection, guildId);
        } catch (SQLException e) {
            throw new RuntimeException(e);

        }

    }

    public long getDiscordIdFromLastfm(String lasFmName, long guildId) throws InstanceNotFoundException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return userGuildDao.getDiscordIdFromLastFm(connection, lasFmName, guildId);
        } catch (SQLException e) {
            throw new RuntimeException(e);

        }
    }

    public int getArtistPlays(long artistId, String whom) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.userPlays(connection, artistId, whom);
        } catch (SQLException e) {
            throw new RuntimeException(e);

        }
    }

    public List<LbEntry> getGuildCrownLb(long guildId) {

        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.crownsLeaderboard(connection, guildId);
        } catch (SQLException e) {
            throw new RuntimeException(e);

        }
    }

    public void removeUserFromOneGuildConsequent(long discordID, long guildID) {
        removeFromGuild(discordID, guildID);
        MultiValuedMap<Long, Long> map = getMapGuildUsers();
        if (!map.containsValue(discordID)) {

            removeUserCompletely(discordID);
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
                Chuu.getLogger().info("Deleted User " + discordID + " :At " + LocalDateTime.now()
                        .format(DateTimeFormatter.ISO_DATE));
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException(e);
            } catch (RuntimeException | Error e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
                throw new RuntimeException(e);
            } catch (RuntimeException | Error e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public MultiValuedMap<Long, Long> getMapGuildUsers() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return userGuildDao.getWholeUser_Guild(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);

        }
    }


    public UpdaterStatus getUpdaterStatusByName(String artist) throws InstanceNotFoundException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return updaterDao.getUpdaterStatus(connection, artist);
        } catch (SQLException e) {
            throw new RuntimeException(e);

        }

    }

    public void updateStatusBit(long artistId) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.updateStatusBit(connection, artistId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void insertCorrection(long artistId, String correction) {

        try (Connection connection = dataSource.getConnection()) {
            updaterDao.insertCorrection(connection, artistId, correction);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String findCorrection(String artist_id) {
        try (Connection connection = dataSource.getConnection()) {
            return updaterDao.findCorrection(connection, artist_id);
        } catch (SQLException e) {
            throw new RuntimeException(e);

        }
    }

    public void updateMetrics(long value1, long value2, long value3, long value4) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.updateMetric(connection, 1, value1);
            updaterDao.updateMetric(connection, 2, value2);
            updaterDao.updateMetric(connection, 3, value3);
            updaterDao.updateMetric(connection, 4, value4);
        } catch (SQLException e) {
            throw new RuntimeException(e);

        }
    }

    public List<LbEntry> getUniqueLeaderboard(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.uniqueLeaderboard(connection, guildId);
        } catch (SQLException e) {
            throw new RuntimeException(e);

        }
    }

    public int getUserArtistCount(String lastfmId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.userArtistCount(connection, lastfmId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<LbEntry> getArtistLeaderboard(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.artistLeaderboard(connection, guildId);
        } catch (SQLException e) {
            throw new RuntimeException(e);

        }
    }

    public List<LbEntry> getObscurityRankings(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.obscurityLeaderboard(connection, guildId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public PresenceInfo getRandomArtistWithUrl() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getRandomArtistWithUrl(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public int randomCount(@Nullable Long userId) {
        try (Connection connection = dataSource.getConnection()) {
            return (queriesDao.getRandomCount(connection, userId));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean randomUrlExists(String url) {
        try (Connection connection = dataSource.getConnection()) {
            return (updaterDao.findRandomUrlById(connection, url) != null);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean addToRandomPool(RandomUrlEntity randomUrlEntity) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            if (updaterDao.findRandomUrlById(connection, randomUrlEntity.getUrl()) == null) {
                updaterDao.insertRandomUrl(connection, randomUrlEntity.getUrl(), randomUrlEntity
                        .getDiscordId(), randomUrlEntity.getGuildId());
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public RandomUrlEntity getRandomUrl() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return updaterDao.getRandomUrl(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public StolenCrownWrapper getCrownsStolenBy(String ogUser, String queriedUser, long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getCrownsStolenBy(connection, ogUser, queriedUser, guildId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public UniqueWrapper<ArtistPlays> getUserAlbumCrowns(String lastfmId, long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getUserAlbumCrowns(connection, lastfmId, guildId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<LbEntry> albumCrownsLeaderboard(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.albumCrownsLeaderboard(connection, guildId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertAlbumCrown(long artistId, String album, long discordID, long guildId, int plays) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.insertAlbumCrown(connection, artistId, album, discordID, guildId, plays);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteAlbumCrown(String artist, String album, long discordID, long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.deleteAlbumCrown(connection, artist, album, discordID, guildId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<Long, Character> getGuildPrefixes() {
        try (Connection connection = dataSource.getConnection()) {
            return updaterDao.getGuildPrefixes(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addGuildPrefix(long guildID, Character prefix) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.upsertGuildPrefix(connection, guildID, prefix);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ObscuritySummary getObscuritySummary(String lastfmid) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getUserObscuritPoints(connection, lastfmid);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void truncateRandomPool() {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.truncateRandomPool(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<GlobalCrown> getGlobalArtistRanking(Long artistId) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getGlobalKnows(connection, artistId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public UniqueWrapper<ArtistPlays> getGlobalUniques(String lastfmid) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getGlobalUniques(connection, lastfmid);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public UniqueWrapper<ArtistPlays> getGlobalCrowns(String lastfmid) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getGlobalCrowns(connection, lastfmid);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void createGuild(long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            userGuildDao.createGuild(connection, guildId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }
    }

    public UpdaterUserWrapper getUserUpdateStatus(long discordId) throws InstanceNotFoundException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return updaterDao.getUserUpdateStatus(connection, discordId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ResultWrapper<ArtistPlays> getArtistFrequencies(long guildID) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getArtistFrequencies(connection, guildID);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ResultWrapper<ArtistPlays> getArtistFrequenciesGlobal() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getGlobalArtistFrequencies((connection));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ResultWrapper<ArtistPlays> getArtistPlayCount(long guildID) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getArtistPlayCount(connection, guildID);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ResultWrapper<ArtistPlays> getArtistPlayCountGlobal() {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getGlobalArtistPlayCount((connection));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ScrobbledArtist> getAllUserArtist(long discordId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.getAllUsersArtist(connection, discordId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void changeLastFMName(long userId, String lastFmID) throws DuplicateInstanceException, InstanceNotFoundException {
        try (Connection connection = dataSource.getConnection()) {
            try {
                LastFMData lastFMData = userGuildDao.findByLastFMId(connection, lastFmID);
                throw new DuplicateInstanceException(lastFmID);
            } catch (InstanceNotFoundException ignored) {
            }
            updaterDao.deleteAllArtists(connection, lastFmID);
            LastFMData lastFmData = userGuildDao.findLastFmData(connection, userId);
            lastFmData.setName(lastFmID);
            userGuildDao.updateLastFmData(connection, lastFmData);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public List<LbEntry> matchingArtistsCount(long userId, long guildId) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setReadOnly(true);
            return queriesDao.matchingArtistCount(connection, userId, guildId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void addAlias(String alias, long toArtist) throws DuplicateInstanceException {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.addAlias(connection, alias, toArtist);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void enqueAlias(String alias, long toArtist, long whom) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.queueAlias(connection, alias, toArtist, whom);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public AliasEntity getNextInAliasQueue() {
        try (Connection connection = dataSource.getConnection()) {
            return updaterDao.getNextInAliasQueue(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteAliasById(long aliasId) throws InstanceNotFoundException {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.deleteAliasById(connection, aliasId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public long getArtistId(String artist) throws InstanceNotFoundException {
        try (Connection connection = dataSource.getConnection()) {
            return updaterDao.getArtistId(connection, artist);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public OptionalLong checkArtistUrlExists(long artistId, String urlParsed) {
        try (Connection connection = dataSource.getConnection()) {
            return updaterDao.checkArtistUrlExists(connection, artistId, urlParsed);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public VoteStatus castVote(long url_id, long discord_id, boolean isPositive) {
        try (Connection connection = dataSource.getConnection()) {
            Boolean hasVotedBefore = queriesDao.hasUserVotedImage(connection, url_id, discord_id);
            updaterDao.castVote(connection, url_id, discord_id, isPositive);
            if (hasVotedBefore == null) {
                return VoteStatus.NEW_VOTE;
            }
            return hasVotedBefore == isPositive ? VoteStatus.SAME_VALUE : VoteStatus.CHANGE_VALUE;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<VotingEntity> getAllArtistImages(long artist_id) {

        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getAllArtistImages(connection, artist_id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void report(long urlId, long userIdLong) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.reportImage(connection, urlId, userIdLong);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getArtistAliases(long artistId) {
        try (Connection connection = dataSource.getConnection()) {
            return queriesDao.getArtistAliases(connection, artistId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void upsertArtistSad(ScrobbledArtist scrobbledArtist) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.insertArtistSad(connection, scrobbledArtist);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeReportedImage(long alt_id, long image_owner, long mod_id) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.removeImage(connection, alt_id);
            updaterDao.logRemovedImage(connection, image_owner, mod_id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int getReportCount() {
        try (Connection connection = dataSource.getConnection()) {
            return updaterDao.getReportCount(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void ignoreReportedImage(long altId) {
        try (Connection connection = dataSource.getConnection()) {
            updaterDao.removeReport(connection, altId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ReportEntity getNextReport(LocalDateTime localDateTime) {
        try (Connection connection = dataSource.getConnection()) {
            return updaterDao.getReportEntity(connection, localDateTime);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
