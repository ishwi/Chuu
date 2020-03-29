package core.parsers;

import core.exceptions.InstanceNotFoundException;
import core.parsers.exceptions.InvalidChartValuesException;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

public class TopParser extends ChartParser {
    public TopParser(ChuuService dao) {
        super(dao);
    }

    @Override
    protected void setUpOptionals() {
        super.setUpOptionals();
    }

    public String[] parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {
        TimeFrameEnum timeFrame = TimeFrameEnum.ALL;
        String x = "5";
        String y = "5";

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
        subMessage = chartParserAux.getMessage();

        LastFMData data = getLastFmUsername1input(subMessage, e.getAuthor().getIdLong(), e);

        return new String[]{x, y, String.valueOf(data.getDiscordId()), data.getName(), timeFrame.toApiFormat()};
    }


    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *sizeXsize*  *Username* ** \n" +
               "\tIf username is not specified defaults to authors account \n" +
               "\tIf Size not specified it defaults to 5x5\n";

    }
}
