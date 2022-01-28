package core.parsers;

import core.commands.Context;
import core.commands.InteracionReceived;
import core.exceptions.LastFmException;
import core.parsers.exceptions.InvalidChartValuesException;
import core.parsers.explanation.FullTimeframeExplanation;
import core.parsers.explanation.YearExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.ChartYearParameters;
import core.parsers.utils.CustomTimeFrame;
import core.parsers.utils.OptionalEntity;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ChartSmartYearParser extends ChartableParser<ChartYearParameters> {
    public ChartSmartYearParser(ChuuService dao) {
        super(dao, TimeFrameEnum.WEEK);
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


    @Override
    void setUpOptionals() {
        super.setUpOptionals();
        addOptional(new OptionalEntity("nolimit", "make the chart as big as possible"), new OptionalEntity("time", "to sort by duration"));
    }

    @Override
    public ChartYearParameters parseSlashLogic(InteracionReceived<? extends CommandInteraction> ctx) throws LastFmException, InstanceNotFoundException {
        CommandInteraction e = ctx.e();
        Point point = InteractionAux.parseSize(e, () -> sendError(getErrorMessage(8), ctx));
        if (point == null) {
            return null;
        }
        Year year = Optional.ofNullable(e.getOption(YearExplanation.NAME)).map(OptionMapping::getAsLong).map(Long::intValue).map(Year::of).orElse(Year.now());
        if (Year.now().compareTo(year) < 0) {
            sendError(getErrorMessage(6), ctx);
            return null;
        }
        TimeFrameEnum timeFrameEnum = calculateTimeFrame(year);
        User user = InteractionAux.parseUser(e);
        LastFMData data = findLastfmFromID(user, ctx);
        return new ChartYearParameters(ctx, data, CustomTimeFrame.ofTimeFrameEnum(timeFrameEnum), point.x, point.y, year);
    }

    @Override
    public ChartYearParameters parseLogic(Context e, String[] words) throws InstanceNotFoundException {
        LastFMData discordName;


        int x = 5;
        int y = 5;

        ChartParserAux chartParserAux = new ChartParserAux(words);
        Point chartSize;
        try {
            chartSize = chartParserAux.getChartSize();
        } catch (InvalidChartValuesException ex) {
            sendError(getErrorMessage(6), e);
            return null;
        }

        if (chartSize != null) {
            x = chartSize.x;
            y = chartSize.y;
        }
        Year year = chartParserAux.parseYear();
        words = chartParserAux.getMessage();
        if (Year.now().compareTo(year) < 0) {
            sendError(getErrorMessage(9), e);
            return null;
        }
        TimeFrameEnum timeFrameEnum = calculateTimeFrame(year);
        discordName = atTheEndOneUser(e, words);
        return new ChartYearParameters(e, discordName, CustomTimeFrame.ofTimeFrameEnum(timeFrameEnum), x, y, year);
    }

    @Override
    public List<Explanation> getUsages() {
        return Stream.concat(Stream.of(new YearExplanation()), super.getUsages().stream()).filter(t -> !(t instanceof FullTimeframeExplanation)).toList();
    }

    @Override
    protected void setUpErrorMessages() {
        errorMessages.put(9, "YEAR must be the current year or lower");

    }
}
