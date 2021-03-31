package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;

public class RatingExplanation implements Explanation {
    @Override
    public ExplanationLine explanation() {
        return new ExplanationLine("Rating", "The rating can only be interpreted in the scale 1-10 , 0.5 to 5 (with .5 decimals) and 0-100");
    }
}
