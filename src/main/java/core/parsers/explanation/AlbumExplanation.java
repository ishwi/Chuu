package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;

public class AlbumExplanation implements Explanation {

    @Override
    public ExplanationLine explanation() {
        return new ExplanationLine("Artist - Album", "The album written will be used or if no album is provided the one that you are currently listening to will be used");
    }

}
