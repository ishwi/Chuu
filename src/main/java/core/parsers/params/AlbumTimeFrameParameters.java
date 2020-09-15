package core.parsers.params;

import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;


public class AlbumTimeFrameParameters extends ArtistAlbumParameters {
    private final TimeFrameEnum timeFrame;

    public AlbumTimeFrameParameters(MessageReceivedEvent e, String artist, String album, LastFMData lastFMData, TimeFrameEnum timeFrame) {
        super(e, artist, album, lastFMData);
        this.timeFrame = timeFrame;
    }


    public TimeFrameEnum getTimeFrame() {
        return timeFrame;
    }
}
