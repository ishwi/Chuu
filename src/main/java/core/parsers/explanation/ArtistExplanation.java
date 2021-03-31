package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;

public class ArtistExplanation implements Explanation {

    @Override
    public ExplanationLine explanation() {
        return new ExplanationLine("Artist", "The artist written will be used or if no artist is provided the artist that you are currently listening to will be used");
    }

}
