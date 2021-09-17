package core.parsers.params;

import core.commands.Context;

public class ExtraParameters<T extends CommandParameters, J> extends CommandParameters {
    private final T innerParams;
    private final J extraParam;

    public ExtraParameters(Context e, T innerParams, J extraParam) {
        super(e);
        this.innerParams = innerParams;
        this.extraParam = extraParam;
    }

    public T getInnerParams() {
        return innerParams;
    }

    public J getExtraParam() {
        return extraParam;
    }
}
