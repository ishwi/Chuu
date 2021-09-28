package core.parsers.params;

import core.commands.Context;
import dao.entities.LastFMData;

public class ArtistAlbumUrlParameters extends ArtistAlbumParameters {
    private final String url;

    public ArtistAlbumUrlParameters(Context e, String artist, String album, LastFMData user, String url) {
        super(e, artist, album, user);
        this.url = url;

    }

    public String getUrl() {
        return url;
    }
}
