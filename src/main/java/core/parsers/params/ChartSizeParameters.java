package core.parsers.params;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ChartSizeParameters extends ChartParameters {

    public ChartSizeParameters(MessageReceivedEvent e, int x, int y) {
        super(e, "", -1, null, x, y);
    }
}
