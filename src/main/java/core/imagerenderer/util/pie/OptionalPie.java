package core.imagerenderer.util.pie;

import core.parsers.Parser;
import core.parsers.utils.Optionals;

public class OptionalPie {

    public OptionalPie(Parser<?> parser) {
        parser.replaceOptional("pie", (Optionals.PIE.opt));
    }
}
