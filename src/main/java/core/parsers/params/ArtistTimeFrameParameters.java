package core.parsers.params;

import core.commands.Context;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;

public class ArtistTimeFrameParameters extends ArtistParameters {
    private final TimeFrameEnum timeFrame;

    public ArtistTimeFrameParameters(Context e, String artist, LastFMData user, TimeFrameEnum timeFrame) {
        super(e, artist, user);
        this.timeFrame = timeFrame;
    }


    public TimeFrameEnum getTimeFrame() {
        return timeFrame;
    }
}
