package core.parsers.params;

import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TimeFrameParameters extends ChuuDataParams {
    private final TimeFrameEnum time;

    public TimeFrameParameters(MessageReceivedEvent e, LastFMData lastFMData, TimeFrameEnum time) {
        super(e, lastFMData);
        this.time = time;
    }

    public TimeFrameEnum getTime() {

        return time;
    }
}
