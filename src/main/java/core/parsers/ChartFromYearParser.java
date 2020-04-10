package core.parsers;

import core.exceptions.InstanceNotFoundException;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Year;

public class ChartFromYearParser extends ChartParser {
    private static final TimeFrameEnum defaultTFE = TimeFrameEnum.WEEK;

    public ChartFromYearParser(ChuuService dao) {
        super(dao);
    }

    @Override
    protected void setUpOptionals() {
        super.setUpOptionals();
    }

    @Override
    public String[] parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {
        TimeFrameEnum timeFrame = defaultTFE;
        LastFMData discordName;

        if (subMessage.length > 2) {
            sendError(getErrorMessage(5), e);
            return null;
        }
        ChartParserAux chartParserAux = new ChartParserAux(subMessage);
        String year = chartParserAux.parseYear();
        timeFrame = chartParserAux.parseTimeframe(timeFrame);
        subMessage = chartParserAux.getMessage();

        discordName = getLastFmUsername1input(e.getAuthor().getIdLong(), e);

        if (Year.now().compareTo(Year.of(Integer.parseInt(year))) < 0) {
            sendError(getErrorMessage(6), e);
            return null;
        }

        return new String[]{year, String.valueOf(discordName.getDiscordId()), discordName.getName(), timeFrame.toApiFormat(), Boolean.toString(true)};

    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *[w,m,q,s,y,a]* *Username* *YEAR*** \n" +
               "\tIf time is not specified defaults to " + defaultTFE.toString() + "\n" +
               "\tIf username is not specified defaults to authors account \n" +
               "\tIf YEAR not specified it default to current year\n";
    }

    @Override
    public void setUpErrorMessages() {
        super.setUpErrorMessages();
        errorMessages.put(6, "YEAR must be current year or lower");
    }
}
