package core.parsers.params;

import dao.entities.ChartMode;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ChartSizeParameters extends ChartParameters {

    public ChartSizeParameters(MessageReceivedEvent e, int x, int y, ChartMode chartMode) {
        super(e, "", -1, chartMode, null, x, y);
    }
}
