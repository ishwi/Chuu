package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;

public class PermissiveUserExplanation implements Explanation {


    @Override
    public ExplanationLine explanation() {
        return new ExplanationLine("User",
                "If an username it's not provided it defaults to authors account, all formats for naming other users are allowed");
    }
}
