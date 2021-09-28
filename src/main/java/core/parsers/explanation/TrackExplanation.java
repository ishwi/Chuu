package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public record TrackExplanation(Explanation artist, Explanation song) {
    public static final String NAME = "song";

    public TrackExplanation() {
        this(
                () -> new ExplanationLineType(ArtistExplanation.NAME, "The artist to query for", OptionType.STRING)
                , () -> new ExplanationLineType(NAME, "The song of the artist to query for, separated with -", OptionType.STRING));
    }

}
