package core.parsers.explanation;

import core.parsers.explanation.util.Explanation;
import core.parsers.explanation.util.ExplanationLine;

public class CountryExplanation implements Explanation {

    @Override
    public ExplanationLine explanation() {
        return new ExplanationLine("Country",
                "Country must come in the full name format or in the ISO 3166-1 alpha-2/alpha-3 format");
    }

}
