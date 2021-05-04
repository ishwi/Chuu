package core.parsers.params;

import core.commands.Context;
import dao.entities.LastFMData;

import java.util.Set;

public class MultiArtistParameters extends ChuuDataParams {
    private final Set<String> artists;

    public MultiArtistParameters(Context e, LastFMData lastFMData, Set<String> artists) {
        super(e, lastFMData);
        this.artists = artists;
    }


    public Set<String> getArtists() {
        return artists;
    }
}
