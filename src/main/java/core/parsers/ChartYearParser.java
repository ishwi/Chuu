package core.parsers;

import core.exceptions.InstanceNotFoundException;
import core.parsers.params.ChartYearParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Year;
import java.util.List;

public class ChartYearParser extends ChartableParser<ChartYearParameters> {
    private final int searchSpace;

    public ChartYearParser(ChuuService dao, int searchSpace) {
        super(dao, TimeFrameEnum.WEEK);
        this.searchSpace = searchSpace;
    }

    @Override
    protected void setUpOptionals() {
        super.setUpOptionals();
    }

    @Override
    public ChartYearParameters parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {
        TimeFrameEnum timeFrame = defaultTFE;
        LastFMData discordName;

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
        ChartYearParameters chartYearParameters = new ChartYearParameters(e, discordName.getName(), discordName.getDiscordId(), timeFrame, x, x, year, doAdditionalEmbed(discordName,e));
        chartYearParameters.initParams(List.of("--nolimit"));
        return chartYearParameters;

    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *[d,w,m,q,s,y,a]* *Username* *YEAR*** \n" +
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
