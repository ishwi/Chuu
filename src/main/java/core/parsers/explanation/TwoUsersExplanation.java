package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;

public class TwoUsersExplanation implements Explanation {
    @Override
    public ExplanationLine explanation() {
        return new ExplanationLine("User",
                "At least one username is required. You can also supplant other users by writing first the other users name and then the target username.");

    }
}
