package core.parsers.params;

import core.commands.Context;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;

public class TimeFrameParameters extends ChuuDataParams {
    private final TimeFrameEnum time;

    public TimeFrameParameters(Context e, LastFMData lastFMData, TimeFrameEnum time) {
        super(e, lastFMData);
        this.time = time;
    }

    public TimeFrameEnum getTime() {

        return time;
    }
}
