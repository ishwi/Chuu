package core.services;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.ConcurrentLastFM;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import dao.ChuuService;
import dao.entities.NowPlayingArtist;
import dao.entities.ScrobbledAlbum;
import dao.entities.ScrobbledArtist;
import dao.entities.ScrobbledTrack;

public record TrackValidator(ChuuService db, ConcurrentLastFM lastFM) {
    private static final DiscogsApi discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
    private static final Spotify spotifyApi = SpotifySingleton.getInstance();

    public ScrobbledTrack validate(String artist, String track) throws LastFmException {
        ScrobbledArtist sa = new ScrobbledArtist(artist, 0, "");
        CommandUtil.validate(db, sa, lastFM, discogsApi, spotifyApi);
        long l = CommandUtil.trackValidate(db, sa, lastFM, track);
        ScrobbledTrack scrobbledTrack = new ScrobbledTrack(artist, track, 0, false, -1, null, null, null);
        scrobbledTrack.setArtistId(sa.getArtistId());
        scrobbledTrack.setTrackId(l);
        return scrobbledTrack;
    }

    public ScrobbledTrack validate(long artistId, String artist, String track) throws LastFmException {
        ScrobbledArtist sa = new ScrobbledArtist(artist, 0, "");
        sa.setArtistId(artistId);
        long l = CommandUtil.trackValidate(db, sa, lastFM, track);
        ScrobbledTrack scrobbledTrack = new ScrobbledTrack(artist, track, 0, false, -1, null, null, null);
        scrobbledTrack.setArtistId(sa.getArtistId());
        scrobbledTrack.setTrackId(l);
        return scrobbledTrack;
    }

    public ScrobbledTrack fromNP(NowPlayingArtist np) throws LastFmException {
        if (np.songName() == null) {
            if (np.albumName() == null) {
                ScrobbledArtist sA = CommandUtil.onlyCorrection(db, np.artistName(), lastFM, true);
                ScrobbledTrack sT = new ScrobbledTrack(sA.getArtist(), null, sA.getCount(), true, 0, null, null, null);
                sT.setArtistId(sA.getArtistId());
                return sT;
            }
            ScrobbledAlbum sAb = new AlbumValidator(db, lastFM).validate(np.artistName(), np.albumName());
            ScrobbledTrack sT = new ScrobbledTrack(sAb.getArtist(), null, sAb.getCount(), false, 0, null, null, null);
            sT.setAlbum(sAb.getAlbum());
            sT.setAlbumId(sAb.getAlbumId());
            return sT;
        }
        var st = validate(np.artistName(), np.songName());
        if (np.albumName() != null) {
            ScrobbledAlbum album = new AlbumValidator(db, lastFM).validate(st.getArtistId(), st.getArtist(), st.getAlbum());
            st.setAlbumId(st.getAlbumId());
        }
        return st;
    }
}
