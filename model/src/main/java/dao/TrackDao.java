package dao;

import dao.entities.Album;
import dao.entities.AlbumInfo;
import dao.entities.AlbumUserPlays;
import dao.entities.FullAlbumEntity;
import dao.entities.Genre;
import dao.entities.IdTrack;
import dao.entities.ResultWrapper;
import dao.entities.ScrobbledArtist;
import dao.entities.ScrobbledTrack;
import dao.entities.Track;
import dao.entities.UnheardCount;
import dao.exceptions.InstanceNotFoundException;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface TrackDao {
    List<ScrobbledTrack> getUserTopTracksNoSpotifyId(Connection connection, String lastfmid, int limit);

    void fillIds(Connection connection, List<ScrobbledTrack> list);

    void deleteAllUserTracks(Connection con, String lastfmId);

    void insertTracks(Connection connection, List<ScrobbledTrack> nonExistingId);

    void insertTrack(Connection connection, ScrobbledTrack x);

    long getTrackIdByName(Connection connection, String track, long artistId) throws InstanceNotFoundException;


    void addSrobbledTracks(Connection con, List<ScrobbledTrack> scrobbledAlbums);

    List<Track> getUserTopArtistTracksDuration(Connection connection, String lastfmId, long artistId, int limit);

    List<Track> getUserTopArtistTracks(Connection connection, String lastfmId, long artistId, int limit);

    Map<ScrobbledArtist, Long> getUserTopArtistByDuration(Connection connection, String lastfmId, int limit);

    List<AlbumUserPlays> getServerTopArtistTracks(Connection connection, long guildId, long artistId, int limit);


    List<AlbumUserPlays> getGlobalTopArtistTracks(Connection connection, long artistId, int limit);

    Map<Genre, Integer> genreCountsByTracks(Connection connection, List<AlbumInfo> albumInfos);

    String getTrackUrlByName(Connection connection, String name, long artistId);

    void fillIdsMbids(Connection connection, List<ScrobbledTrack> list);

    Optional<Album> findAlbumFromTrack(Connection connection, long trackId);

    Optional<FullAlbumEntity> getAlbumTrackList(Connection connection, long albumId, String lastfmId);

    ScrobbledTrack getUserTrackInfo(Connection connection, String lastfmid, long trackId);

    List<ScrobbledTrack> getUserTopTracks(Connection connection, String lastfmid, Integer limit);

    List<ScrobbledTrack> getTopSpotifyTracksIds(Connection connection, String lastfmId, int limit);


    void storeTrackList(Connection connection, long albumId, List<ScrobbledTrack> trackList);

    Optional<FullAlbumEntity> getServerAlbumTrackList(Connection connection, long albumId, long guildId);

    Optional<FullAlbumEntity> getGlobalAlbumTrackList(Connection connection, long albumId);

    ResultWrapper<ScrobbledTrack> getGuildTopTracks(Connection connection, Long guildID, int limit, boolean doCount);

    Set<String> getTrackTags(Connection connection, Long trackId);


    void updateLovedSongs(Connection connection, Set<Long> ids, boolean loved, String lastfmId);

    void resetLovedSongs(Connection connection, String lastfm);


    IdTrack findTrackByName(Connection connection, String track, long artistId) throws InstanceNotFoundException;

    void deleteTracklist(Connection connection, long albumId);


    List<UnheardCount> getUnheardSongs(Connection connection, String lastFmName, long artistId, boolean listeners, Long filter);

    List<Track> getUserTracksByLength(Connection connection, String lastfmId, boolean longestFirst, Integer limit);

}
