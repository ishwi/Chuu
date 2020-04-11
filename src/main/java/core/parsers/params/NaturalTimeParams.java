package core.parsers.params;

import dao.entities.LastFMData;
import dao.entities.NaturalTimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class NaturalTimeParams extends ChuuDataParams {
    private final NaturalTimeFrameEnum time;

    public NaturalTimeParams(MessageReceivedEvent e, LastFMData lastFMData, NaturalTimeFrameEnum time) {
        super(e, lastFMData);
        this.time = time;
    }

    public NaturalTimeFrameEnum getTime() {
        return time;
    }
}
