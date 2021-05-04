package core.parsers.params;

import core.commands.Context;

public class StringParameters extends CommandParameters {
    private final String value;

    public StringParameters(Context e, String value) {
        super(e);
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
