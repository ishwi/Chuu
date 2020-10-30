package dao;

import dao.entities.AlbumInfo;
import dao.entities.ArtistInfo;
import dao.entities.ScrobbledAlbum;
import dao.entities.ScrobbledArtist;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface DiscoveralDao {
    void setDiscoveredAlbumTempTable(Connection connection, Collection<ScrobbledAlbum> scrobbledArtists, String lastfmId);

    void setDiscoveredArtistTempTable(Connection connection, Collection<ScrobbledArtist> scrobbledAlbums, String lastfmId);

    List<AlbumInfo> calculateDiscoveryFromAlbumTemp(Connection connection, String lastfmId);

    Set<ArtistInfo> calculateDiscoveryFromArtistTemp(Connection connection, String lastfmId);

    void deleteDiscoveryAlbumTempTable(Connection connection);

    void deleteDiscoveryArtistTable(Connection connection);
}
