package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.explanation.util.Interactible;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class DecadeExplanation implements Explanation {

    @Override
    public Interactible explanation() {
        return new ExplanationLineType("Decade Range",
                "Decade Range can be either two years separated by a - (E.g.  2009 - 2013) or a two digit representative of a decade " +
                "(E.g. 20, 20s, 20's, 80s...)\n\t Default to the current decade if left empty", OptionType.STRING);
    }

}
