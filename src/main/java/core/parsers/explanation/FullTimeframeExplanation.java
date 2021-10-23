package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.explanation.util.Interactible;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public record FullTimeframeExplanation(TimeFrameEnum timeFrame) implements Explanation {
    public static final String NAME = "timeframe";
    private static final OptionData optionData;
    private static final OptionData from;
    private static final OptionData to;

    static {

        optionData = new OptionData(OptionType.STRING, NAME, "chart time-frame");
        for (TimeFrameEnum value : TimeFrameEnum.values()) {
            optionData.addChoice(value.toValueString(), value.getName());
        }
        from = new OptionData(OptionType.STRING, "from", "From: date parsing");
        to = new OptionData(OptionType.STRING, "to", "To: date parsing");
        for (TimeFrameEnum value : TimeFrameEnum.values()) {
            optionData.addChoice(value.toValueString(), value.getName());
        }
    }

    @Override
    public Interactible explanation() {
        return new ExplanationLine("timeframe", "" +
                "If a timeframe it's not specified defaults to " + timeFrame.toValueString() + "\n" +
                "The full alias of the timeframes can be used. Also you can specify a custom date like in the `since` command.", List.of(optionData, from, to));
    }

}
