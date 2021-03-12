package core.parsers.params;

import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ArtistAlbumUrlParameters extends ArtistAlbumParameters {
    private final String url;

    public ArtistAlbumUrlParameters(MessageReceivedEvent e, String artist, String album, LastFMData user, String url) {
        super(e, artist, album, user);
        this.url = url;

    }

    public String getUrl() {
        return url;
    }
}
