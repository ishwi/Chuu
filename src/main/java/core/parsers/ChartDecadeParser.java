package core.parsers;

import core.exceptions.InstanceNotFoundException;
import core.parsers.exceptions.InvalidChartValuesException;
import core.parsers.params.ChartYearRangeParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.time.Year;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChartDecadeParser extends ChartableParser<ChartYearRangeParameters> {
    private final int searchSpace;

    public ChartDecadeParser(ChuuService dao, int searchSpace) {
        super(dao, TimeFrameEnum.ALL);
        this.searchSpace = searchSpace;
    }

    @Override
    protected void setUpOptionals() {
        super.setUpOptionals();
        opts.add(new OptionalEntity("--nolimit", "make the chart as big as possible"));
        opts.add(new OptionalEntity("--time", "make the chart to be sorted by duration (quite inaccurate)"));
    }

    @Override
    public ChartYearRangeParameters parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {
        TimeFrameEnum timeFrame = defaultTFE;
        LastFMData discordName;
        Year baseYear = Year.now().minus(Year.now().getValue() % 10, ChronoUnit.YEARS);
        int numberOfYears = 9;
        Pattern compile = Pattern.compile("(?:.*)(\\s*(\\d{4})\\s*-\\s*(\\d{4})\\s*)(?:.*)");
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
            boolean conflictFlag = e.getMessage().getContentRaw().contains("--nolimit");
            if (conflictFlag) {
                sendError(getErrorMessage(7), e);
                return null;
            }
            x = chartSize.x;
            y = chartSize.y;
        }

        timeFrame = chartParserAux.parseTimeframe(timeFrame);
        subMessage = chartParserAux.getMessage();
        discordName =

                atTheEndOneUser(e, subMessage);


        return new

                ChartYearRangeParameters(e, baseYear, discordName.getName(), discordName.

                getDiscordId(), timeFrame, x, y, numberOfYears, doAdditionalEmbed(discordName,e));
    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *[d,w,m,q,s,y,a]* *Username* *Decade_Range*** \n" +
                "\tIf time is not specified defaults to " + defaultTFE.toString() + "\n" +
                "\tIf username is not specified defaults to authors account \n" +
                "\tDecade Range can be either two years separated by a - (E.g.  2009 - 2013) or a two digit representative of a decade " +
                "(E.g. 20, 20s, 20's, 80s...)\n\t Default to the current decade if left empty";
    }

    @Override
    public void setUpErrorMessages() {
        super.setUpErrorMessages();
    }
}
