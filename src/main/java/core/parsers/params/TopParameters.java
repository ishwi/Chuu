package core.parsers.params;

import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TopParameters extends ChartParameters {

    public TopParameters(MessageReceivedEvent e, String lastfmID, long discordId, TimeFrameEnum timeFrameEnum, int x, int y) {
        super(e, lastfmID, discordId, timeFrameEnum, x, y);

    }


    @Override
    public boolean isWritePlays() {
        return !super.isWritePlays();
    }

    public boolean isDoArtist() {
        return hasOptional("--artist");
    }
}
