package core.parsers.params;

import core.commands.Context;
import dao.entities.LastFMData;

public class RYMRatingParams extends ChuuDataParams {
    private final Short rating;

    public RYMRatingParams(Context e, LastFMData lastFMData, Short rating) {
        super(e, lastFMData);
        this.rating = rating;
    }

    public Short getRating() {
        return rating;
    }
}
