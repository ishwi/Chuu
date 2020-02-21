package core.parsers;

import core.exceptions.InstanceNotFoundException;
import core.parsers.exceptions.InvalidChartValuesException;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

public class ChartParser extends DaoParser {
    private final TimeFrameEnum defaultTFE = TimeFrameEnum.WEEK;

    public ChartParser(ChuuService dao) {
        super(dao);
    }

    @Override
    protected void setUpOptionals() {
        opts.add(new OptionalEntity("--artist", "use artist instead of albums"));
        opts.add(new OptionalEntity("--notitles", "dont display titles"));
        opts.add(new OptionalEntity("--plays", "display play count"));
    }

    @Override
    public String[] parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {
        TimeFrameEnum timeFrame = defaultTFE;
        String x = "5";
        String y = "5";

        String pattern = "\\d+[xX]\\d+";

        if (subMessage.length > 3) {
            sendError(getErrorMessage(5), e);
            return null;
        }


        ChartParserAux chartParserAux = new ChartParserAux(subMessage);
        try {
            Point chartSize = chartParserAux.getChartSize();
            if (chartSize != null) {
                x = String.valueOf(chartSize.x);
                y = String.valueOf(chartSize.y);
            }
        } catch (InvalidChartValuesException ex) {
            this.sendError(getErrorMessage(6), e);
            return null;
        }
        timeFrame = chartParserAux.parseTimeframe(timeFrame);
        subMessage = chartParserAux.getMessage();

        LastFMData data = getLastFmUsername1input(subMessage, e.getAuthor().getIdLong(), e);

        return new String[]{x, y, data.getName(), timeFrame.toApiFormat()};
    }


    public String getErrorMessage(int code) {
        return errorMessages.get(code);
    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *[w,m,q,s,y,a]* *sizeXsize*  *Username* ** \n" +
                "\tIf time is not specified defaults to Yearly \n" +
                "\tIf username is not specified defaults to authors account \n" +
                "\tIf Size not specified it defaults to 5x5\n";
    }

    @Override
    protected void setUpErrorMessages() {
        super.setUpErrorMessages();
        errorMessages.put(5, "You Introduced too many words");
        errorMessages.put(6, "0 is not a valid value for a chart!");

    }

}
