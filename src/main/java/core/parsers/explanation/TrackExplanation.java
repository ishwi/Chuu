package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;

public class TrackExplanation implements Explanation {

    @Override
    public ExplanationLine explanation() {
        return new ExplanationLine("Artist - Song", "The song written will be used or if no song is provided the one that you are currently listening to will be used");
    }

}
