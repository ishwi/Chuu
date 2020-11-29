package core.services;

import com.google.common.collect.Multimaps;
import core.apis.last.ConcurrentLastFM;
import core.apis.last.LastFMFactory;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.LastFmEntityNotFoundException;
import core.exceptions.LastFmException;
import dao.ChuuService;
import dao.entities.FullAlbumEntity;
import dao.entities.ScrobbledAlbum;
import dao.entities.Track;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class TracklistService {
    private final ChuuService service;
    private final ConcurrentLastFM lastFM;
    private final Spotify spotifyApi;
    private final MusicBrainzService mb;

    public TracklistService(ChuuService service) {
        this.service = service;
        this.lastFM = LastFMFactory.getNewInstance();
        this.spotifyApi = SpotifySingleton.getInstance();
        this.mb = MusicBrainzServiceSingleton.getInstance();
    }

    public Optional<FullAlbumEntity> getTrackList(ScrobbledAlbum scrobbledAlbum, String lastfmId, @Nullable String artistUrl) throws LastFmException {
        Optional<FullAlbumEntity> opt = service.getAlbumTrackList(scrobbledAlbum.getAlbumId(), lastfmId);
        FullAlbumEntity fullAlbumEntity;
        String artist = scrobbledAlbum.getArtist();
        String album = scrobbledAlbum.getAlbum();
        long albumId = scrobbledAlbum.getAlbumId();
        long artistId = scrobbledAlbum.getArtistId();

        if (opt.isEmpty()) {
            Set<Track> trackList;
            try {
                fullAlbumEntity = lastFM.getAlbumSummary(lastfmId, artist, album);
                trackList = new HashSet<>();
                handleList(trackList, fullAlbumEntity.getTrackList(), lastfmId, albumId, artistId);
                fullAlbumEntity.setTrackList(new ArrayList<>(trackList));
            } catch (LastFmEntityNotFoundException ex)
            //If it doesnt exists on last.fm we do a little workaround
            {
                int artistPlays = service.getArtistPlays(scrobbledAlbum.getArtistId(), lastfmId);
                fullAlbumEntity = new FullAlbumEntity(artist, album, artistPlays, null, lastfmId);
            }
            trackList = new HashSet<>(fullAlbumEntity.getTrackList());

            if (trackList.isEmpty()) {
                List<Track> spoList = spotifyApi.getAlbumTrackList(artist, album);
                handleList(trackList, spoList, lastfmId, albumId, artistId);
            }
            if (trackList.isEmpty()) {
                if (fullAlbumEntity.getMbid() != null && !fullAlbumEntity.getMbid().isBlank()) {
                    List<Track> albumTrackListMbid = mb.getAlbumTrackListMbid(fullAlbumEntity.getMbid());
                    handleList(trackList, albumTrackListMbid, lastfmId, albumId, artistId);
                }
                if (trackList.isEmpty()) {
                    List<Track> albumTrackList = mb.getAlbumTrackList(fullAlbumEntity.getArtist(), fullAlbumEntity.getAlbum());
                    handleList(trackList, albumTrackList, lastfmId, albumId, artistId);


                    if (trackList.isEmpty()) {
                        //Force it to lowerCase
                        List<Track> albumTrackListLowerCase = mb.getAlbumTrackListLowerCase(fullAlbumEntity.getArtist(), fullAlbumEntity.getAlbum());
                        handleList(trackList, albumTrackListLowerCase, lastfmId, albumId, artistId);
                        if (trackList.isEmpty()) {
                            //If is still empty well fuck it
                            return Optional.empty();
                        }
                    }

                }

            }
            fullAlbumEntity.setTrackList(trackList.stream().sorted(Comparator.comparingInt(Track::getPosition)).collect(Collectors.toList()));
            List<Track> handler = new ArrayList<>(trackList);


            List<Track> collect = Multimaps.index(handler, Track::getPosition)
                    .asMap().values().stream()
                    .map(value -> {
                        Optional<Track> max = value.stream().max(Comparator.comparingInt(Track::getPlays));
                        return max.orElse(null);
                    }).filter(Objects::nonNull).sorted(Comparator.comparingInt(Track::getPosition))
                    .collect(Collectors.toList());
            if (trackList.stream().mapToInt(Track::getPlays).sum() <= collect.stream().mapToInt(Track::getPlays).sum()) {
                fullAlbumEntity.setTrackList(collect);
            }
        } else {
            fullAlbumEntity = opt.get();
        }
        fullAlbumEntity.setArtistUrl(artistUrl);
        return Optional.of(fullAlbumEntity);
    }

    private void handleList(Set<Track> trackList, List<Track> listToHandle, String lastfmName, long albumId, long artistId) {

        if (!listToHandle.isEmpty()) {
            service.storeTrackList(albumId, artistId, new HashSet<>(listToHandle));
            Optional<FullAlbumEntity> albumTrackList = service.getAlbumTrackList(albumId, lastfmName);
            albumTrackList.ifPresent((t) -> trackList.addAll(t.getTrackList()));
        }
    }
}
