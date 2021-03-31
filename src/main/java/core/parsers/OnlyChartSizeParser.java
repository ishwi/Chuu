package core.parsers;

import core.parsers.exceptions.InvalidChartValuesException;
import core.parsers.explanation.ChartSizeExplanation;
import core.parsers.explanation.PermissiveUserExplanation;
import core.parsers.explanation.util.Explanation;
import core.parsers.params.ChartSizeParameters;
import dao.ChuuService;
import dao.entities.LastFMData;
import dao.entities.TimeFrameEnum;
import dao.exceptions.InstanceNotFoundException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.List;

public class OnlyChartSizeParser extends ChartableParser<ChartSizeParameters> {
    public OnlyChartSizeParser(ChuuService dao, TimeFrameEnum defaultT, OptionalEntity... optionalEntity) {
        super(dao, defaultT, optionalEntity);
    }


    @Override

    protected void setUpErrorMessages() {
        errorMessages.put(1, "0 is not a valid value for a chart!");
    }


    @Override
    public ChartSizeParameters parseLogic(MessageReceivedEvent e, String[] words) throws InstanceNotFoundException {

        ChartParserAux chartParserAux = new ChartParserAux(words);
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

        LastFMData lastFMData = atTheEndOneUser(e, chartParserAux.getMessage());

        return new ChartSizeParameters(e, x, y, lastFMData.getChartMode(), lastFMData);
    }

    @Override
    public List<Explanation> getUsages() {
        return List.of(new ChartSizeExplanation(), new PermissiveUserExplanation());
    }

}
