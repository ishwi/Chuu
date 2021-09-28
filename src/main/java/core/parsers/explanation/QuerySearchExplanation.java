package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.explanation.util.Interactible;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class QuerySearchExplanation implements Explanation {

    public static final String NAME = "query";


    @Override
    public Interactible explanation() {
        return new ExplanationLineType(NAME, "If you don't introduce a query it takes your now playing song as a parameter", OptionType.STRING);
    }
}
