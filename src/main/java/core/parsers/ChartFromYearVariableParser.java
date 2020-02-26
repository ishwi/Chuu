package core.parsers;

import core.exceptions.InstanceNotFoundException;
import core.parsers.exceptions.InvalidChartValuesException;
import dao.ChuuService;
import dao.entities.LastFMData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.time.Year;

public class ChartFromYearVariableParser extends DaoParser {
    public ChartFromYearVariableParser(ChuuService dao) {
        super(dao);
    }


    @Override
    void setUpOptionals() {
        opts.add(new OptionalEntity("--notitles", "dont display titles"));
        opts.add(new OptionalEntity("--plays", "display play count"));
        opts.add(new OptionalEntity("--nolimit", "makes the chart as big as possible"));
    }

    @Override
    protected String[] parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException {
        LastFMData discordName;

        if (words.length > 3) {
            sendError(getErrorMessage(5), e);
            return null;
        }

        String x = "5";
        String y = "5";


        ChartParserAux chartParserAux = new ChartParserAux(words);
        Point chartSize = null;
        try {
            chartSize = chartParserAux.getChartSize();

        } catch (InvalidChartValuesException ex) {
            sendError(getErrorMessage(8), e);
            ex.printStackTrace();
        }
        if (chartSize != null) {
            boolean conflictFlag = e.getMessage().getContentRaw().contains("--nolimit");
            if (conflictFlag) {
                sendError(getErrorMessage(7), e);
                return null;
            }
            x = String.valueOf(chartSize.x);
            y = String.valueOf(chartSize.y);
        }
        String year = chartParserAux.parseYear();
        words = chartParserAux.getMessage();

        discordName = getLastFmUsername1input(words, e.getAuthor().getIdLong(), e);
        if (Year.now().compareTo(Year.of(Integer.parseInt(year))) < 0) {
            sendError(getErrorMessage(6), e);
            return null;
        }
        return new String[]{x, y, year, discordName.getName()};
    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *Username* *YEAR* *sizeXsize*** \n" +
               "\tIf username is not specified defaults to authors account \n" +
               "\tIf YEAR not specified it default to current year\n" +
               "\tIf Size not specified it defaults to 5x5\n";
    }

    @Override
    protected void setUpErrorMessages() {
        errorMessages.put(5, "You Introduced too many words");
        errorMessages.put(6, "YEAR must be current year or lower");
        errorMessages.put(7, "Cant use a size for the chart if you specify the --nolimit flag!");
        errorMessages.put(8, "0 is not a valid value for a chart!");

    }
}
