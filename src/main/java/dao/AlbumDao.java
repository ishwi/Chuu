package dao;

import core.exceptions.InstanceNotFoundException;
import dao.entities.*;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface AlbumDao {

    void fillIds(Connection connection, List<ScrobbledAlbum> list);

    void deleteAllUserAlbums(Connection con, String lastfmId);

    void insertAlbums(Connection connection, List<ScrobbledAlbum> nonExistingId);

    void insertLastFmAlbum(Connection connection, ScrobbledAlbum x);

    long getAlbumIdByName(Connection connection, String album, long artist_id) throws InstanceNotFoundException;

    Album getAlbumByName(Connection connection, String album, long artist_id) throws InstanceNotFoundException;

    void addSrobbledAlbums(Connection con, List<ScrobbledAlbum> scrobbledAlbums);

    List<AlbumUserPlays> getUserTopArtistAlbums(Connection connection, long discord_id, long artistId, int limit);
    List<AlbumUserPlays> getServerTopArtistAlbums(Connection connection, long guildId, long artistId, int limit);


    List<AlbumUserPlays> getGlobalTopArtistAlbums(Connection connection, long artistId, int limit);

    Map<Genre, Integer> genreCountsByAlbum(Connection connection, List<AlbumInfo> albumInfos);
}
