package core.parsers.params;

import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ChartGroupParameters extends ChartParameters {
    private final boolean showTime;


    public ChartGroupParameters(String[] message, MessageReceivedEvent e) {
        super(message, e, new OptionalParameter("--notime", 9));
        this.showTime = !hasOptional("--notime");
    }


    public ChartGroupParameters(String[] message, MessageReceivedEvent e, String username, long discordId, TimeFrameEnum timeFrameEnum, int x, int y, boolean writeTitles, boolean writePlays, boolean isList, boolean pieFormat, boolean showTime, OptionalParameter... opts) {
        super(message, e, username, discordId, timeFrameEnum, x, y, writeTitles, writePlays, isList, pieFormat, opts);
        this.showTime = showTime;
    }

    public static ChartGroupParameters toListParams() {
        return new ChartGroupParameters(null, null, null, -1, null, 0, 0, true, true, true, false, true);
    }

    public boolean isShowTime() {
        return showTime;
    }

}
