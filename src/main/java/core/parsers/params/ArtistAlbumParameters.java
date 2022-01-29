package core.parsers.params;

import core.commands.Context;
import dao.entities.LastFMData;
import dao.entities.ScrobbledAlbum;
import dao.entities.ScrobbledArtist;

public class ArtistAlbumParameters extends ChuuDataParams {
    private final String artist;
    private final String album;
    private ScrobbledArtist scrobbledArtist;
    private ScrobbledAlbum scrobbledAlbum;

    public ArtistAlbumParameters(Context e, String artist, String album, LastFMData lastFMData) {
        super(e, lastFMData);
        this.artist = artist;
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }


    public ScrobbledArtist getScrobbledArtist() {
        return scrobbledArtist;
    }

    public void setScrobbledArtist(ScrobbledArtist scrobbledArtist) {
        this.scrobbledArtist = scrobbledArtist;
    }

    public ScrobbledAlbum getScrobbledAlbum() {
        return scrobbledAlbum;
    }

    public void setScrobbledAlbum(ScrobbledAlbum scrobbledAlbum) {
        this.scrobbledAlbum = scrobbledAlbum;
    }

    public String getAlbum() {
        return album;
    }

    public boolean isNoredirect() {
        return hasOptional("noredirect");
    }

}
