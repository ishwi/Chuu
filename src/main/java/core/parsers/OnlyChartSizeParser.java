package core.parsers;

import core.parsers.exceptions.InvalidChartValuesException;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

public class OnlyChartSizeParser extends OptionableParser {
    public OnlyChartSizeParser(OptionalEntity optionalEntity) {
        super(optionalEntity);
    }

    @Override
    protected void setUpErrorMessages() {
        errorMessages.put(1, "0 is not a valid value for a chart!");
    }


    @Override
    public String[] parseLogic(MessageReceivedEvent e, String[] words) {

        ChartParserAux chartParserAux = new ChartParserAux(words);
        String x = "5";
        String y = "5";

        try {
            Point chartSize = chartParserAux.getChartSize();
            if (chartSize != null) {
                x = String.valueOf(chartSize.x);
                y = String.valueOf(chartSize.y);
            }
        } catch (InvalidChartValuesException ex) {
            this.sendError(getErrorMessage(1), e);
            return null;
        }
        return new String[]{x, y};

    }

    @Override
    public String getUsageLogic(String commandName) {
        return "**" + commandName + " *sizeXsize*  *Username* ** \n";
    }
}
