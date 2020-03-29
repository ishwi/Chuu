package core.parsers.params;

import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ChartGroupParameters extends ChartParameters {
    private final boolean showTime;

    public ChartGroupParameters(String[] returned, MessageReceivedEvent e) {
        super(returned, e);
        showTime = Boolean.parseBoolean(returned[8]);


    }

    public ChartGroupParameters(String username, long discordId, TimeFrameEnum timeFrameEnum, int x, int y, MessageReceivedEvent e, boolean writeTitles, boolean writePlays, boolean isList, boolean showTime) {
        super(username, discordId, timeFrameEnum, x, y, e, writeTitles, writePlays, isList);
        this.showTime = showTime;
    }

    public static ChartGroupParameters toListParams() {
        return new ChartGroupParameters(null, 0, null, 0, 0, null, true, true, true, true);
    }

    public boolean isShowTime() {
        return showTime;
    }

}
