package core.services;

import com.wrapper.spotify.model_objects.specification.Track;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import dao.ChuuService;
import dao.entities.ScrobbledTrack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class SpotifyTrackService {
    private final Spotify spotifyApi;
    private final ChuuService service;
    private final String lastfmId;

    public SpotifyTrackService(ChuuService service, String lastfmId) {
        this.lastfmId = lastfmId;
        this.spotifyApi = SpotifySingleton.getInstance();
        this.service = service;
    }

    public List<ScrobbledTrack> getTracksWithId() {
        List<ScrobbledTrack> topTracks = service.getTopTracksNoSpotifyId(lastfmId, 50);
        List<Pair<ScrobbledTrack, Track>> pairs = spotifyApi.searchMultipleTracks(topTracks);
        pairs.forEach(x -> {
            ScrobbledTrack left = x.getLeft();
            Track right = x.getRight();
            left.setSpotifyId(right.getId());
            left.setDuration(right.getDurationMs() / 1000);
            service.updateSpotifyInfo(left.getTrackId(), right.getId(), right.getDurationMs() / 1000, right.getPopularity());
        });
        return service.getTopSpotifyTracksIds(lastfmId, 500);
    }

    public List<Pair<ScrobbledTrack, Track>> searchTracks(List<ScrobbledTrack> tracks) {
        List<Pair<ScrobbledTrack, Track>> pairs = spotifyApi.searchMultipleTracks(tracks);
        pairs.forEach(x -> {
            ScrobbledTrack left = x.getLeft();
            Track right = x.getRight();
            left.setSpotifyId(right.getId());
            left.setDuration(right.getDurationMs() / 1000);
            service.updateSpotifyInfo(left.getTrackId(), right.getId(), right.getDurationMs() / 1000, right.getPopularity());
        });
        return pairs;
    }
}
