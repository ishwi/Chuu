package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.explanation.util.Interactible;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class StrictUserExplanation implements Explanation {
    public static final String NAME = "strict-user";


    @Override
    public Interactible explanation() {
        return new ExplanationLineType(NAME,
                "If an username it's not provided it defaults to authors account, only ping, tag format (user#number),discord id, u:username or lfm:lastfmname"
                , OptionType.USER
        );
    }

}
