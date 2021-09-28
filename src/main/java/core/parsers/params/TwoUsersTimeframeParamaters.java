package core.parsers.params;

import core.commands.Context;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;

public class TwoUsersTimeframeParamaters extends TwoUsersParamaters {
    private final TimeFrameEnum timeFrameEnum;

    public TwoUsersTimeframeParamaters(Context e, LastFMData firstUser, LastFMData secondUser, TimeFrameEnum timeFrameEnum) {
        super(e, firstUser, secondUser);
        this.timeFrameEnum = timeFrameEnum;
    }

    public TimeFrameEnum getTimeFrameEnum() {
        return timeFrameEnum;
    }
}
