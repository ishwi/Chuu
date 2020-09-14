package core.parsers.params;

import dao.entities.ChartMode;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RainbowParams extends ChartParameters {
    private int x;
    private int y;

    public RainbowParams(MessageReceivedEvent e, String lastfmID, long discordId, TimeFrameEnum timeFrameEnum, int x, int y, ChartMode chartMode, LastFMData lastFMData) {
        super(e, lastfmID, discordId, chartMode, lastFMData, timeFrameEnum, x, y);
        this.x = x;
        this.y = y;
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

    public boolean isInverse() {
        return hasOptional("inverse");
    }

    public boolean isColumn() {
        return hasOptional("column");
    }

    public boolean isLinear() {
        return hasOptional("linear");
    }

    @Override
    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isColor() {
        return hasOptional("color");
    }

    @Override
    public boolean isPieFormat() {
        return false;
    }

    @Override
    public boolean isList() {
        return false;
    }
}
