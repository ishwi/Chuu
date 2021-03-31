package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;

public class YearExplanation implements Explanation {
    @Override
    public ExplanationLine explanation() {
        return new ExplanationLine("Year",
                "If the year is not specified it defaults to the current year"
        );
    }
}
