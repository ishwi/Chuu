package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.explanation.util.Interactible;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class ChartSizeExplanation implements Explanation {
    public static final String NAME = "chart-size";
    private static final List<OptionData> optionData;

    static {

        optionData = List.of(new OptionData(OptionType.INTEGER, "columns", "Columns of the chart"),
                new OptionData(OptionType.INTEGER, "rows", "Rows of the chart"));
    }

    @Override
    public Interactible explanation() {
        return new ExplanationLine("sizeXsize", "If the size is not specified it defaults to 5x5", optionData);
    }

}
