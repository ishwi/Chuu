package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;

public class ChartSizeExplanation implements Explanation {

    @Override
    public ExplanationLine explanation() {
        return new ExplanationLine("sizeXsize", "If the size is not specified it defaults to 5x5");
    }

}
