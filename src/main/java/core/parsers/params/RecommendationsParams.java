package core.parsers.params;

import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RecommendationsParams extends TwoUsersParamaters {
    private final boolean noUser;
    private final int recCount;

    public RecommendationsParams(MessageReceivedEvent e, LastFMData firstUser, LastFMData secondUser, boolean noUser, int recCount) {
        super(e, firstUser, secondUser);
        this.noUser = noUser;
        this.recCount = recCount;
    }

    public boolean isNoUser() {
        return noUser;
    }


    public boolean isShowRepeated() {
        return hasOptional("repeated");
    }

    public int getRecCount() {
        return recCount;
    }
}
