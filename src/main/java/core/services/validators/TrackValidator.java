package core.services.validators;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.ConcurrentLastFM;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import dao.ChuuService;
import dao.entities.IdTrack;
import dao.entities.LastFMData;
import dao.entities.NowPlayingArtist;
import dao.entities.ScrobbledAlbum;
import dao.entities.ScrobbledArtist;
import dao.entities.ScrobbledTrack;
import dao.entities.Track;
import dao.exceptions.InstanceNotFoundException;
import org.jetbrains.annotations.NotNull;

public record TrackValidator(ChuuService db, ConcurrentLastFM lastFM) {
    private static final DiscogsApi discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
    private static final Spotify spotifyApi = SpotifySingleton.getInstance();

    public ScrobbledTrack validate(String artist, String track) throws LastFmException {
        ScrobbledArtist sa = new ScrobbledArtist(artist, 0, "");
        CommandUtil.validate(db, sa, lastFM, discogsApi, spotifyApi);
        return doValidate(artist, track, sa);
    }

    public ScrobbledTrack validate(long artistId, String artist, String track) throws LastFmException {
        ScrobbledArtist sa = new ScrobbledArtist(artist, 0, "");
        sa.setArtistId(artistId);
        return doValidate(artist, track, sa);
    }

    @NotNull
    private ScrobbledTrack doValidate(String artist, String track, ScrobbledArtist sA) throws LastFmException {
        var l = trackValidate(sA, track);
        ScrobbledTrack scrobbledTrack = new ScrobbledTrack(artist, track, 0, false, -1, null, null, null);
        scrobbledTrack.setArtistId(sA.getArtistId());
        scrobbledTrack.setTrackId(l.id());
        scrobbledTrack.setImageUrl(l.track().getImageUrl());
        scrobbledTrack.setName(l.track().getName());
        return scrobbledTrack;
    }

    public ScrobbledTrack fromNP(NowPlayingArtist np) throws LastFmException {
        if (np.songName() == null) {
            if (np.albumName() == null) {
                ScrobbledArtist sA = new ArtistValidator(db, lastFM, null).validate(np.artistName(), false, true);
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
            ScrobbledAlbum album = new AlbumValidator(db, lastFM).validate(st.getArtistId(), st.getArtist(), np.albumName());
            st.setAlbumId(album.getAlbumId());
            st.setAlbum(album.getAlbum());
        }
        return st;
    }


    private IdTrack trackValidate(ScrobbledArtist artist, String track) throws LastFmException {
        try {
            return db.findTrackByName(artist.getArtistId(), track);
        } catch (InstanceNotFoundException exception) {
            Track trackInfo = lastFM.getTrackInfo(LastFMData.ofDefault(), artist.getArtist(), track);
            ScrobbledTrack scrobbledTrack = new ScrobbledTrack(artist.getArtist(), track, 0, false, trackInfo.getDuration(), trackInfo.getImageUrl(), null, trackInfo.getMbid());
            scrobbledTrack.setArtistId(artist.getArtistId());
            db.insertTrack(scrobbledTrack);
            return new IdTrack(scrobbledTrack.getTrackId(), trackInfo);
        }
    }
}
