package core.parsers.params;

import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TimeFrameParameters extends UserParameters {
    private final TimeFrameEnum time;

    public TimeFrameParameters(String[] message, MessageReceivedEvent e, OptionalParameter... opts) {
        super(message, e, opts);
        this.time = TimeFrameEnum.fromCompletePeriod(message[2]);
    }

    public TimeFrameEnum getTime() {

        return time;
    }
}
