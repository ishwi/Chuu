package core.parsers.params;

import dao.entities.ScrobbledArtist;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ArtistAlbumParameters extends CommandParameters {
    private final String artist;
    private final String album;
    private final long discordId;
    private ScrobbledArtist scrobbledArtist;

    public ArtistAlbumParameters(String[] message, MessageReceivedEvent e, OptionalParameter... opts) {
        super(message, e, opts);
        this.artist = message[0];
        this.album = message[1];
        this.discordId = Long.parseLong(message[2]);
    }


    public String getArtist() {
        return artist;
    }

    public long getDiscordId() {
        return discordId;
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
