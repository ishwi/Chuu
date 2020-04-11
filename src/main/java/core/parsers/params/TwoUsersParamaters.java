package core.parsers.params;

import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class TwoUsersParamaters extends CommandParameters {
    private final LastFMData firstUser;
    private final LastFMData secondUser;

    public TwoUsersParamaters(MessageReceivedEvent e, LastFMData firstUser, LastFMData secondUser) {
        super(e);
        this.firstUser = firstUser;
        this.secondUser = secondUser;
    }

    public LastFMData getFirstUser() {
        return firstUser;
    }

    public LastFMData getSecondUser() {
        return secondUser;
    }
}
