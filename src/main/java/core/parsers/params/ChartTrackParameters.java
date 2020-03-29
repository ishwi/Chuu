package core.parsers.params;

import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ChartTrackParameters extends ChartParameters {
    public ChartTrackParameters(String[] returned, MessageReceivedEvent e) {
        super(returned, e);
    }

    public ChartTrackParameters(String username, long discordId, TimeFrameEnum timeFrameEnum, int x, int y, MessageReceivedEvent e, boolean writeTitles, boolean writePlays, boolean isList, boolean isPie) {
        super(username, discordId, timeFrameEnum, x, y, e, writeTitles, writePlays, isList, isPie);
    }

    @Override
    public boolean isList() {
        return !super.isList();
    }

    public boolean isImage() {
        return this.isList();
    }
}
