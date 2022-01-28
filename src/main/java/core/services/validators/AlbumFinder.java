package core.services.validators;

import core.apis.last.ConcurrentLastFM;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.exceptions.LastFmException;
import dao.ChuuService;
import dao.entities.Album;

import java.util.Optional;

public record AlbumFinder(ChuuService db, ConcurrentLastFM lastFM, TrackValidator trackValidator) {

    private static final Spotify spotifyApi = SpotifySingleton.getInstance();


    public AlbumFinder(ChuuService db, ConcurrentLastFM lastFM) {
        this(db, lastFM, new TrackValidator(db, lastFM));
    }

    public Optional<Album> find(String artist, String song) {
        try {
            var validate = trackValidator.validate(artist, song);
            return db.findAlbumByTrackId(validate.getTrackId());
        } catch (LastFmException e) {
            return Optional.empty();
        }
    }

    public Optional<Album> findSpotify(String artist, String song) {
        return spotifyApi.findAlbum(artist, song);
    }
}
