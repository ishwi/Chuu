package core.parsers.params;

import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ChuuDataParams extends CommandParameters {
    private final LastFMData lastFMData;

    public ChuuDataParams(MessageReceivedEvent e, LastFMData lastFMData) {
        super(e);
        this.lastFMData = lastFMData;
    }

    public LastFMData getLastFMData() {
        return lastFMData;
    }
}
