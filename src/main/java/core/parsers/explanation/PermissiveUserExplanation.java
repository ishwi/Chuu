package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.explanation.util.Interactible;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class PermissiveUserExplanation implements Explanation {
    public static final String NAME = "user";


    @Override
    public Interactible explanation() {
        return new ExplanationLineType(NAME,
                "Who will be used for the command.", OptionType.USER);
    }
}
