package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.explanation.util.Interactible;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class ChartSizeExplanation implements Explanation {
    public static final String NAME = "chart-size";
    private static final OptionData optionData;

    static {
        optionData = new OptionData(OptionType.STRING, "chart-size", "If the size is not specified it defaults to 5x5");
        for (int i = 5; i > 0; i--) {
            String s = i + "x" + i;
            optionData.addChoice(s, s);
        }
        optionData.addChoice("4x2", "4x2");
        optionData.addChoice("5x2", "5x2");
        optionData.addChoice("10x10", "10x10");
        optionData.addChoice("20x20", "20x20");

    }

    @Override
    public Interactible explanation() {
        return new ExplanationLine("sizeXsize", "If the size is not specified it defaults to 5x5", optionData);
    }

}
