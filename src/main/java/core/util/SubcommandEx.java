package core.util;

import core.parsers.Parser;

public interface SubcommandEx<Y extends Parser<?>> {

    Y getParser(Deps deps);


}
