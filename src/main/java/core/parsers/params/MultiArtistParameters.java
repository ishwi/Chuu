package core.parsers.params;

import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Set;

public class MultiArtistParameters extends ChuuDataParams {
    private final Set<String> artists;

    public MultiArtistParameters(MessageReceivedEvent e, LastFMData lastFMData, Set<String> artists) {
        super(e, lastFMData);
        this.artists = artists;
    }


    public Set<String> getArtists() {
        return artists;
    }
}
