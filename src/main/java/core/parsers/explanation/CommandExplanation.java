package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;

public record CommandExplanation(String commandExplanation) implements Explanation {

    @Override
    public ExplanationLine explanation() {
        return new ExplanationLine(null,
                commandExplanation);
    }
}
