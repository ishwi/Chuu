package core.parsers.params;

import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Year;

public class ChartYearParameters extends ChartParameters {
    private final Year year;
    private final boolean careAboutSized;


    public ChartYearParameters(String username, long discordId, TimeFrameEnum timeFrameEnum, int x, int y, MessageReceivedEvent e, boolean writeTitles, boolean writePlays, boolean isList, Year year, boolean careAboutSized) {
        super(username, discordId, timeFrameEnum, x, y, e, writeTitles, writePlays, isList);
        this.year = year;
        this.careAboutSized = careAboutSized;
    }

    public Year getYear() {
        return year;
    }

    public boolean isCareAboutSized() {
        return careAboutSized;
    }
}
