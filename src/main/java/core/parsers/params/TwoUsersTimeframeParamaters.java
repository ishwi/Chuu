package core.parsers.params;

import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TwoUsersTimeframeParamaters extends TwoUsersParamaters {
    private final TimeFrameEnum timeFrameEnum;

    public TwoUsersTimeframeParamaters(MessageReceivedEvent e, LastFMData firstUser, LastFMData secondUser, TimeFrameEnum timeFrameEnum) {
        super(e, firstUser, secondUser);
        this.timeFrameEnum = timeFrameEnum;
    }

    public TimeFrameEnum getTimeFrameEnum() {
        return timeFrameEnum;
    }
}
