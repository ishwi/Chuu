package core.parsers.params;

import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RYMRatingParams extends ChuuDataParams {
    private final Short rating;

    public RYMRatingParams(MessageReceivedEvent e, LastFMData lastFMData, Short rating) {
        super(e, lastFMData);
        this.rating = rating;
    }

    public Short getRating() {
        return rating;
    }
}
