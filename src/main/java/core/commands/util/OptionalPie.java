package core.commands.util;

import core.parsers.OptionalEntity;
import core.parsers.Parser;

public class OptionalPie {

    public OptionalPie(Parser parser) {
        parser.replaceOptional("--pie", (new OptionalEntity("--pie", "display it as a chart pie")));
        ;
    }
}
