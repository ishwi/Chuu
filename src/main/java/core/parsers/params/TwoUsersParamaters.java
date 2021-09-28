package core.parsers.params;

import core.commands.Context;
import dao.entities.LastFMData;

public class TwoUsersParamaters extends CommandParameters {
    private final LastFMData firstUser;
    private final LastFMData secondUser;

    public TwoUsersParamaters(Context e, LastFMData firstUser, LastFMData secondUser) {
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
