package core.parsers.params;

import com.neovisionaries.i18n.CountryCode;
import core.parsers.utils.CustomTimeFrame;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CountryParameters extends ChuuDataParams {
    private final CountryCode code;
    private final CustomTimeFrame timeFrame;

    public CountryParameters(MessageReceivedEvent e, LastFMData lastFMData, CountryCode code, CustomTimeFrame timeFrame) {
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
