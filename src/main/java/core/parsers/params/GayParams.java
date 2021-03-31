package core.parsers.params;

import core.parsers.utils.CustomTimeFrame;
import dao.entities.GayType;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;


public class GayParams extends ChartParameters {


    private int cols;
    private GayType gayType;

    public GayParams(MessageReceivedEvent e, LastFMData lastFMData, CustomTimeFrame timeFrameEnum, int x, int y, int cols, GayType gayType) {
        super(e, lastFMData, timeFrameEnum, x, y);
        this.cols = cols;
        this.gayType = gayType;
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
        return this.cols;
    }

    public void setX(int x) {
        this.cols = x;
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

    public void setGayType(GayType gayType) {
        this.gayType = gayType;
    }
}
