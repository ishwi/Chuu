package core.parsers.params;

import core.commands.Context;

public class EnumParameters<T extends Enum<T>> extends CommandParameters {
    private final T element;

    public EnumParameters(Context e, T element) {
        super(e);
        this.element = element;
    }


    public T getElement() {
        return element;
    }
}
