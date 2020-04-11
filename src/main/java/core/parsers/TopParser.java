package core.parsers;

import core.exceptions.InstanceNotFoundException;
import core.parsers.exceptions.InvalidChartValuesException;
import core.parsers.params.TopParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

public class TopParser extends ChartableParser<TopParameters> {
    public TopParser(ChuuService dao) {
        super(dao, TimeFrameEnum.ALL);
    }


    @Override
    public TopParameters parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {
        int x = 5;
        int y = 5;

        if (subMessage.length > 3) {
            sendError(getErrorMessage(5), e);
            return null;
        }


        ChartParserAux chartParserAux = new ChartParserAux(subMessage);
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
        subMessage = chartParserAux.getMessage();

        LastFMData data = getLastFmUsername1input(e.getAuthor().getIdLong(), e);
        return new TopParameters(e, data.getName(), data.getDiscordId(), defaultTFE, x, y);
    }


    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *sizeXsize*  *Username* ** \n" +
               "\tIf username is not specified defaults to authors account \n" +
               "\tIf Size not specified it defaults to 5x5\n";

    }
}
