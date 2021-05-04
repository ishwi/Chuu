package core.parsers.params;

import core.commands.Context;
import dao.entities.LastFMData;
import dao.entities.NaturalTimeFrameEnum;

public class NaturalTimeParams extends ChuuDataParams {
    private final NaturalTimeFrameEnum time;

    public NaturalTimeParams(Context e, LastFMData lastFMData, NaturalTimeFrameEnum time) {
        super(e, lastFMData);
        this.time = time;
    }

    public NaturalTimeFrameEnum getTime() {
        return time;
    }
}
