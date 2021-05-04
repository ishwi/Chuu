package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.explanation.util.Interactible;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class RatingExplanation implements Explanation {

    private static final OptionData optionData;

    static {
        optionData = new OptionData(OptionType.INTEGER, "Rating", "Value of the rating");
        for (int i = 1; i <= 10; i++) {
            String name = String.valueOf(i);
            optionData.addChoice(name, name);
        }
    }

    @Override
    public Interactible explanation() {
        return new ExplanationLine("Rating", "The rating can only be interpreted in the scale 1-10 , 0.5 to 5 (with .5 decimals) and 0-100", optionData);
    }
}
