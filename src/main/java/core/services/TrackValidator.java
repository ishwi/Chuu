package core.services;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.ConcurrentLastFM;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import dao.ChuuService;
import dao.entities.ScrobbledArtist;
import dao.entities.ScrobbledTrack;

public record TrackValidator(ChuuService db, ConcurrentLastFM lastFM) {
    private static final DiscogsApi discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
    private static final Spotify spotifyApi = SpotifySingleton.getInstance();

    public ScrobbledTrack validate(String artist, String track) throws LastFmException {
        ScrobbledArtist sa = new ScrobbledArtist(artist, 0, "");
        CommandUtil.validate(db, sa, lastFM, discogsApi, spotifyApi);
        long l = CommandUtil.trackValidate(db, sa, lastFM, track);
        return new ScrobbledTrack(artist, track, 0, false, -1, null, null, null);
    }

    public ScrobbledTrack validate(long artistId, String artist, String track) throws LastFmException {
        ScrobbledArtist sa = new ScrobbledArtist(artist, 0, "");
        sa.setArtistId(artistId);
        long l = CommandUtil.trackValidate(db, sa, lastFM, track);
        return new ScrobbledTrack(artist, track, 0, false, -1, null, null, null);
    }
}
