package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;

public class DecadeExplanation implements Explanation {

    @Override
    public ExplanationLine explanation() {
        return new ExplanationLine("Decade Range",
                "Decade Range can be either two years separated by a - (E.g.  2009 - 2013) or a two digit representative of a decade " +
                "(E.g. 20, 20s, 20's, 80s...)\n\t Default to the current decade if left empty");
    }

}
