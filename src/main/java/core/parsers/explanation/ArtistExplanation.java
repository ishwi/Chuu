package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import core.parsers.explanation.util.Interactible;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public class ArtistExplanation implements Explanation {
    public static final String NAME = "artist";

    @Override
    public Interactible explanation() {
        return new ExplanationLineType(NAME, "The artist written will be used or if no artist is provided the artist that you are currently listening to will be used", OptionType.STRING);
    }

}
