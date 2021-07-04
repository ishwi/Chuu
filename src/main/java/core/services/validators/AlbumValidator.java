package core.services.validators;

import core.apis.discogs.DiscogsApi;
import core.apis.discogs.DiscogsSingleton;
import core.apis.last.ConcurrentLastFM;
import core.apis.spotify.Spotify;
import core.apis.spotify.SpotifySingleton;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import dao.ChuuService;
import dao.entities.ScrobbledAlbum;
import dao.entities.ScrobbledArtist;

public record AlbumValidator(ChuuService db, ConcurrentLastFM lastFM) {
    private static final DiscogsApi discogsApi = DiscogsSingleton.getInstanceUsingDoubleLocking();
    private static final Spotify spotifyApi = SpotifySingleton.getInstance();

    public ScrobbledAlbum validate(String artist, String album) throws LastFmException {
        ScrobbledArtist sa = new ScrobbledArtist(artist, 0, "");
        CommandUtil.validate(db, sa, lastFM, discogsApi, spotifyApi);
        return CommandUtil.validateAlbum(db, sa.getArtistId(), artist, album, lastFM);
    }

    public ScrobbledAlbum validate(long artistId, String artist, String album) throws LastFmException {
        return CommandUtil.validateAlbum(db, artistId, artist, album, lastFM);
    }
}
