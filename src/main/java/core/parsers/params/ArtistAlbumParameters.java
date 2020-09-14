package core.parsers.params;

import dao.entities.LastFMData;
import dao.entities.ScrobbledArtist;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ArtistAlbumParameters extends ChuuDataParams {
    private final String artist;
    private final String album;
    private ScrobbledArtist scrobbledArtist;

    public ArtistAlbumParameters(MessageReceivedEvent e, String artist, String album, LastFMData lastFMData) {
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

    public String getAlbum() {
        return album;
    }

    public boolean isNoredirect() {
        return hasOptional("noredirect");
    }

}
