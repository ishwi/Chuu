package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;

public class StrictUserExplanation implements Explanation {


    @Override
    public ExplanationLine explanation() {
        return new ExplanationLine("Strict User",
                "If an username it's not provided it defaults to authors account, only ping, tag format (user#number),discord id, u:username or lfm:lastfmname");
    }
}
