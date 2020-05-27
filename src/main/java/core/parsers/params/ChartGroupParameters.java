package core.parsers.params;

import dao.entities.ChartMode;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ChartGroupParameters extends ChartParameters {
    private final boolean showTime;

    public ChartGroupParameters(MessageReceivedEvent e, String lastfmID, long discordId, TimeFrameEnum timeFrameEnum, int x, int y, boolean showTime, ChartMode chartMode) {
        super(e, lastfmID, discordId, chartMode, timeFrameEnum, x, y);
        this.showTime = showTime;
    }

    public ChartGroupParameters(MessageReceivedEvent e, String lastfmID, long discordId, TimeFrameEnum timeFrameEnum, int x, int y, boolean writeTitles, boolean writePlays, boolean isList, boolean pieFormat, boolean showTime, ChartMode chartMode) {
        super(e, lastfmID, discordId, timeFrameEnum, x, y, writeTitles, writePlays, isList, chartMode);
        this.showTime = showTime;
    }


    public static ChartGroupParameters toListParams() {
        return new ChartGroupParameters(null, null, -1, null, 0, 0, true, true, true, false, true, ChartMode.LIST);
    }

    public boolean isShowTime() {
        return showTime;
    }

}
