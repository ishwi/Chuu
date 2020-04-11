package core.parsers.params;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class UrlParameters extends CommandParameters {
    private final String url;

    public UrlParameters(MessageReceivedEvent e, String url) {
        super(e);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
