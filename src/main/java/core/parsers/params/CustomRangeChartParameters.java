package core.parsers.params;

import core.parsers.utils.CustomTimeFrame;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CustomRangeChartParameters extends ChartParameters {

    private final CustomTimeFrame customTimeFrame;

    public CustomRangeChartParameters(MessageReceivedEvent e, LastFMData lastFMData, CustomTimeFrame timeFrameEnum, int x, int y, CustomTimeFrame customTimeFrame) {
        super(e, lastFMData, timeFrameEnum, x, y);
        this.customTimeFrame = customTimeFrame;
    }

    public CustomTimeFrame getCustomTimeFrame() {
        return customTimeFrame;
    }

}
