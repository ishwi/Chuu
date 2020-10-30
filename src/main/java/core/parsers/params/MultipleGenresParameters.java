package core.parsers.params;

import dao.entities.LastFMData;
import dao.entities.SearchMode;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Set;

public class MultipleGenresParameters extends ChuuDataParams {
    private final Set<String> genres;


    public MultipleGenresParameters(MessageReceivedEvent e, LastFMData lastFMData, Set<String> genres) {
        super(e, lastFMData);
        this.genres = genres;
    }

    public Set<String> getGenres() {
        return genres;
    }

    public SearchMode getMode() {
        return hasOptional("any") ? SearchMode.INCLUSIVE : SearchMode.EXCLUSIVE;
    }
}
