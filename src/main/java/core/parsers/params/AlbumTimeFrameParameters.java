package core.parsers.params;

import core.commands.Context;
import core.parsers.utils.CustomTimeFrame;
import dao.entities.LastFMData;


public class AlbumTimeFrameParameters extends ArtistAlbumParameters {
    private final CustomTimeFrame timeFrame;

    public AlbumTimeFrameParameters(Context e, String artist, String album, LastFMData lastFMData, CustomTimeFrame timeFrame) {
        super(e, artist, album, lastFMData);
        this.timeFrame = timeFrame;
    }


    public CustomTimeFrame getTimeFrame() {
        return timeFrame;
    }
}
