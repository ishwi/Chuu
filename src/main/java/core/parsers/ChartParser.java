package core.parsers;

import core.exceptions.InstanceNotFoundException;
import core.parsers.exceptions.InvalidChartValuesException;
import core.parsers.params.ChartParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

public class ChartParser extends ChartableParser<ChartParameters> {

    public ChartParser(ChuuService dao, TimeFrameEnum defaultTFE) {
        super(dao, defaultTFE);
    }

    public ChartParser(ChuuService dao) {
        super(dao, TimeFrameEnum.WEEK);
    }

    @Override
    public ChartParameters parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {
        TimeFrameEnum timeFrame = this.defaultTFE;
        int x = 5;
        int y = 5;


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
        timeFrame = chartParserAux.parseTimeframe(timeFrame);
        subMessage = chartParserAux.getMessage();
        LastFMData data = atTheEndOneUser(e, subMessage);
        return new ChartParameters(e, data.getName(), data.getDiscordId(), data.getChartMode(), data, timeFrame, x, y);
    }
}
