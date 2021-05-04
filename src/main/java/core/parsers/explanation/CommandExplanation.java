package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.explanation.util.Interactible;

public record CommandExplanation(String commandExplanation) implements Explanation {

    @Override
    public Interactible explanation() {
        return new ExplanationLine(null,
                commandExplanation, null);
    }
}
