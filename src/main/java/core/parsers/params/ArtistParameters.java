package core.parsers.params;

import dao.entities.ScrobbledArtist;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ArtistParameters extends CommandParameters {
    private final String artist;
    private final long discordId;
    private ScrobbledArtist scrobbledArtist;

    public ArtistParameters(String[] message, MessageReceivedEvent e, OptionalParameter... opts) {
        super(message, e, opts);
        this.artist = message[0];
        this.discordId = Long.parseLong(message[1]);
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
}
