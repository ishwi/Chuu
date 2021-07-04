package core.services.validators;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.ConcurrentLastFM;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import dao.ChuuService;
import dao.entities.*;
import dao.exceptions.InstanceNotFoundException;
import org.apache.commons.lang3.tuple.Pair;
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
        scrobbledTrack.setTrackId(l.getLeft());
        scrobbledTrack.setImageUrl(l.getRight().getImageUrl());
        scrobbledTrack.setName(l.getRight().getName());
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
            ScrobbledAlbum album = new AlbumValidator(db, lastFM).validate(st.getArtistId(), st.getArtist(), st.getAlbum());
            st.setAlbumId(st.getAlbumId());
        }
        return st;
    }


    private Pair<Long, Track> trackValidate(ScrobbledArtist artist, String track) throws LastFmException {
        try {
            return db.findTrackByName(artist.getArtistId(), track);
        } catch (InstanceNotFoundException exception) {
            Track trackInfo = lastFM.getTrackInfo(LastFMData.ofDefault(), artist.getArtist(), track);
            ScrobbledTrack scrobbledTrack = new ScrobbledTrack(artist.getArtist(), track, 0, false, trackInfo.getDuration(), trackInfo.getImageUrl(), null, trackInfo.getMbid());
            scrobbledTrack.setArtistId(artist.getArtistId());
            db.insertTrack(scrobbledTrack);
            return Pair.of(scrobbledTrack.getTrackId(), trackInfo);
        }
    }
}
