package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.explanation.util.Interactible;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.apache.commons.lang3.StringUtils;

public class StrictUserExplanation implements Explanation {
    public static final String NAME = "user";


    @Override
    public Interactible explanation() {
        return new ExplanationLine("strict-user",
                "If an username it's not provided it defaults to authors account, only ping, tag format (user#number),discord id, u:username or lfm:lastfmname"
                , new OptionData(OptionType.USER, NAME, StringUtils.abbreviate("Person who will be used for the command. Defaults to the author.", 100))
        );
    }

}
