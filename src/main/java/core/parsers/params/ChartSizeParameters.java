package core.parsers.params;

import core.parsers.utils.CustomTimeFrame;
import dao.entities.ChartMode;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ChartSizeParameters extends ChartParameters {

    public ChartSizeParameters(MessageReceivedEvent e, int x, int y, ChartMode chartMode, LastFMData lastFMData) {
        super(e, lastFMData, CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL), x, y);
    }
}
