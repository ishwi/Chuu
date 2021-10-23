package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.explanation.util.Interactible;
import dao.entities.NaturalTimeFrameEnum;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.commons.lang3.ArrayUtils;

public record NaturalTimeframeExplanation(NaturalTimeFrameEnum timeFrame) implements Explanation {
    public static final String NAME = "timeframe";
    private static final OptionData optionData;

    static {
        optionData = new OptionData(OptionType.STRING, "timeframe", "the timeframe to use");
        NaturalTimeFrameEnum[] values = NaturalTimeFrameEnum.values();
        ArrayUtils.reverse(values);
        for (NaturalTimeFrameEnum value : values) {
            optionData.addChoice(value.toValueString(), value.getName());
        }
    }

    @Override
    public Interactible explanation() {
        return new ExplanationLine("natural-timeframe",
                "One of Year,Quarter,Month,All,Semester,Week,Day,Hour,Minute,Second with plural forms and abbreviations included\n" +
                        "If the time expression is not specified it defaults to " + timeFrame.toValueString() + "\n", optionData);
    }

}
