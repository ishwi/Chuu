package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import dao.entities.TimeFrameEnum;

public record FullTimeframeExplanation(TimeFrameEnum timeFrame) implements Explanation {

    @Override
    public ExplanationLine explanation() {
        return new ExplanationLine("Timeframe", "" +
                                                "If a timeframe it's not specified defaults to " + timeFrame.toValueString() + "\n" +
                                                "The full alias of the timeframes can be used. Also you can specify a custom date like in the `since` command.");
    }

}
