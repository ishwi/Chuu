package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.explanation.util.Interactible;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public record FullTimeframeExplanation(TimeFrameEnum timeFrame) implements Explanation {
    public static final String NAME = "timeframe";
    private static final OptionData optionData;

    static {

        optionData = new OptionData(OptionType.STRING, NAME, "time-frame of the chart");
        for (TimeFrameEnum value : TimeFrameEnum.values()) {
            optionData.addChoice(value.toValueString(), value.name());
        }
    }

    @Override
    public Interactible explanation() {
        return new ExplanationLine("Timeframe", "" +
                                                "If a timeframe it's not specified defaults to " + timeFrame.toValueString() + "\n" +
                                                "The full alias of the timeframes can be used. Also you can specify a custom date like in the `since` command.", optionData);
    }

}
