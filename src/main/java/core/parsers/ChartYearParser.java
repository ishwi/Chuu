package core.parsers;

import core.parsers.exceptions.InvalidChartValuesException;
import core.parsers.exceptions.InvalidDateException;
import core.parsers.explanation.YearExplanation;
import core.parsers.explanation.util.Explanation;
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
import java.util.List;
import java.util.stream.Stream;

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
    public List<Explanation> getUsages() {
        return Stream.concat(Stream.of(new YearExplanation()), super.getUsages().stream()).toList();
    }

    @Override
    public void setUpErrorMessages() {
        super.setUpErrorMessages();
        errorMessages.put(6, "YEAR must be current year or lower");
    }
}
