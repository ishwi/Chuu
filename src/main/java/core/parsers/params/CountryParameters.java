package core.parsers.params;

import com.neovisionaries.i18n.CountryCode;
import core.commands.Context;
import core.parsers.utils.CustomTimeFrame;
import dao.entities.LastFMData;

public class CountryParameters extends ChuuDataParams {
    private final CountryCode code;
    private final CustomTimeFrame timeFrame;

    public CountryParameters(Context e, LastFMData lastFMData, CountryCode code, CustomTimeFrame timeFrame) {
        super(e, lastFMData);
        this.code = code;
        this.timeFrame = timeFrame;
    }

    public CountryCode getCode() {
        return code;
    }

    public CustomTimeFrame getTimeFrame() {
        return timeFrame;
    }
}
