package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import dao.entities.TimeFrameEnum;

public class StrictTimeframeExplanation implements Explanation {

    private final TimeFrameEnum timeFrame;

    public StrictTimeframeExplanation(TimeFrameEnum timeFrame) {
        this.timeFrame = timeFrame;
    }

    @Override
    public ExplanationLine explanation() {
        return new ExplanationLine("[d,w,m,q,s,y]", "If a timeframe it's not specified defaults to " + timeFrame.toString() + "\n" +
                                                    "Due to being able to provide an entity name and the timeframe, some conflicts may occur if the timeframe keyword appears on the entity name, to reduce possible conflicts only the one letter shorthand is available for the timeframe, the [a] shorthand is also disabled to reduce more conflicts since its the default time frame applied");
    }

}
