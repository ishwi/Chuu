package core.parsers.params;

import core.commands.Context;
import dao.entities.LastFMData;

public class RecommendationsParams extends TwoUsersParamaters {
    private final boolean noUser;
    private final int recCount;

    public RecommendationsParams(Context e, LastFMData firstUser, LastFMData secondUser, boolean noUser, int recCount) {
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
