package core.parsers.params;

import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class UserStringParameters extends ChuuDataParams {
    private final String value;

    public UserStringParameters(MessageReceivedEvent e, LastFMData lastFMData, String value) {
        super(e, lastFMData);
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
