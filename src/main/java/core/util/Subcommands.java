package core.util;

import core.parsers.params.CommandParameters;

public interface Subcommands<T extends CommandParameters> {

    T fromParameters(String parameters);
}
