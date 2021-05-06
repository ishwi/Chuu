package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.explanation.util.Interactible;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.time.Year;
import java.util.List;

public class DecadeExplanation implements Explanation {
    public static final String NAME = "decade";
    public static final String RANGE_START = "start";
    public static final String RANGE_END = "end";


    static final List<OptionData> datas;

    static {
        OptionData optionData = new OptionData(OptionType.STRING, NAME, "The decade to choose");
        int now = Year.now().getValue();
        int currentDecade = now % 100 / 10;
        for (int i = currentDecade; i >= 0; i--) {
            String decade = String.format("%d0s", i);
            optionData.addChoice(decade, decade);
        }
        for (int i = 9; i > currentDecade; i--) {
            String decade = String.format("%d0s", i);
            optionData.addChoice(decade, decade);
        }
        for (int i = currentDecade; i >= 0; i--) {
            String decade = String.format("19%d0s", i);
            optionData.addChoice(decade, decade);
        }
        optionData.addChoice("1890s", "1890s");
        optionData.addChoice("1880s", "1880s");
        optionData.addChoice("1870s", "1870s");

        OptionData op1 = new OptionData(OptionType.STRING, RANGE_START, "Year/Decade to start the range");
        OptionData op2 = new OptionData(OptionType.STRING, RANGE_END, "Year/Decade to end the range");

        datas = List.of(optionData, op1, op2);
    }

    @Override
    public Interactible explanation() {

        return new ExplanationLine("Decade Range",
                "Decade Range can be either two years separated by a - (E.g.  2009 - 2013) or a two digit representative of a decade " +
                "(E.g. 20, 20s, 20's, 80s...)\n\t Default to the current decade if left empty", datas);
    }

}
