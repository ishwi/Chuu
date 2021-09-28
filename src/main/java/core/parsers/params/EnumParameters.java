package core.parsers.params;

import core.commands.Context;

public class EnumParameters<T extends Enum<T>> extends CommandParameters {
    private final T element;
    private final String params;

    public EnumParameters(Context e, T element) {
        super(e);
        this.element = element;
        this.params = null;
    }

    public EnumParameters(Context e, T element, String params) {
        super(e);
        this.element = element;
        this.params = params;
    }


    public T getElement() {
        return element;
    }

    public String getParams() {
        return params;
    }
}
