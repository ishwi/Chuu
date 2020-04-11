package core.parsers.params;

import dao.entities.ScrobbledArtist;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ArtistAlbumParameters extends DiscordParameters {
    private final String artist;
    private final String album;
    private ScrobbledArtist scrobbledArtist;

    public ArtistAlbumParameters(MessageReceivedEvent e, String artist, String album, User user) {
        super(e, user);
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
}
