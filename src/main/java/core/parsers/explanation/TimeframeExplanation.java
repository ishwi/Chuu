package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import dao.entities.TimeFrameEnum;

public class TimeframeExplanation implements Explanation {

    private final TimeFrameEnum timeFrame;

    public TimeframeExplanation(TimeFrameEnum timeFrame) {
        this.timeFrame = timeFrame;
    }

    @Override
    public ExplanationLine explanation() {
        return new ExplanationLine("Time Expression", "If a timeframe it's not specified defaults to " + timeFrame.toString() + "\n");
    }

}
