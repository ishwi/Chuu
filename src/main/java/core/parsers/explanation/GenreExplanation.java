package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.explanation.util.Interactible;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class GenreExplanation implements Explanation {
    @Override
    public Interactible explanation() {
        return new ExplanationLineType("genre", "A genre can be specified or otherwise it defaults to the genre of your current track|album|artist according to last.fm", OptionType.STRING);
    }
}
