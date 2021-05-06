package core.parsers;

import core.commands.Context;
import core.commands.ContextSlashReceived;
import core.commands.utils.CommandUtil;
import core.exceptions.LastFmException;
import core.parsers.exceptions.InvalidChartValuesException;
import core.parsers.explanation.DecadeExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.interactions.InteractionAux;
import core.parsers.params.ChartYearRangeParameters;
import core.parsers.utils.CustomTimeFrame;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.*;
import java.time.Year;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ChartDecadeParser extends ChartableParser<ChartYearRangeParameters> {
    private final int searchSpace;

    public ChartDecadeParser(ChuuService dao, int searchSpace) {
        super(dao, TimeFrameEnum.ALL);
        this.searchSpace = searchSpace;
    }

    @Override
    protected void setUpOptionals() {
        super.setUpOptionals();
        opts.add(new OptionalEntity("nolimit", "make the chart as big as possible (40x40 is the hard limit)"));
        opts.add(new OptionalEntity("time", "make the chart to be sorted by duration (quite inaccurate)"));
    }

    @Override
    public ChartYearRangeParameters parseSlashLogic(ContextSlashReceived ctx) throws LastFmException, InstanceNotFoundException {
        SlashCommandEvent e = ctx.e();
        TimeFrameEnum tfe = InteractionAux.parseTimeFrame(e, defaultTFE);
        User user = InteractionAux.parseUser(e);
        OptionMapping option = e.getOption(DecadeExplanation.NAME);
        Point size = InteractionAux.parseSize(e, () -> sendError(getErrorMessage(8), ctx));
        if (size == null) {
            return null;
        }

        int baseYear;
        int numberOfYears;
        if (option != null) {
            String value = option.getAsString();
            value = value.substring(0, value.length() - 1);
            baseYear = CommandUtil.getDecade(Integer.parseInt(value));
            numberOfYears = 9;
        } else {
            OptionMapping start = e.getOption(DecadeExplanation.RANGE_START);

            baseYear = Optional.ofNullable(e.getOption(DecadeExplanation.RANGE_START))
                    .map(OptionMapping::getAsLong)
                    .map(Math::toIntExact)
                    .map(CommandUtil::getDecade)
                    .orElse(Year.now().getValue());
            OptionMapping end = e.getOption(DecadeExplanation.RANGE_END);


            if (end != null) {
                long endYear = e.getOption(DecadeExplanation.RANGE_END).getAsLong();
                if (baseYear > endYear) {
                    sendError("First year must be greater than the second", ctx);
                    return null;
                }
                numberOfYears = (int) (endYear - baseYear);
            } else {
                numberOfYears = 9;
            }
        }
        return new ChartYearRangeParameters(ctx, findLastfmFromID(user, ctx), CustomTimeFrame.ofTimeFrameEnum(tfe), size.x, size.y, Year.of(baseYear), numberOfYears);

    }

    @Override
    public ChartYearRangeParameters parseLogic(Context e, String[] subMessage) throws InstanceNotFoundException {
        TimeFrameEnum timeFrame = defaultTFE;
        LastFMData discordName;
        Year baseYear = Year.now().minus(Year.now().getValue() % 10, ChronoUnit.YEARS);
        int numberOfYears = 9;
        Pattern compile = Pattern.compile(".*(\\s*(\\d{4})\\s*-\\s*(\\d{4})\\s*).*");
        boolean matched = false;

        String join = String.join(" ", subMessage);
        Matcher matcher = compile.matcher(join);
        if (matcher.matches()) {
            String firstYear = matcher.group(2);
            String secondYear = matcher.group(3);
            Year firstYear1 = Year.parse(firstYear);
            Year secondYear2 = Year.parse(secondYear);


            if (firstYear1.compareTo(secondYear2) > 0) {
                sendError("First year must be greater than second", e);
                return null;
            }
            baseYear = firstYear1;
            int i = firstYear1.get(ChronoField.YEAR);
            numberOfYears = secondYear2.minusYears(i).getValue();
            matched = true;
            String replace = join.replace(matcher.group(1), "");
            String[] parts = replace.split("\\s+");
            subMessage = Arrays.copyOfRange(parts, 1, parts.length);
        }


        ChartParserAux chartParserAux = new ChartParserAux(subMessage);
        if (!matched) {
            baseYear = Year.of(chartParserAux.parseDecade());
        }


        int x = 5;
        int y = 5;

        Point chartSize;
        try {
            chartSize = chartParserAux.getChartSize();
        } catch (
                InvalidChartValuesException ex) {
            sendError(getErrorMessage(8), e);
            return null;
        }

        if (chartSize != null) {

            boolean conflictFlag = hasOptional("nolimit", e);
            if (conflictFlag) {
                sendError(getErrorMessage(7), e);
                return null;
            }
            x = chartSize.x;
            y = chartSize.y;
        }

        timeFrame = chartParserAux.parseTimeframe(timeFrame);
        subMessage = chartParserAux.getMessage();
        discordName = atTheEndOneUser(e, subMessage);


        return new ChartYearRangeParameters(e, discordName, CustomTimeFrame.ofTimeFrameEnum(timeFrame), x, y, baseYear, numberOfYears);
    }

    @Override
    public List<Explanation> getUsages() {
        return Stream.concat(Stream.of(new DecadeExplanation()), super.getUsages().stream()).toList();
    }

    @Override
    public void setUpErrorMessages() {
        super.setUpErrorMessages();
    }

}
