package core.parsers.params;

import core.parsers.utils.CustomTimeFrame;
import dao.entities.ChartMode;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Year;

public class ChartYearParameters extends ChartParameters {
    private final Year year;

    public ChartYearParameters(MessageReceivedEvent e, String username, long discordId, CustomTimeFrame timeFrameEnum, int x, int y, Year year, ChartMode chartMode, LastFMData lastFMData) {
        super(e, username, discordId, chartMode, lastFMData, timeFrameEnum, x, y);
        this.year = year;
    }


    public Year getYear() {
        return year;
    }

    public boolean isByTime() {
        return hasOptional("time");
    }

    public boolean isCareAboutSized() {
        return !hasOptional("nolimit");
    }

}
