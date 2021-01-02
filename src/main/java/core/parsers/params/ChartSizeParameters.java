package core.parsers.params;

import dao.entities.ChartMode;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ChartSizeParameters extends ChartParameters {

    public ChartSizeParameters(MessageReceivedEvent e, int x, int y, ChartMode chartMode, LastFMData lastFMData) {
        super(e, null, -1, chartMode, lastFMData, null, x, y);
    }
}
