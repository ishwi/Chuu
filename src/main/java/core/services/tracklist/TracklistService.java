package core.services.tracklist;

import com.google.common.collect.Multimaps;
import core.Chuu;
import core.apis.last.ConcurrentLastFM;
import core.apis.last.LastFMFactory;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.Context;
import core.exceptions.LastFmEntityNotFoundException;
import core.exceptions.LastFmException;
import dao.ChuuService;
import dao.entities.*;
import dao.musicbrainz.MusicBrainzService;
import dao.musicbrainz.MusicBrainzServiceSingleton;

import javax.annotation.Nullable;
import java.util.*;

import static java.util.stream.Collectors.toCollection;

public abstract class TracklistService {
    final ChuuService service;
    final ConcurrentLastFM lastFM;
    final Spotify spotifyApi;
    final MusicBrainzService mb;

    public TracklistService(ChuuService service) {
        this.service = service;
        this.lastFM = LastFMFactory.getNewInstance();
        this.spotifyApi = SpotifySingleton.getInstance();
        this.mb = MusicBrainzServiceSingleton.getInstance();
    }


    public FullAlbumEntityExtended getLastFmTracklist(LastFMData lastFMData, String artist, String album) throws LastFmException {
        return lastFM.getAlbumSummary(lastFMData, artist, album);
    }

    public List<Track> getSpotifyTracklist(String artist, String album) {
        return spotifyApi.getAlbumTrackList(artist, album);
    }

    public List<Track> getMusicBrainzTracklist(String mbid, String artist, String album) {
        List<Track> toInsert = new ArrayList<>();
        if (mbid != null && !mbid.isBlank()) {
            toInsert = mb.getAlbumTrackListMbid(mbid);
        }
        if (toInsert.isEmpty()) {
            toInsert = mb.getAlbumTrackList(artist, album);


            if (toInsert.isEmpty()) {
                //Force it to lowerCase
                toInsert = mb.getAlbumTrackListLowerCase(artist, album);

            }

        }

        return toInsert;
    }


    public Optional<FullAlbumEntity> getTrackList(ScrobbledAlbum scrobbledAlbum, LastFMData lastfmId, @Nullable String artistUrl, Context event) throws LastFmException {
        Optional<FullAlbumEntity> opt = obtainTrackList(scrobbledAlbum.getAlbumId());
        FullAlbumEntity fullAlbumEntity;
        String artist = scrobbledAlbum.getArtist();
        String album = scrobbledAlbum.getAlbum();
        long albumId = scrobbledAlbum.getAlbumId();
        long artistId = scrobbledAlbum.getArtistId();

        if (opt.isEmpty()) {
            Set<Track> trackList = new HashSet<>();
            try {
                fullAlbumEntity = getLastFmTracklist(lastfmId, artist, album);
                handleList(trackList, fullAlbumEntity.getTrackList(), albumId, artistId);
                fullAlbumEntity.setTrackList(new ArrayList<>(trackList));
            } catch (LastFmEntityNotFoundException ex)
            //If it doesnt exists on last.fm we do a little workaround
            {
                int artistPlays = service.getArtistPlays(scrobbledAlbum.getArtistId(), lastfmId.getName());
                fullAlbumEntity = new FullAlbumEntity(artist, album, artistPlays, null, lastfmId.getName());
            }
            trackList = new HashSet<>(fullAlbumEntity.getTrackList());

            if (trackList.isEmpty()) {
                List<Track> spotifyTracklist = getSpotifyTracklist(artist, album);
                handleList(trackList, spotifyTracklist, albumId, artistId);
            }
            if (trackList.isEmpty()) {
                List<Track> musicBrainzTracklist = getMusicBrainzTracklist(fullAlbumEntity.getMbid(), artist, album);
                handleList(trackList, musicBrainzTracklist, albumId, artistId);
                if (trackList.isEmpty()) {
                    //If is still empty well fuck it
                    return Optional.empty();
                }
            }
            fullAlbumEntity.setTrackList(trackList.stream().sorted(Comparator.comparingInt(Track::getPosition)).collect(toCollection(ArrayList::new)));
            List<Track> handler = new ArrayList<>(trackList);


            List<Track> tracks = Multimaps.index(handler, Track::getPosition)
                    .asMap().values().stream()
                    .map(value -> {
                        Optional<Track> max = value.stream().max(Comparator.comparingInt(Track::getPlays));
                        return max.orElse(null);
                    }).filter(Objects::nonNull).sorted(Comparator.comparingInt(Track::getPosition))
                    .collect(toCollection(ArrayList::new));
            if (trackList.stream().mapToInt(Track::getPlays).sum() <= tracks.stream().mapToInt(Track::getPlays).sum()) {
                fullAlbumEntity.setTrackList(tracks);
            }
        } else {
            fullAlbumEntity = opt.get();
        }
        fullAlbumEntity.setAlbumUrl(Chuu.getCoverService().getCover(scrobbledAlbum.getAlbumId(), scrobbledAlbum.getUrl(), event));
        fullAlbumEntity.setArtistUrl(artistUrl);
        fullAlbumEntity.getTrackList().sort(Comparator.comparingInt(Track::getPosition));
        return Optional.of(fullAlbumEntity);
    }

    protected abstract Optional<FullAlbumEntity> obtainTrackList(long albumId);

    void handleList(Set<Track> trackList, List<Track> listToHandle, long albumId, long artistId) {

        if (!listToHandle.isEmpty()) {
            service.storeTrackList(albumId, artistId, new HashSet<>(listToHandle));
            Optional<FullAlbumEntity> albumTrackList = obtainTrackList(albumId);
            albumTrackList.ifPresent((t) -> trackList.addAll(t.getTrackList()));
        }
    }


}
