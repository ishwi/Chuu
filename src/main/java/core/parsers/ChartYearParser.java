package core.parsers;

import core.parsers.exceptions.InvalidChartValuesException;
import core.parsers.exceptions.InvalidDateException;
import core.parsers.params.ChartYearParameters;
import core.parsers.utils.CustomTimeFrame;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.time.Year;

public class ChartYearParser extends ChartableParser<ChartYearParameters> {
    private final int searchSpace;

    public ChartYearParser(ChuuService dao, TimeFrameEnum defaultTFE) {
        super(dao, defaultTFE);
        this.searchSpace = 1;
    }

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
        ParserAux parserAux = new ParserAux(subMessage);
        User oneUser = parserAux.getOneUser(e, dao);
        subMessage = parserAux.getMessage();
        LastFMData data = findLastfmFromID(oneUser, e);

        ChartParserAux chartParserAux = new ChartParserAux(subMessage);
        Year year = chartParserAux.parseYear();
        int x = 5;
        int y = 5;
        try {
            Point chartSize = chartParserAux.getChartSize();
            if (chartSize != null) {
                x = chartSize.x;
                y = chartSize.y;
            }
        } catch (InvalidChartValuesException ex) {
            this.sendError(getErrorMessage(6), e);
            return null;
        }
        CustomTimeFrame customTimeFrame;
        try {
            customTimeFrame = chartParserAux.parseCustomTimeFrame(defaultTFE);
        } catch (InvalidDateException invalidDateException) {
            this.sendError(invalidDateException.getErrorMessage(), e);
            return null;
        }
        if (Year.now().compareTo(year) < 0) {
            sendError(getErrorMessage(6), e);
            return null;
        }
        return new ChartYearParameters(e, data, data.getDiscordId(), customTimeFrame, x, y, year, data.getChartMode(), data);

    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *[d,w,m,q,s,y,a]* *sizeXsize* *Username* *YEAR*** \n" +
                "\tIf time is not specified defaults to " + defaultTFE.toString() + "\n" +
                "\tIf username is not specified defaults to authors account \n" +
                "\tIf Size not specified it defaults to 5x5\n" +
                "\tIf YEAR not specified it default to current year\n";
    }

    @Override
    public void setUpErrorMessages() {
        super.setUpErrorMessages();
        errorMessages.put(6, "YEAR must be current year or lower");
    }
}
