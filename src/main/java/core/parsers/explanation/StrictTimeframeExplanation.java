package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.explanation.util.Interactible;
import dao.entities.TimeFrameEnum;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public record StrictTimeframeExplanation(TimeFrameEnum timeFrame) implements Explanation {

    private static final OptionData optionData;


    static {

        optionData = new OptionData(OptionType.STRING, "timeframe", "time-frame of the chart");
        for (TimeFrameEnum value : TimeFrameEnum.values()) {
            optionData.addChoice(value.toValueString(), value.name());
        }
    }

    @Override
    public Interactible explanation() {
        return new ExplanationLine("[d,w,m,q,s,y]", "If a timeframe it's not specified defaults to " + timeFrame.toString() + "\n" +
                                                    "Due to being able to provide an entity name and the timeframe, some conflicts may occur if the timeframe keyword appears on the entity name, to reduce possible conflicts only the one letter shorthand is available for the timeframe, the [a] shorthand is also disabled to reduce more conflicts since its the default time frame applied", optionData);
    }

}
