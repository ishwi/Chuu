package core.parsers.params;

import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ChartGroupParameters extends ChartParameters {
    private final boolean showTime;

    public ChartGroupParameters(MessageReceivedEvent e, String lastfmID, long discordId, TimeFrameEnum timeFrameEnum, int x, int y, boolean showTime, boolean doAdditionalEmbed) {
        super(e, lastfmID, discordId, doAdditionalEmbed, timeFrameEnum, x, y);
        this.showTime = showTime;
    }

    public ChartGroupParameters(MessageReceivedEvent e, String lastfmID, long discordId, TimeFrameEnum timeFrameEnum, int x, int y, boolean writeTitles, boolean writePlays, boolean isList, boolean pieFormat, boolean showTime, boolean doAdditionalEmbed) {
        super(e, lastfmID, discordId, timeFrameEnum, x, y, writeTitles, writePlays, isList, pieFormat, doAdditionalEmbed);
        this.showTime = showTime;
    }


    public static ChartGroupParameters toListParams() {
        return new ChartGroupParameters(null, null, -1, null, 0, 0, true, true, true, false, true, false);
    }

    public boolean isShowTime() {
        return showTime;
    }

}
