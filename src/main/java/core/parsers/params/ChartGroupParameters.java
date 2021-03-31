package core.parsers.params;

import core.parsers.utils.CustomTimeFrame;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ChartGroupParameters extends ChartParameters {
    private final boolean showTime;

    public ChartGroupParameters(MessageReceivedEvent e, LastFMData lastFMData, CustomTimeFrame timeFrameEnum, int x, int y, boolean showTime) {
        super(e, lastFMData, timeFrameEnum, x, y);
        this.showTime = showTime;
    }

    public ChartGroupParameters(MessageReceivedEvent e, LastFMData user, CustomTimeFrame timeFrameEnum, int x, int y, boolean writeTitles, boolean writePlays, boolean isList, boolean showTime) {
        super(e, user, timeFrameEnum, x, y, writeTitles, writePlays, isList);
        this.showTime = showTime;
    }

    public boolean isShowTime() {
        return showTime;
    }

}
