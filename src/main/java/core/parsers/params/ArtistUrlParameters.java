package core.parsers.params;

import core.commands.Context;
import dao.entities.LastFMData;

public class ArtistUrlParameters extends ArtistParameters {
    private final String url;

    public ArtistUrlParameters(Context e, String artist, LastFMData user, String url) {
        super(e, artist, user);
        this.url = url;

    }

    public String getUrl() {
        return url;
    }
}
