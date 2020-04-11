package core.parsers.params;

import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ArtistTimeFrameParameters extends ArtistParameters {
    private final TimeFrameEnum timeFrame;

    public ArtistTimeFrameParameters(MessageReceivedEvent e, String artist, User user, TimeFrameEnum timeFrame) {
        super(e, artist, user);
        this.timeFrame = timeFrame;
    }


    public TimeFrameEnum getTimeFrame() {
        return timeFrame;
    }
}
