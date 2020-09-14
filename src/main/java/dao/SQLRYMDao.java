package dao;

import dao.entities.*;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SQLRYMDao {
    void setServerTempTable(Connection connection, List<RYMImportRating> ratings);

    Map<Long, Long> findArtists(Connection connection);

    Map<Long, Long> findArtistsByLocalizedJoinedNames(Connection connection);

    Map<Long, Long> findArtistsByLocalizedNames(Connection connection);

    Map<Long, Long> findArtistsByJoinedNames(Connection connection);

    Map<Long, Long> findArtistsByNames(Connection connection);

    Map<Long, Long> findArtistsAuxiliar(Connection connection);

    void insertRatings(Connection connection, List<RYMImportRating> knownAlbums, long ownerId);

    void cleanUp(Connection connection);

    AlbumRatings getRatingsByName(Connection connection, long idLong, String album, long artistId);

    void deletePartialTempTable(Connection connection, Set<Long> idsToWipe);

    Collection<AlbumRatings> getArtistRatings(Connection connection, long guildId, long artistId);


    List<ScoredAlbumRatings> getGlobalTopRatings(Connection connection);

    List<ScoredAlbumRatings> getServerTopRatings(Connection connection, long guildId);

    List<ScoredAlbumRatings> getSelfRatingsScore(Connection connection, Short ratingNumber, long discordId);

    RymStats getUserRymStatms(Connection connection, long discordId);


    RymStats getServerStats(Connection connection, long guildId);

    RymStats getRYMBotStats(Connection connection);

    List<AlbumPlays> unratedAlbums(Connection connection, long discordId);
}
