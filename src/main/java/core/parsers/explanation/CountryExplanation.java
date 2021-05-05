package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.explanation.util.Interactible;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class CountryExplanation implements Explanation {

    @Override
    public Interactible explanation() {
        return new ExplanationLineType("country",
                "Country must come in the full name format or in the ISO 3166-1 alpha-2/alpha-3 format", OptionType.STRING);
    }

}
