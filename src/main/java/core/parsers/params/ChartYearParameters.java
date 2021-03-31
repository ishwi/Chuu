package core.parsers.params;

import core.parsers.utils.CustomTimeFrame;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Year;

public class ChartYearParameters extends ChartParameters {
    private final Year year;

    public ChartYearParameters(MessageReceivedEvent e, LastFMData lastFMData, CustomTimeFrame timeFrameEnum, int x, int y, Year year) {
        super(e, lastFMData, timeFrameEnum, x, y);
        this.year = year;
    }

    public ChartYearParameters(MessageReceivedEvent e, LastFMData user, CustomTimeFrame timeFrameEnum, int x, int y, boolean writeTitles, boolean writePlays, boolean isList, Year year) {
        super(e, user, timeFrameEnum, x, y, writeTitles, writePlays, isList);
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
