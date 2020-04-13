package core.parsers;

import core.exceptions.InstanceNotFoundException;
import core.parsers.params.ChartYearParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Year;

public class ChartSmartYearParser extends ChartableParser<ChartYearParameters> {
    private final int searchSpace;

    public ChartSmartYearParser(ChuuService dao, int searchSpace) {
        super(dao, TimeFrameEnum.WEEK);
        this.searchSpace = searchSpace;
    }

    @Override
    protected void setUpOptionals() {
        super.setUpOptionals();
        opts.add(new OptionalEntity("--nolimit", "make the chart as big as possible"));
    }

    @Override
    public ChartYearParameters parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {
        TimeFrameEnum timeFrame = defaultTFE;
        LastFMData discordName;

        if (subMessage.length > 2) {
            sendError(getErrorMessage(5), e);
            return null;
        }
        ChartParserAux chartParserAux = new ChartParserAux(subMessage);
        Year year = chartParserAux.parseYear();
        timeFrame = chartParserAux.parseTimeframe(timeFrame);
        subMessage = chartParserAux.getMessage();
        discordName = atTheEndOneUser(e, subMessage);
        if (Year.now().compareTo(year) < 0) {
            sendError(getErrorMessage(6), e);
            return null;
        }
        int x = (int) Math.sqrt(searchSpace);
        return new ChartYearParameters(e, discordName.getName(), discordName.getDiscordId(), timeFrame, x, x, year);

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
