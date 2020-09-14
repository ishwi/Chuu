package core.parsers.params;

import dao.entities.ChartMode;
import dao.entities.GayType;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;


public class GayParams extends ChartParameters {


    private int x;
    private final GayType gayType;

    public GayParams(MessageReceivedEvent e, String lastfmID, long discordId, GayType gayType, TimeFrameEnum timeFrameEnum, int y, int x, ChartMode chartMode, LastFMData lastFMData) {
        super(e, lastfmID, discordId, chartMode, lastFMData, timeFrameEnum, x, y);
        this.gayType = gayType;
        this.x = x;
    }

    @Override
    public boolean isWritePlays() {
        return hasOptional("plays");
    }

    @Override
    public boolean isWriteTitles() {
        return hasOptional("titles");
    }

    public boolean isArtist() {
        return hasOptional("artist");
    }


    @Override
    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }


    @Override
    public boolean isPieFormat() {
        return false;
    }

    @Override
    public boolean isList() {
        return false;
    }


    public GayType getGayType() {
        return gayType;
    }
}
