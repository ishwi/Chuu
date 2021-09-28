package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLineType;
import net.dv8tion.jda.api.interactions.commands.OptionType;

public record AlbumExplanation(Explanation artist, Explanation album) {
    public static final String NAME = "album";


    public AlbumExplanation() {
        this(
                () -> new ExplanationLineType(ArtistExplanation.NAME, "The artist to query for", OptionType.STRING),
                () -> new ExplanationLineType(NAME, "The album of the artist to query for, separated with -", OptionType.STRING));

    }
}

