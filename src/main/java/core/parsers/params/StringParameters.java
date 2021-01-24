package core.parsers.params;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class StringParameters extends CommandParameters {
    private final String value;

    public StringParameters(MessageReceivedEvent e, String value) {
        super(e);
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
