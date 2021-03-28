package dao;

import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;

import java.sql.Connection;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    String getAlbumUrlByName(Connection connection, String name, long artistId) throws InstanceNotFoundException;

    List<AlbumInfo> get(Connection connection, List<AlbumInfo> albumes, Year year);

    List<ScrobbledAlbum> getUserAlbumsOfYear(Connection connection, String username, Year year);


    List<ScrobbledAlbum> getUserAlbumsWithNoYear(Connection connection, String username, int limit);

    void insertAlbumsOfYear(Connection connection, List<AlbumInfo> foundByYear, Year year);


    Map<Year, Integer> countByYears(Connection connection, String lastfmId, List<AlbumInfo> albumInfos);

    Map<Year, Integer> countByDecades(Connection connection, String lastfmId, List<AlbumInfo> albumInfos);

    Set<String> getAlbumTags(Connection connection, long artistId);

}
