package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;
import dao.entities.NaturalTimeFrameEnum;

public record NaturalTimeframeExplanation(NaturalTimeFrameEnum timeFrame) implements Explanation {

    @Override
    public ExplanationLine explanation() {
        return new ExplanationLine("Time Expression",
                "One of Year,Quarter,Month,All,Semester,Week,Day,Hour,Minute,Second with plural forms and abbreviations included\n" +
                "If the time expression is not specified it defaults to " + timeFrame.toValueString() + "\n");
    }

}
