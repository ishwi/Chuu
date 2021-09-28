package core.parsers.params;

import core.commands.Context;
import dao.entities.LastFMData;
import dao.entities.SearchMode;

import java.util.Set;

public class MultipleGenresParameters extends ChuuDataParams {
    private final Set<String> genres;


    public MultipleGenresParameters(Context e, LastFMData lastFMData, Set<String> genres) {
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
