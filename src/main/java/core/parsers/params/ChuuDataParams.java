package core.parsers.params;

import core.commands.Context;
import dao.entities.LastFMData;

public class ChuuDataParams extends CommandParameters {
    private final LastFMData lastFMData;

    public ChuuDataParams(Context e, LastFMData lastFMData) {
        super(e);
        this.lastFMData = lastFMData;
    }

    public LastFMData getLastFMData() {
        return lastFMData;
    }
}
