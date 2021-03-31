package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;

public class UrlExplanation implements Explanation {


    @Override
    public ExplanationLine explanation() {
        return new ExplanationLine("url",
                "A url to an image. It can be a plain image or you can upload directly to discord using the command");
    }
}
