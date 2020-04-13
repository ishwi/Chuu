package core.parsers.params;

import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Year;

public class ChartYearParameters extends ChartParameters {
    private final Year year;

    public ChartYearParameters(MessageReceivedEvent e, String username, long discordId, TimeFrameEnum timeFrameEnum, int x, int y, Year year) {
        super(e, username, discordId, timeFrameEnum, x, y);
        this.year = year;
    }


    public Year getYear() {
        return year;
    }

    public boolean isByTime() {
        return hasOptional("--time");
    }

    public boolean isCareAboutSized() {
        return !hasOptional("--nolimit");
    }

}
