package core.parsers.params;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TwoArtistParams extends CommandParameters {
    private final String firstArtist;
    private final String secondArtist;

    public TwoArtistParams(MessageReceivedEvent e, String firstArtist, String secondArtist) {
        super(e);
        this.firstArtist = firstArtist;
        this.secondArtist = secondArtist;
    }

    public String getFirstArtist() {
        return firstArtist;
    }

    public String getSecondArtist() {
        return secondArtist;
    }
}
