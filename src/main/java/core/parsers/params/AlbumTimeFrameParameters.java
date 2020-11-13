package core.parsers.params;

import core.parsers.utils.CustomTimeFrame;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;


public class AlbumTimeFrameParameters extends ArtistAlbumParameters {
    private final CustomTimeFrame timeFrame;

    public AlbumTimeFrameParameters(MessageReceivedEvent e, String artist, String album, LastFMData lastFMData, CustomTimeFrame timeFrame) {
        super(e, artist, album, lastFMData);
        this.timeFrame = timeFrame;
    }


    public CustomTimeFrame getTimeFrame() {
        return timeFrame;
    }
}
