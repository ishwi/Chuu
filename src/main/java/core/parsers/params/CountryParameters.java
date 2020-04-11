package core.parsers.params;

import com.neovisionaries.i18n.CountryCode;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CountryParameters extends ChuuDataParams {
    private final CountryCode code;
    private final TimeFrameEnum timeFrame;

    public CountryParameters(MessageReceivedEvent e, LastFMData lastFMData, CountryCode code, TimeFrameEnum timeFrame) {
        super(e, lastFMData);
        this.code = code;
        this.timeFrame = timeFrame;
    }

    public CountryCode getCode() {
        return code;
    }

    public TimeFrameEnum getTimeFrame() {
        return timeFrame;
    }
}
