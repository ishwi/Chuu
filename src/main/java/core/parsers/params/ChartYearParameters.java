package core.parsers.params;

import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.LocalDateTime;
import java.time.Year;

public class ChartYearParameters extends ChartParameters {
    private final Year year;
    private final boolean careAboutSized;

    public ChartYearParameters(String[] message, MessageReceivedEvent e) {
        super(message, (message[3]), Long.parseLong(message[4]), calculateTimeFrame(Year.of(Integer.parseInt(message[2]))), Integer.parseInt(message[0]), Integer.parseInt(message[1]), e);
        handleOptParameters(message,
                new OptionalParameter("--nolimit", 9));

        this.year = Year.of(Integer.parseInt(message[2]));
        this.careAboutSized = !hasOptional("--nolimit");


    }

    public ChartYearParameters(String[] message, String username, long discordId, TimeFrameEnum timeFrameEnum, int x, int y, MessageReceivedEvent e, Year year) {
        super(message, username, discordId, timeFrameEnum, x, y, e);
        this.year = year;
        this.careAboutSized = !hasOptional("--nolimit");
    }

    private static TimeFrameEnum calculateTimeFrame(Year year) {
        TimeFrameEnum timeframe;
        LocalDateTime time = LocalDateTime.now();
        if (year.isBefore(Year.of(time.getYear()))) {
            timeframe = TimeFrameEnum.ALL;
        } else {
            int monthValue = time.getMonthValue();
            if (monthValue == 1 && time.getDayOfMonth() < 8) {
                timeframe = TimeFrameEnum.WEEK;
            } else if (monthValue < 2) {
                timeframe = TimeFrameEnum.MONTH;
            } else if (monthValue < 4) {
                timeframe = TimeFrameEnum.QUARTER;
            } else if (monthValue < 7)
                timeframe = TimeFrameEnum.SEMESTER;
            else {
                timeframe = TimeFrameEnum.YEAR;
            }
        }
        return timeframe;
    }

    public Year getYear() {
        return year;
    }

    public boolean isCareAboutSized() {
        return careAboutSized;
    }

}
