package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.explanation.util.Interactible;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class TwoUsersExplanation implements Explanation {
    public static final String NAME = "compare-user";

    @Override
    public Interactible explanation() {
        String usage = "At least one username is required. You can also supplant other users by writing first the other users name and then the target username.";
        OptionData data = new OptionData(OptionType.USER, NAME, "user to compare with");
        data.setRequired(true);
        return new ExplanationLine(NAME, usage, data);

    }
}
