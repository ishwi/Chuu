package core.parsers;

import core.commands.Context;
import core.commands.InteracionReceived;
import core.parsers.exceptions.InvalidChartValuesException;
import core.parsers.explanation.TimeframeExplanation;
import core.parsers.explanation.YearExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.ChartYearParameters;
import core.parsers.utils.CustomTimeFrame;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;

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
    public ChartYearParameters parseSlashLogic(InteracionReceived<? extends CommandInteraction> ctx) throws InstanceNotFoundException {
        CommandInteraction e = ctx.e();
        User user = InteractionAux.parseUser(e);
        Year year = InteractionAux.parseYear(e, () -> sendError(getErrorMessage(6), ctx));
        Point point = InteractionAux.parseSize(e, () -> sendError(getErrorMessage(6), ctx));

        if (year == null || point == null) {
            return null;
        }
        return new ChartYearParameters(ctx, findLastfmFromID(user, ctx), CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL), point.x, point.y, year);

    }

    @Override
    public ChartYearParameters parseLogic(Context e, String[] subMessage) throws InstanceNotFoundException {
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
        if (Year.now().compareTo(year) < 0) {
            sendError(getErrorMessage(6), e);
            return null;
        }
        return new ChartYearParameters(e, data, CustomTimeFrame.ofTimeFrameEnum(TimeFrameEnum.ALL), x, y, year);

    }

    @Override
    public List<Explanation> getUsages() {
        return Stream.concat(Stream.of(new YearExplanation()), super.getUsages().stream().filter(t -> !(t instanceof TimeframeExplanation))).toList();
    }

    @Override
    public void setUpErrorMessages() {
        super.setUpErrorMessages();
        errorMessages.put(6, "YEAR must be current year or lower");
    }
}
