package core.parsers.params;

import core.parsers.utils.CustomTimeFrame;
import dao.entities.ChartMode;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CustomRangeChartParameters extends ChartParameters {

    private final CustomTimeFrame customTimeFrame;

    public CustomRangeChartParameters(MessageReceivedEvent e, LastFMData name, Long discordId, ChartMode chartMode, LastFMData data, CustomTimeFrame timeFrame, int x, int y) {
        super(e, name, discordId, chartMode, data, null, x, y);
        customTimeFrame = timeFrame;


    }

    public CustomTimeFrame getCustomTimeFrame() {
        return customTimeFrame;
    }

}
