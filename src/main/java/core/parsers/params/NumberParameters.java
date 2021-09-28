package core.parsers.params;

import core.commands.Context;

public class NumberParameters<T extends CommandParameters> extends ExtraParameters<T, Long> {

    public NumberParameters(Context e, T innerParams, Long extraParam) {
        super(e, innerParams, extraParam);
    }

}
