package core.commands;

import core.parsers.ChartFromYearVariableParser;
import core.parsers.params.ChartParameters;
import core.parsers.params.ChartYearParameters;
import dao.ChuuService;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.Arrays;
import java.util.List;

public class MbizThisYearCommand extends MusicBrainzCommand {


    public MbizThisYearCommand(ChuuService dao) {
        super(dao);
        this.parser = new ChartFromYearVariableParser(dao);
        this.searchSpace = 1500;
    }

    @Override
    public boolean doDiscogs() {
        return false;
    }


    @Override
    public String getDescription() {
        return "Gets your top albums of the year queried.\t" +
               "NOTE: The further the year is from the  current year, the less precise the command will be";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("aoty", "albumoftheyear");
    }

    @Override
    public String getName() {
        return "Albums Of The Year!";
    }

    @Override
    public ChartParameters getParameters(String[] message, MessageReceivedEvent e) {
        int x = Integer.parseInt(message[0]);
        int y = Integer.parseInt(message[1]);
        Year year = Year.of(Integer.parseInt(message[2]));
        String username = message[3];
        long discordId = Long.parseLong(message[4]);
        boolean titleWrite = !Boolean.parseBoolean(message[5]);
        boolean playsWrite = Boolean.parseBoolean(message[6]);
        boolean isList = Boolean.parseBoolean(message[7]);
        boolean isPie = Boolean.parseBoolean(message[8]);
        boolean noLimitFlag = Boolean.parseBoolean(message[9]);
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
        return new ChartYearParameters(username, discordId, timeframe, x, y, e, titleWrite, playsWrite, isList, isPie, year, !noLimitFlag);
    }


}
