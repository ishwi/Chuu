package core.parsers.params;

import core.commands.Context;
import core.parsers.utils.CustomTimeFrame;
import dao.entities.LastFMData;

public class CustomRangeChartParameters extends ChartParameters {

    private final CustomTimeFrame customTimeFrame;

    public CustomRangeChartParameters(Context e, LastFMData lastFMData, CustomTimeFrame timeFrameEnum, int x, int y, CustomTimeFrame customTimeFrame) {
        super(e, lastFMData, timeFrameEnum, x, y);
        this.customTimeFrame = customTimeFrame;
    }

    public CustomTimeFrame getCustomTimeFrame() {
        return customTimeFrame;
    }

}
