package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;

public class GenreExplanation implements Explanation {
    @Override
    public ExplanationLine explanation() {
        return new ExplanationLine("Genre", "A genre can be specified or otherwise it defaults to the genre of your current track|album|artist according to last.fm");
    }
}
