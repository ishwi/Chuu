package core.parsers.params;

import core.parsers.utils.CustomTimeFrame;
import dao.entities.ChartMode;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ChartGroupParameters extends ChartParameters {
    private final boolean showTime;

    public ChartGroupParameters(MessageReceivedEvent e, String lastfmID, long discordId, CustomTimeFrame timeFrameEnum, int x, int y, boolean showTime, ChartMode chartMode, LastFMData lastFMData) {
        super(e, lastfmID, discordId, chartMode, lastFMData, timeFrameEnum, x, y);
        this.showTime = showTime;
    }

    public ChartGroupParameters(MessageReceivedEvent e, String lastfmID, long discordId, CustomTimeFrame timeFrameEnum, int x, int y, boolean writeTitles, boolean writePlays, boolean isList, boolean pieFormat, boolean showTime, ChartMode chartMode, LastFMData lastFMData) {
        super(e, lastfmID, discordId, timeFrameEnum, x, y, writeTitles, writePlays, isList, chartMode, lastFMData);
        this.showTime = showTime;
    }


    public static ChartGroupParameters toListParams() {
        return new ChartGroupParameters(null, null, -1, null, 0, 0, true, true, true, false, true, ChartMode.LIST, null);
    }

    public boolean isShowTime() {
        return showTime;
    }

}
