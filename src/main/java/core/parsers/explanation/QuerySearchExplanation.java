package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;

public class QuerySearchExplanation implements Explanation {
    @Override
    public ExplanationLine explanation() {
        return new ExplanationLine("[whatever you want to search for]", "If you don't introduce a query it takes your now playing song as a parameter");
    }
}
