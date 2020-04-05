package core.parsers.params;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TopParameters extends ChartParameters {
    private final boolean doArtist;

    public TopParameters(String[] returned, MessageReceivedEvent e) {
        super(returned, e, new OptionalParameter("--artist", 9));
        doArtist = hasOptional("--artist");


    }


    @Override
    public boolean isWritePlays() {
        return !super.isWritePlays();
    }

    public boolean isDoArtist() {
        return doArtist;
    }
}
