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
                "If an username it's not provided it defaults to authors account, all formats for naming other users are allowed", OptionType.USER);
    }
}
