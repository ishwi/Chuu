package core.parsers;

import core.parsers.exceptions.InvalidChartValuesException;
import core.parsers.exceptions.InvalidDateException;
import core.parsers.params.CustomRangeChartParameters;
import core.parsers.utils.CustomTimeFrame;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

public class RangeChartParser extends ChartableParser<CustomRangeChartParameters> {
    public RangeChartParser(ChuuService dao, TimeFrameEnum defaultTFE) {
        super(dao, defaultTFE);
    }

    @Override
    public CustomRangeChartParameters parseLogic(MessageReceivedEvent e, String[] subMessage) throws InstanceNotFoundException {
        int x = 5;
        int y = 5;

        ParserAux parserAux = new ParserAux(subMessage);
        User oneUser = parserAux.getOneUser(e, dao);
        subMessage = parserAux.getMessage();
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
        CustomTimeFrame customTimeFrame;
        try {
            customTimeFrame = chartParserAux.parseCustomTimeFrame(defaultTFE);
        } catch (InvalidDateException invalidDateException) {
            this.sendError(invalidDateException.getErrorMessage(), e);
            return null;
        }
        LastFMData data = findLastfmFromID(oneUser, e);
//        return new CustomRangeChartParameters(e, data, data.getChartMode(), customTimeFrame, x, y, customTimeFrame);
        // TODO
        return null;
    }
}
