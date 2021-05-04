package core.parsers.params;

import core.commands.Context;
import core.parsers.utils.CustomTimeFrame;
import dao.entities.LastFMData;

public class ChartGroupParameters extends ChartParameters {
    private final boolean showTime;

    public ChartGroupParameters(Context e, LastFMData lastFMData, CustomTimeFrame timeFrameEnum, int x, int y, boolean showTime) {
        super(e, lastFMData, timeFrameEnum, x, y);
        this.showTime = showTime;
    }

    public ChartGroupParameters(Context e, LastFMData user, CustomTimeFrame timeFrameEnum, int x, int y, boolean writeTitles, boolean writePlays, boolean isList, boolean showTime) {
        super(e, user, timeFrameEnum, x, y, writeTitles, writePlays, isList);
        this.showTime = showTime;
    }

    public boolean isShowTime() {
        return showTime;
    }

}
