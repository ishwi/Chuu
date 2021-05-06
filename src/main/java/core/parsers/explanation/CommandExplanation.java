package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import core.parsers.explanation.util.Interactible;

import java.util.Collections;

public record CommandExplanation(String commandExplanation) implements Explanation {

    @Override
    public Interactible explanation() {
        return new ExplanationLine(null,
                commandExplanation, Collections.emptyList());
    }
}
