package core.parsers.params;

import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TopParameters extends ChartParameters {
    private final boolean doArtist;

    public TopParameters(String[] returned, MessageReceivedEvent e) {
        super(returned, e);
        doArtist = Boolean.parseBoolean(returned[9]);


    }

    public TopParameters(String username, long discordId, int x, int y, MessageReceivedEvent e, boolean writeTitles, boolean writePlays, boolean isList, boolean isPie, boolean doArtist) {
        super(username, discordId, TimeFrameEnum.ALL, x, y, e, writeTitles, writePlays, isList, isPie);
        this.doArtist = doArtist;
    }

    @Override
    public boolean isWritePlays() {
        return !super.isWritePlays();
    }

    public boolean isDoArtist() {
        return doArtist;
    }
}
